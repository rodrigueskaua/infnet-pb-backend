package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvSubmissaoRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "ATIVIDADE_ID")
    private Long atividadeId;

    @CsvBindByName(column = "ALUNO_ID")
    private Long alunoId;

    @CsvBindByName(column = "ARQUIVO_RESPOSTA")
    private String arquivoResposta;

    @CsvBindByName(column = "DATA_ENTREGA")
    private String dataEntrega;

    @CsvBindByName(column = "NOTA")
    private Double nota;

    @CsvBindByName(column = "FEEDBACK")
    private String feedback;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAtividadeId() { return atividadeId; }
    public void setAtividadeId(Long atividadeId) { this.atividadeId = atividadeId; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public String getArquivoResposta() { return arquivoResposta; }
    public void setArquivoResposta(String arquivoResposta) { this.arquivoResposta = arquivoResposta; }

    public String getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(String dataEntrega) { this.dataEntrega = dataEntrega; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
