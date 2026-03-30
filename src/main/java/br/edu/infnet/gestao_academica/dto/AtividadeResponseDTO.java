package br.edu.infnet.gestao_academica.dto;

public record AtividadeResponseDTO(
        Long id,
        String titulo,
        String descricao,
        String dataPrazo,
        String status,
        String arquivoApoio,
        Long turmaId,
        String nomeDisciplina,
        Long professorId,
        String nomeProfessor
) {}
