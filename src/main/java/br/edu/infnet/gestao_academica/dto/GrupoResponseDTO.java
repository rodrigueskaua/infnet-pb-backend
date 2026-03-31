package br.edu.infnet.gestao_academica.dto;

import java.util.List;

public record GrupoResponseDTO(
        Long id,
        Long turmaId,
        String nome,
        Long liderId,
        String nomeLider,
        List<Long> alunosIds,
        Boolean finalizado
) {
}
