package br.edu.infnet.gestao_academica.dto;

import java.util.List;

public record TopicoTimelineResponseDTO(
        Long id,
        Long turmaId,
        String titulo,
        String dataCriacao,
        String status,
        Long autorId,
        String autorNome,
        String perfilAutor,
        List<MensagemTopicoResponseDTO> respostas
) {
}
