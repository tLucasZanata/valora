package br.com.valora.Service;

import br.com.valora.model.ProdutoServico;
import java.util.List;
import java.util.Optional;

public interface ProdutoServicoService {
    List<ProdutoServico> findAll();
    List<ProdutoServico> findByRegistroServicoId(Integer registroServicoId);
    Optional<ProdutoServico> findById(Integer id);
    ProdutoServico save(ProdutoServico produtoServico);
    ProdutoServico update(Integer id, ProdutoServico produtoServico);
    void delete(Integer id);
}