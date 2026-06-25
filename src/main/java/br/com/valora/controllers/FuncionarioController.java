package br.com.valora.controllers;

import br.com.valora.model.Funcionario;
import br.com.valora.Service.FuncionarioService;
import br.com.valora.Service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final AuditService auditService;

    public FuncionarioController(FuncionarioService funcionarioService, AuditService auditService) {
        this.funcionarioService = funcionarioService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Funcionario>> listarTodos() {
        return ResponseEntity.ok(funcionarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Funcionario> buscarPorId(@PathVariable Integer id) {
        return funcionarioService.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Funcionario> criar(@RequestBody Funcionario obj) {
        Funcionario novo = funcionarioService.save(obj);
        auditService.registrarCriacao("FUNCIONARIO", novo.getId(), novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Funcionario> atualizar(@PathVariable Integer id, @RequestBody Funcionario detalhes) {
        Funcionario antes = funcionarioService.findById(id).orElse(null);
        Funcionario atualizado = funcionarioService.update(id, detalhes);
        if (atualizado == null) return ResponseEntity.notFound().build();
        auditService.registrarEdicao("FUNCIONARIO", id, antes, atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        Funcionario antes = funcionarioService.findById(id).orElse(null);
        funcionarioService.delete(id);
        auditService.registrarExclusao("FUNCIONARIO", id, antes);
        return ResponseEntity.noContent().build();
    }
}
