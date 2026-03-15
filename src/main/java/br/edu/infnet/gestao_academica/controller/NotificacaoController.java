package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.NotificacaoResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.NotificacaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<NotificacaoResponseDTO>> minhas(HttpServletRequest request) {
        UsuarioResponseDTO usuario = AutorizacaoHelper.getUsuarioLogado(request);
        return ResponseEntity.ok(service.listarPorUsuario(usuario.id()));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacaoResponseDTO>> porUsuario(@PathVariable Long usuarioId,
                                                                    HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR");
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(@PathVariable Long id,
                                                                   HttpServletRequest request) {
        AutorizacaoHelper.getUsuarioLogado(request);
        return ResponseEntity.ok(service.marcarComoLida(id));
    }
}
