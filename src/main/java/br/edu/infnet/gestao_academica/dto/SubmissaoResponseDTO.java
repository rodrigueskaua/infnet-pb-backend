package br.edu.infnet.gestao_academica.dto;

public record SubmissaoResponseDTO(
        Long id,
        Long atividadeId,
        Long alunoId,
        String arquivoResposta,
        String dataEntrega,
        Double nota,
        String feedback
) {}
