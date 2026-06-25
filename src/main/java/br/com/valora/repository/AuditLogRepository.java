package br.com.valora.repository;

import br.com.valora.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Busca com filtros opcionais de usuário, ação, entidade e período
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:usuario IS NULL OR LOWER(a.usuario) LIKE LOWER(CONCAT('%', :usuario, '%'))) AND " +
            "(:acao IS NULL OR a.acao = :acao) AND " +
            "(:entidade IS NULL OR a.entidade = :entidade) AND " +
            "(:inicio IS NULL OR a.dataHora >= :inicio) AND " +
            "(:fim IS NULL OR a.dataHora <= :fim) " +
            "ORDER BY a.dataHora DESC")
    Page<AuditLog> buscarComFiltros(
            @Param("usuario") String usuario,
            @Param("acao") String acao,
            @Param("entidade") String entidade,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable);

    List<AuditLog> findTop100ByOrderByDataHoraDesc();
}