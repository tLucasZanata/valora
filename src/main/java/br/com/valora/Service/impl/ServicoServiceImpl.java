package br.com.valora.Service.impl;

import br.com.valora.model.Funcionario;
import br.com.valora.model.Servico;
import br.com.valora.repository.FuncionarioRepository;
import br.com.valora.repository.ServicoRepository;
import br.com.valora.Service.ServicoService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoServiceImpl implements ServicoService {

    private final ServicoRepository servicoRepository;
    private final FuncionarioRepository funcionarioRepository;

    public ServicoServiceImpl(ServicoRepository servicoRepository,
                              FuncionarioRepository funcionarioRepository) {
        this.servicoRepository = servicoRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public List<Servico> findAll() {
        return servicoRepository.findAll();
    }

    @Override
    public Optional<Servico> findById(Integer id) {
        return servicoRepository.findById(id);
    }

    @Override
    public Servico save(Servico servico) {
        // Resolve os objetos Funcionario a partir dos IDs recebidos
        // O frontend envia: "funcionarios": [{"id": 1}, {"id": 2}, ...]
        // Precisamos buscar as entidades reais no banco para evitar erros de persistência
        servico.setFuncionarios(resolverFuncionarios(servico.getFuncionarios()));
        return servicoRepository.save(servico);
    }

    @Override
    public Servico update(Integer id, Servico servicoDetails) {
        return servicoRepository.findById(id).map(servicoExistente -> {
            servicoExistente.setNome(servicoDetails.getNome());
            servicoExistente.setCodigo(servicoDetails.getCodigo());
            servicoExistente.setDescricao(servicoDetails.getDescricao());
            servicoExistente.setObservacoes(servicoDetails.getObservacoes());
            servicoExistente.setValorHora(servicoDetails.getValorHora());
            servicoExistente.setIsento(servicoDetails.getIsento());
            // Atualiza a lista de funcionários resolvidos
            servicoExistente.setFuncionarios(resolverFuncionarios(servicoDetails.getFuncionarios()));
            return servicoRepository.save(servicoExistente);
        }).orElse(null);
    }

    @Override
    public void delete(Integer id) {
        servicoRepository.deleteById(id);
    }

    /**
     * Recebe a lista de Funcionario com apenas o ID preenchido (vindo do JSON do frontend)
     * e retorna as entidades completas buscadas no banco.
     */
    private List<Funcionario> resolverFuncionarios(List<Funcionario> funcionariosRecebidos) {
        if (funcionariosRecebidos == null || funcionariosRecebidos.isEmpty()) {
            return new ArrayList<>();
        }
        List<Funcionario> resolvidos = new ArrayList<>();
        for (Funcionario f : funcionariosRecebidos) {
            funcionarioRepository.findById(f.getId()).ifPresent(resolvidos::add);
        }
        return resolvidos;
    }
}