package br.com.valora.Service.impl;

import br.com.valora.model.Produto;
import br.com.valora.model.ProdutoServico;
import br.com.valora.model.RegistroServico;
import br.com.valora.repository.ProdutoRepository;
import br.com.valora.repository.ProdutoServicoRepository;
import br.com.valora.repository.RegistroServicoRepository;
import br.com.valora.Service.RegistroServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RegistroServicoServiceImpl implements RegistroServicoService {

    private final RegistroServicoRepository registroServicoRepository;
    private final ProdutoRepository         produtoRepository;
    private final ProdutoServicoRepository  produtoServicoRepository;

    public RegistroServicoServiceImpl(
            RegistroServicoRepository registroServicoRepository,
            ProdutoRepository produtoRepository,
            ProdutoServicoRepository produtoServicoRepository) {
        this.registroServicoRepository = registroServicoRepository;
        this.produtoRepository         = produtoRepository;
        this.produtoServicoRepository  = produtoServicoRepository;
    }

    // Helpers de estoque

    /**
     * Valida e desconta estoque dos produtos vinculados ao lancamento.
     * Lanca 409 se qualquer produto nao tiver quantidade suficiente.
     */
    private void descontarEstoque(Integer registroId) {
        List<ProdutoServico> itens = produtoServicoRepository.findByRegistroServicoId(registroId);
        // Valida tudo antes de alterar
        for (ProdutoServico item : itens) {
            Produto p = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Produto ID " + item.getProdutoId() + " nao encontrado."));
            if (p.getQuantidade() < item.getQuantidade()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Estoque insuficiente para \"" + p.getNome() + "\". "
                                + "Disponivel: " + p.getQuantidade() + ", necessario: " + item.getQuantidade() + ".");
            }
        }
        // Desconta
        for (ProdutoServico item : itens) {
            Produto p = produtoRepository.findById(item.getProdutoId()).get();
            p.setQuantidade(p.getQuantidade() - item.getQuantidade());
            produtoRepository.save(p);
        }
    }

    /**
     * Devolve ao estoque os produtos vinculados ao lancamento.
     */
    private void devolverEstoque(Integer registroId) {
        List<ProdutoServico> itens = produtoServicoRepository.findByRegistroServicoId(registroId);
        for (ProdutoServico item : itens) {
            produtoRepository.findById(item.getProdutoId()).ifPresent(p -> {
                p.setQuantidade(p.getQuantidade() + item.getQuantidade());
                produtoRepository.save(p);
            });
        }
    }

    // CRUD

    @Override
    public List<RegistroServico> findAll() {
        return registroServicoRepository.findAll();
    }

    @Override
    public Optional<RegistroServico> findById(Integer id) {
        return registroServicoRepository.findById(id);
    }

    /**
     * Novo lancamento: salva e desconta estoque imediatamente.
     * O controller salva os produto-servico ANTES de chamar este metodo,
     * entao os itens ja estao no banco quando descontarEstoque() roda.
     */
    @Override
    public RegistroServico save(RegistroServico registroServico) {
        registroServico.setStatus("ABERTO");
        return registroServicoRepository.save(registroServico);
    }

    /**
     * Chamado pelo frontend APOS salvar todos os produtos do lancamento.
     * Desconta o estoque com base nos itens ja persistidos.
     */
    @Override
    @Transactional
    public RegistroServico confirmarEstoque(Integer id) {
        return registroServicoRepository.findById(id).map(r -> {
            descontarEstoque(id);
            return r;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Lancamento nao encontrado."));
    }

    /**
     * Edicao: devolve o estoque dos itens antigos, salva, desconta com os novos itens.
     */
    @Override
    @Transactional
    public RegistroServico update(Integer id, RegistroServico detalhes) {
        return registroServicoRepository.findById(id).map(existente -> {
            if ("FECHADO".equals(existente.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Nao e possivel editar um lancamento FECHADO. Reabra-o primeiro.");
            }
            // Devolve estoque dos itens atuais antes de aplicar os novos
            devolverEstoque(id);

            existente.setFuncionarioId(detalhes.getFuncionarioId());
            existente.setClienteId(detalhes.getClienteId());
            existente.setServicoId(detalhes.getServicoId());
            existente.setDataServico(detalhes.getDataServico());
            existente.setHoraInicio(detalhes.getHoraInicio());
            existente.setHoraFim(detalhes.getHoraFim());
            existente.setVeiculoId(detalhes.getVeiculoId());
            existente.setKmPercorridos(detalhes.getKmPercorridos());
            existente.setObservacao(detalhes.getObservacao());
            RegistroServico salvo = registroServicoRepository.save(existente);

            // O desconto dos novos itens e feito via confirmarEstoque()
            // chamado pelo frontend apos salvar os produtos
            return salvo;
        }).orElse(null);
    }

    /**
     * Exclusao: devolve estoque se nao estiver FECHADO.
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        registroServicoRepository.findById(id).ifPresent(r -> {
            if ("FECHADO".equals(r.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Nao e possivel excluir um lancamento FECHADO. Reabra-o primeiro.");
            }
            devolverEstoque(id);
            registroServicoRepository.deleteById(id);
        });
    }

    /**
     * Alterar status: so devolve estoque ao reabrir um FECHADO.
     * Nos demais casos o estoque ja foi tratado no save/update/delete.
     */
    @Override
    @Transactional
    public RegistroServico alterarStatus(Integer id, String novoStatus) {
        List<String> validos = List.of("ABERTO", "EM_ANDAMENTO", "FECHADO");
        if (!validos.contains(novoStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status invalido. Use: ABERTO, EM_ANDAMENTO ou FECHADO.");
        }
        return registroServicoRepository.findById(id).map(r -> {
            // Reabrir FECHADO: devolve estoque
            if ("FECHADO".equals(r.getStatus()) && "ABERTO".equals(novoStatus)) {
                devolverEstoque(id);
            }
            r.setStatus(novoStatus);
            return registroServicoRepository.save(r);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Lancamento nao encontrado."));
    }
}