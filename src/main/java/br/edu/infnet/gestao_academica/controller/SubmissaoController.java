package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.dto.CorrecaoRequestDTO;
import br.edu.infnet.gestao_academica.dto.SubmissaoResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.StorageService;
import br.edu.infnet.gestao_academica.service.SubmissaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/submissoes")
public class SubmissaoController {

    private final SubmissaoService submissaoService;
    private final StorageService storageService;

    public SubmissaoController(SubmissaoService submissaoService, StorageService storageService) {
        this.submissaoService = submissaoService;
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<SubmissaoResponseDTO> submeter(
            @RequestParam Long atividadeId,
            @RequestParam MultipartFile arquivo,
            HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.exigirPerfil(request, "ALUNO");
        String caminho = storageService.salvar(arquivo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissaoService.submeter(atividadeId, logado.id(), caminho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissaoResponseDTO> buscarPorId(@PathVariable Long id,
                                                             HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.getUsuarioLogado(request);
        SubmissaoResponseDTO submissao = submissaoService.buscarPorId(id);

        if ("ALUNO".equalsIgnoreCase(logado.perfil()) && !logado.id().equals(submissao.alunoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return ResponseEntity.ok(submissao);
    }

    @GetMapping("/atividade/{atividadeId}")
    public ResponseEntity<List<SubmissaoResponseDTO>> listarPorAtividade(@PathVariable Long atividadeId,
                                                                          HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(submissaoService.listarPorAtividade(atividadeId));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<SubmissaoResponseDTO>> listarPorAluno(@PathVariable Long alunoId,
                                                                      HttpServletRequest request) {
        UsuarioResponseDTO logado = AutorizacaoHelper.getUsuarioLogado(request);
        if ("ALUNO".equalsIgnoreCase(logado.perfil()) && !logado.id().equals(alunoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return ResponseEntity.ok(submissaoService.listarPorAluno(alunoId));
    }

    @PatchMapping("/{id}/corrigir")
    public ResponseEntity<SubmissaoResponseDTO> corrigir(@PathVariable Long id,
                                                          @RequestBody CorrecaoRequestDTO dto,
                                                          HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "PROFESSOR", "DIRETOR");
        return ResponseEntity.ok(submissaoService.corrigir(id, dto.nota(), dto.feedback()));
    }
}
