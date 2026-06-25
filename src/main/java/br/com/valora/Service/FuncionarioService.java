package br.com.valora.Service;

import br.com.valora.model.Funcionario;
import java.util.List;
import java.util.Optional;

public interface FuncionarioService {
    List<Funcionario> findAll();
    Optional<Funcionario> findById(Integer id);
    Funcionario save(Funcionario funcionario);
    Funcionario update(Integer id, Funcionario funcionarioDetails);
    void delete(Integer id);
}