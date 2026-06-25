package br.com.valora.Service;

import br.com.valora.model.Veiculo;
import java.util.List;
import java.util.Optional;

public interface VeiculoService {
    List<Veiculo> findAll();
    Optional<Veiculo> findById(Integer id);
    Veiculo save(Veiculo veiculo);
    Veiculo update(Integer id, Veiculo veiculo);
    void delete(Integer id);
}