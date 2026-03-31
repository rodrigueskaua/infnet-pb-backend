package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.GrupoRequestDTO;
import br.edu.infnet.gestao_academica.dto.GrupoResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.GrupoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    private final GrupoService service;

    public GrupoController(GrupoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> criar(@RequestBody GrupoRequestDTO dto,
                                                   HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "ALUNO");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarGrupo(dto, logado.id()));
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<GrupoResponseDTO>> listarPorTurma(@PathVariable Long turmaId,
                                                                  HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "ALUNO", "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(service.listarPorTurma(turmaId, logado.id(), logado.perfil()));
    }

    @PatchMapping("/turma/{turmaId}/finalizar")
    public ResponseEntity<List<GrupoResponseDTO>> finalizar(@PathVariable Long turmaId,
                                                             HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(service.finalizarPorTurma(turmaId, logado.id(), logado.perfil()));
    }
}
