package br.edu.infnet.gestao_academica.dto;

public record NotificacaoTurmaResponseDTO(
        Long turmaId,
        String mensagem,
        int totalAlunosNotificados
) {}
