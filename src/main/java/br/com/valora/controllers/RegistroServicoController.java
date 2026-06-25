package br.com.valora.controllers;

import br.com.valora.model.RegistroServico;
import br.com.valora.Service.AuditService;
import br.com.valora.Service.RegistroServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registros")
public class RegistroServicoController {

    private final RegistroServicoService registroServicoService;
    private final AuditService           auditService;

    public RegistroServicoController(RegistroServicoService registroServicoService,
                                     AuditService auditService) {
        this.registroServicoService = registroServicoService;
        this.auditService           = auditService;
    }

    @GetMapping
    public ResponseEntity<List<RegistroServico>> listarTodos() {
        return ResponseEntity.ok(registroServicoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroServico> buscarPorId(@PathVariable Integer id) {
        return registroServicoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RegistroServico> criarRegistro(@RequestBody RegistroServico registroServico) {
        RegistroServico novo = registroServicoService.save(registroServico);
        auditService.registrarCriacao("LANCAMENTO", novo.getId(), novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistroServico> atualizarRegistro(@PathVariable Integer id,
                                                             @RequestBody RegistroServico detalhes) {
        RegistroServico antes = registroServicoService.findById(id).orElse(null);
        RegistroServico atualizado = registroServicoService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("LANCAMENTO", id, antes, atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRegistro(@PathVariable Integer id) {
        RegistroServico antes = registroServicoService.findById(id).orElse(null);
        registroServicoService.delete(id);
        auditService.registrarExclusao("LANCAMENTO", id, antes);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmar-estoque")
    public ResponseEntity<RegistroServico> confirmarEstoque(@PathVariable Integer id) {
        RegistroServico r = registroServicoService.confirmarEstoque(id);
        return ResponseEntity.ok(r);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RegistroServico> alterarStatus(@PathVariable Integer id,
                                                         @RequestBody Map<String, String> body) {
        String novoStatus = body.get("status");
        if (novoStatus == null || novoStatus.isBlank()) return ResponseEntity.badRequest().build();

        RegistroServico antes = registroServicoService.findById(id).orElse(null);
        String statusAntes = antes != null ? antes.getStatus() : "?";

        RegistroServico atualizado = registroServicoService.alterarStatus(id, novoStatus.toUpperCase());
        auditService.registrarAlteracaoStatus("LANCAMENTO", id, statusAntes, novoStatus.toUpperCase());
        return ResponseEntity.ok(atualizado);
    }
}
