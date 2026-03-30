package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.service.RelatorioGerencialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioGerencialService relatorioGerencialService;

    public RelatorioController(RelatorioGerencialService relatorioGerencialService) {
        this.relatorioGerencialService = relatorioGerencialService;
    }

    @GetMapping(value = "/turmas/{turmaId}/engajamento.csv", produces = "text/csv")
    public ResponseEntity<ByteArrayResource> exportarEngajamentoTurmaCsv(@PathVariable Long turmaId,
                                                                          HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR");

        byte[] conteudoCsv = relatorioGerencialService.gerarCsvEngajamentoPorTurma(turmaId);
        ByteArrayResource arquivo = new ByteArrayResource(conteudoCsv);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-engajamento-turma-" + turmaId + ".csv")
                .contentType(new MediaType("text", "csv"))
                .contentLength(conteudoCsv.length)
                .body(arquivo);
    }
}