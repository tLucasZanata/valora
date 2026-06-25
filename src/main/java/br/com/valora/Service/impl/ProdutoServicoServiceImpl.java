package br.com.valora.Service.impl;

import br.com.valora.model.ProdutoServico;
import br.com.valora.repository.ProdutoServicoRepository;
import br.com.valora.Service.ProdutoServicoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServicoServiceImpl implements ProdutoServicoService {

    private final ProdutoServicoRepository produtoServicoRepository;

    public ProdutoServicoServiceImpl(ProdutoServicoRepository produtoServicoRepository) {
        this.produtoServicoRepository = produtoServicoRepository;
    }

    @Override
    public List<ProdutoServico> findAll() {
        return produtoServicoRepository.findAll();
    }

    @Override
    public List<ProdutoServico> findByRegistroServicoId(Integer registroServicoId) {
        return produtoServicoRepository.findByRegistroServicoId(registroServicoId);
    }

    @Override
    public Optional<ProdutoServico> findById(Integer id) {
        return produtoServicoRepository.findById(id);
    }

    @Override
    public ProdutoServico save(ProdutoServico produtoServico) {
        return produtoServicoRepository.save(produtoServico);
    }

    @Override
    public ProdutoServico update(Integer id, ProdutoServico details) {
        return produtoServicoRepository.findById(id).map(existente -> {
            existente.setProdutoId(details.getProdutoId());
            existente.setRegistroServicoId(details.getRegistroServicoId());
            existente.setQuantidade(details.getQuantidade());
            return produtoServicoRepository.save(existente);
        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        produtoServicoRepository.deleteById(id);
    }
}
