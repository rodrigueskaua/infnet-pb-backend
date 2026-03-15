package br.edu.infnet.gestao_academica.dto;

public record TurmaRequestDTO(
        String codigo,
        String nomeDisciplina,
        String semestre,
        Long professorId
) {}
