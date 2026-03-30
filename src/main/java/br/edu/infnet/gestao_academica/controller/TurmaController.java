package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.TurmaRequestDTO;
import br.edu.infnet.gestao_academica.dto.TurmaResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.TurmaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService service;

    public TurmaController(TurmaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TurmaResponseDTO> criar(@RequestBody TurmaRequestDTO dto,
                                                   HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<TurmaResponseDTO>> listarTodas(HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR", "ALUNO");
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<TurmaResponseDTO> buscarPorId(@PathVariable Long id,
                                                         HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR", "ALUNO");
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/professor/minhas")
    public ResponseEntity<List<TurmaResponseDTO>> listarMinhasTurmasProfessor(HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "PROFESSOR");
        return ResponseEntity.ok(service.listarPorProfessor(logado.id()));
    }

    @GetMapping("/professor/{professorId:\\d+}")
    public ResponseEntity<List<TurmaResponseDTO>> listarPorProfessor(@PathVariable Long professorId,
                                                                       HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR");
        if ("PROFESSOR".equalsIgnoreCase(logado.perfil()) && !logado.id().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return ResponseEntity.ok(service.listarPorProfessor(professorId));
    }

    @GetMapping("/aluno/{alunoId:\\d+}")
    public ResponseEntity<List<TurmaResponseDTO>> listarPorAluno(@PathVariable Long alunoId,
                                                                   HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR", "ALUNO");
        if ("ALUNO".equalsIgnoreCase(logado.perfil()) && !logado.id().equals(alunoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @PostMapping("/{turmaId:\\d+}/matricular/{alunoId:\\d+}")
    public ResponseEntity<TurmaResponseDTO> matricularAluno(@PathVariable Long turmaId,
                                                             @PathVariable Long alunoId,
                                                             HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR");
        return ResponseEntity.ok(service.matricularAluno(turmaId, alunoId));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR");
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
