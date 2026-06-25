package br.com.valora.Service;

import br.com.valora.model.Servico;
import java.util.List;
import java.util.Optional;

public interface ServicoService {
    List<Servico> findAll();
    Optional<Servico> findById(Integer id);
    Servico save(Servico servico);
    Servico update(Integer id, Servico servicoDetails);
    void delete(Integer id);
}