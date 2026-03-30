package br.edu.infnet.gestao_academica.dto;

import java.util.List;

public record TurmaResponseDTO(
        Long id,
        String codigo,
        String nomeDisciplina,
        String semestre,
        Long professorId,
        String nomeProfessor,
        List<Long> alunosIds
) {}
