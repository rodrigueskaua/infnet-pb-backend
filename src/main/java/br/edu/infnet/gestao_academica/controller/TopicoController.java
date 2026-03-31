package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.MensagemTopicoRequestDTO;
import br.edu.infnet.gestao_academica.dto.MensagemTopicoResponseDTO;
import br.edu.infnet.gestao_academica.dto.TopicoRequestDTO;
import br.edu.infnet.gestao_academica.dto.TopicoTimelineResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.TopicoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topicos")
public class TopicoController {

    private final TopicoService service;

    public TopicoController(TopicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TopicoTimelineResponseDTO> criar(@RequestBody TopicoRequestDTO dto,
                                                            HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "PROFESSOR");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarTopico(logado.id(), dto.turmaId(), dto.titulo()));
    }

    @PostMapping("/{topicoId}/respostas")
    public ResponseEntity<MensagemTopicoResponseDTO> responder(@PathVariable Long topicoId,
                                                               @RequestBody MensagemTopicoRequestDTO dto,
                                                               HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "ALUNO");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.responderTopico(topicoId, logado.id(), dto.mensagem()));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<TopicoTimelineResponseDTO>> timelineGeral(HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "ALUNO", "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(service.timelineGeral());
    }

    @GetMapping("/turma/{turmaId}/timeline")
    public ResponseEntity<List<TopicoTimelineResponseDTO>> timelinePorTurma(@PathVariable Long turmaId,
                                                                             HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "ALUNO", "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(service.timelinePorTurma(turmaId));
    }
}
