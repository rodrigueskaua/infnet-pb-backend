package br.edu.infnet.gestao_academica.model;

import java.time.LocalDateTime;

public class Submissao {

    private Long id;
    private LocalDateTime dataEntrega;
    private String arquivoResposta;
    private Double nota;
    private String feedback;
    private Long atividadeId;
    private Long alunoId;
    private Atividade atividade;

    public Submissao() {
    }

    public Submissao(Long id, LocalDateTime dataEntrega, String arquivoResposta, Double nota, String feedback) {
        this.id = id;
        this.dataEntrega = dataEntrega;
        this.arquivoResposta = arquivoResposta;
        this.nota = nota;
        this.feedback = feedback;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getArquivoResposta() {
        return arquivoResposta;
    }

    public void setArquivoResposta(String arquivoResposta) {
        this.arquivoResposta = arquivoResposta;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Long getAtividadeId() {
        return atividadeId;
    }

    public void setAtividadeId(Long atividadeId) {
        this.atividadeId = atividadeId;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade atividade) {
        this.atividade = atividade;
    }
}