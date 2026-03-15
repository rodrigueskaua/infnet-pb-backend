package br.edu.infnet.gestao_academica.dto;

public record NotificacaoResponseDTO(
        Long id,
        Long usuarioId,
        String mensagem,
        String dataEnvio,
        Boolean lida
) {}
