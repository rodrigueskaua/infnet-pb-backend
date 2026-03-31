package br.edu.infnet.gestao_academica.dto;

import java.util.List;

public record GrupoRequestDTO(
        Long turmaId,
        String nome,
        List<Long> integrantesIds
) {
}
