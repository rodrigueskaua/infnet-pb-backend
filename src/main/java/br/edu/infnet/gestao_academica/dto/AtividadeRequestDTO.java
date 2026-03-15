package br.edu.infnet.gestao_academica.dto;

public record AtividadeRequestDTO(
        String titulo,
        String descricao,
        String dataPrazo,
        String arquivoApoio,
        Long turmaId,
        Long professorId
) {}
