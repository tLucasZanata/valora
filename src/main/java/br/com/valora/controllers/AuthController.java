package br.com.valora.controllers;

import br.com.valora.Service.AuditService;
import br.com.valora.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final AuditService      auditService;

    public AuthController(UsuarioRepository usuarioRepository, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.auditService      = auditService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, String>> perfil(Authentication auth) {
        return usuarioRepository.findByNome(auth.getName())
                .map(u -> {
                    // Registra login bem-sucedido
                    auditService.registrarLogin(u.getNome(), true);
                    return ResponseEntity.ok(Map.of(
                            "nome",  u.getNome(),
                            "role",  u.getRole(),
                            "email", u.getEmail() != null ? u.getEmail() : ""
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
