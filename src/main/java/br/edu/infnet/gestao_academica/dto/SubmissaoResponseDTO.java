package br.edu.infnet.gestao_academica.dto;

public record SubmissaoResponseDTO(
        Long id,
        Long atividadeId,
        String tituloAtividade,
        Long alunoId,
        String nomeAluno,
        String arquivoResposta,
        String dataEntrega,
        Double nota,
        String feedback
) {}
