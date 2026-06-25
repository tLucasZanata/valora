package br.com.valora.controllers;

import br.com.valora.model.Servico;
import br.com.valora.Service.ServicoService;
import br.com.valora.Service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService servicoService;
    private final AuditService auditService;

    public ServicoController(ServicoService servicoService, AuditService auditService) {
        this.servicoService = servicoService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Servico>> listarTodos() {
        return ResponseEntity.ok(servicoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servico> buscarPorId(@PathVariable Integer id) {
        return servicoService.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Servico> criar(@RequestBody Servico obj) {
        Servico novo = servicoService.save(obj);
        auditService.registrarCriacao("SERVICO", novo.getId(), novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizar(@PathVariable Integer id, @RequestBody Servico detalhes) {
        Servico antes = servicoService.findById(id).orElse(null);
        Servico atualizado = servicoService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("SERVICO", id, antes, atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        Servico antes = servicoService.findById(id).orElse(null);
        servicoService.delete(id);
        auditService.registrarExclusao("SERVICO", id, antes);
        return ResponseEntity.noContent().build();
    }
}
