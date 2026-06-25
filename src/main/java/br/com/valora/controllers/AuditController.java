package br.com.valora.controllers;

import br.com.valora.model.AuditLog;
import br.com.valora.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> buscar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        LocalDateTime dtInicio = inicio != null ? inicio.atStartOfDay() : null;
        LocalDateTime dtFim    = fim    != null ? fim.atTime(LocalTime.MAX) : null;

        Page<AuditLog> resultado = auditLogRepository.buscarComFiltros(
                usuario, acao, entidade, dtInicio, dtFim,
                PageRequest.of(page, size));

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> recentes() {
        return ResponseEntity.ok(auditLogRepository.findTop100ByOrderByDataHoraDesc());
    }

    @GetMapping("/acoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> acoes() {
        return ResponseEntity.ok(List.of(
                "CRIAR", "EDITAR", "EXCLUIR", "ALTERAR_STATUS", "LOGIN", "LOGIN_FALHOU"
        ));
    }

    @GetMapping("/entidades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> entidades() {
        return ResponseEntity.ok(List.of(
                "CLIENTE", "FUNCIONARIO", "SERVICO", "PRODUTO", "VEICULO",
                "LANCAMENTO", "USUARIO"
        ));
    }
}
