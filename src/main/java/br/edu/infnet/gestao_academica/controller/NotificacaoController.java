package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.NotificacaoTurmaRequestDTO;
import br.edu.infnet.gestao_academica.dto.NotificacaoResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.NotificacaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
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
        UsuarioResponseDTO logado = AutorizacaoHelper.getUsuarioLogado(request);
        NotificacaoResponseDTO notificacao = service.buscarPorId(id);
        if (!logado.id().equals(notificacao.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return ResponseEntity.ok(service.marcarComoLida(id));
    }

    @PostMapping("/turma")
    public ResponseEntity<Map<String, Object>> notificarTurma(@RequestBody NotificacaoTurmaRequestDTO dto,
                                                               HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "DIRETOR");

        int totalEnviado = service.notificarTurma(dto.turmaId(), dto.mensagem(), logado.id(), logado.perfil());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "turmaId", dto.turmaId(),
                "mensagem", dto.mensagem(),
                "totalAlunosNotificados", totalEnviado
        ));
    }
}
