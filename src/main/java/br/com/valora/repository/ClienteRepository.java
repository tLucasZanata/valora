package br.com.valora.repository;

import br.com.valora.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
// O segundo parâmetro é o tipo da chave primária (int = Integer)
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}