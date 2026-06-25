package br.com.valora.repository;

import br.com.valora.model.RegistroServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegistroServicoRepository extends JpaRepository<RegistroServico, Integer> {
}