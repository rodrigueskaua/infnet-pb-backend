package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.dto.SubmissaoResponseDTO;
import br.edu.infnet.gestao_academica.service.StorageService;
import br.edu.infnet.gestao_academica.service.SubmissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestParam Long alunoId,
            @RequestParam MultipartFile arquivo) {
        String caminho = storageService.salvar(arquivo);
        SubmissaoResponseDTO resposta = submissaoService.submeter(atividadeId, alunoId, caminho);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(submissaoService.buscarPorId(id));
    }

    @GetMapping("/atividade/{atividadeId}")
    public ResponseEntity<List<SubmissaoResponseDTO>> listarPorAtividade(@PathVariable Long atividadeId) {
        return ResponseEntity.ok(submissaoService.listarPorAtividade(atividadeId));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<SubmissaoResponseDTO>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(submissaoService.listarPorAluno(alunoId));
    }
}
