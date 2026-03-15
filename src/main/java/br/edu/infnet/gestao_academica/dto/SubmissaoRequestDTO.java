package br.edu.infnet.gestao_academica.dto;

public record SubmissaoRequestDTO(
        Long atividadeId,
        Long alunoId,
        String arquivoResposta
) {}
