package br.com.valora.controllers;

import br.com.valora.model.ProdutoServico;
import br.com.valora.Service.ProdutoServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produto-servico")
public class ProdutoServicoController {

    private final ProdutoServicoService produtoServicoService;

    public ProdutoServicoController(ProdutoServicoService produtoServicoService) {
        this.produtoServicoService = produtoServicoService;
    }

    // Lista todos os vínculos produto-serviço
    @GetMapping
    public ResponseEntity<List<ProdutoServico>> listarTodos() {
        return ResponseEntity.ok(produtoServicoService.findAll());
    }

    // Lista os produtos de um registro de serviço específico
    @GetMapping("/registro/{registroId}")
    public ResponseEntity<List<ProdutoServico>> listarPorRegistro(@PathVariable Integer registroId) {
        return ResponseEntity.ok(produtoServicoService.findByRegistroServicoId(registroId));
    }

    // Busca por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoServico> buscarPorId(@PathVariable Integer id) {
        return produtoServicoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Cria um novo vínculo (movimento: produto → serviço)
    @PostMapping
    public ResponseEntity<ProdutoServico> criar(@RequestBody ProdutoServico produtoServico) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoServicoService.save(produtoServico));
    }

    // Atualiza a quantidade de um vínculo existente
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoServico> atualizar(@PathVariable Integer id,
                                                    @RequestBody ProdutoServico produtoServico) {
        ProdutoServico atualizado = produtoServicoService.update(id, produtoServico);
        return atualizado != null ? ResponseEntity.ok(atualizado) : ResponseEntity.notFound().build();
    }

    // Remove um produto de um serviço
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        produtoServicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}