package br.edu.infnet.gestao_academica.dto;

public record MensagemTopicoResponseDTO(
        Long id,
        Long topicoId,
        Long autorId,
        String autorNome,
        String perfilAutor,
        String mensagem,
        String dataEnvio
) {
}
