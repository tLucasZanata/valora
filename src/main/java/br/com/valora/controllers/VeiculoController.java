package br.com.valora.controllers;

import br.com.valora.model.Veiculo;
import br.com.valora.Service.VeiculoService;
import br.com.valora.Service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final AuditService auditService;

    public VeiculoController(VeiculoService veiculoService, AuditService auditService) {
        this.veiculoService = veiculoService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Veiculo>> listarTodos() {
        return ResponseEntity.ok(veiculoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veiculo> buscarPorId(@PathVariable Integer id) {
        return veiculoService.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Veiculo> criar(@RequestBody Veiculo obj) {
        Veiculo novo = veiculoService.save(obj);
        auditService.registrarCriacao("VEICULO", novo.getId(), novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veiculo> atualizar(@PathVariable Integer id, @RequestBody Veiculo detalhes) {
        Veiculo antes = veiculoService.findById(id).orElse(null);
        Veiculo atualizado = veiculoService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("VEICULO", id, antes, atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        Veiculo antes = veiculoService.findById(id).orElse(null);
        veiculoService.delete(id);
        auditService.registrarExclusao("VEICULO", id, antes);
        return ResponseEntity.noContent().build();
    }
}
