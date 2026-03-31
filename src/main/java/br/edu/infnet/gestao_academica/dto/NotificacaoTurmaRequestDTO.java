package br.edu.infnet.gestao_academica.dto;

public record NotificacaoTurmaRequestDTO(
        Long turmaId,
        String mensagem
) {
}