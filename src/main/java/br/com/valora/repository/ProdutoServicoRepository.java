package br.com.valora.repository;

import br.com.valora.model.ProdutoServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoServicoRepository extends JpaRepository<ProdutoServico, Integer> {

    List<ProdutoServico> findByRegistroServicoId(Integer registroServicoId);
}