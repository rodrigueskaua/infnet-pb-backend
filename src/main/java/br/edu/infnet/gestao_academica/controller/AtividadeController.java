package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.AtividadeRequestDTO;
import br.edu.infnet.gestao_academica.dto.AtividadeResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.AtividadeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atividades")
public class AtividadeController {

    private final AtividadeService service;

    public AtividadeController(AtividadeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AtividadeResponseDTO> criar(@RequestBody AtividadeRequestDTO dto,
                                                      HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "DIRETOR");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto, logado.id()));
    }

    @GetMapping
    public ResponseEntity<List<AtividadeResponseDTO>> listarTodas(HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "ALUNO", "DIRETOR");
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtividadeResponseDTO> buscarPorId(@PathVariable Long id,
                                                             HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "ALUNO", "DIRETOR");
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<AtividadeResponseDTO>> listarPorTurma(@PathVariable Long turmaId,
                                                                      @RequestParam(required = false) String status,
                                                                      HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "ALUNO", "DIRETOR");

        if ("ALUNO".equalsIgnoreCase(logado.perfil())) {
            return ResponseEntity.ok(service.listarPorTurmaComFiltroAluno(turmaId, logado.id(), status));
        }

        return ResponseEntity.ok(service.listarPorTurma(turmaId));
    }

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<AtividadeResponseDTO>> listarPorProfessor(@PathVariable Long professorId,
                                                                          HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(service.listarPorProfessor(professorId));
    }
}
