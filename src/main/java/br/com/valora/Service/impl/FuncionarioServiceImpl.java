package br.com.valora.Service.impl;

import br.com.valora.model.Funcionario;
import br.com.valora.repository.FuncionarioRepository;
import br.com.valora.Service.FuncionarioService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioServiceImpl(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public List<Funcionario> findAll() {
        return funcionarioRepository.findAll();
    }

    @Override
    public Optional<Funcionario> findById(Integer id) {
        return funcionarioRepository.findById(id);
    }

    @Override
    public Funcionario save(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    @Override
    public Funcionario update(Integer id, Funcionario funcionarioDetails) {
        return funcionarioRepository.findById(id).map(funcionarioExistente -> {

            // Atualizacao de campos
            funcionarioExistente.setNome(funcionarioDetails.getNome());
            funcionarioExistente.setCargo(funcionarioDetails.getCargo());
            funcionarioExistente.setContato(funcionarioDetails.getContato());
            funcionarioExistente.setValorHora(funcionarioDetails.getValorHora());
            funcionarioExistente.setEmail(funcionarioDetails.getEmail());

            return funcionarioRepository.save(funcionarioExistente);
        }).orElse(null);
    }


    @Override
    public void delete(Integer id) {
        funcionarioRepository.deleteById(id);
    }
}