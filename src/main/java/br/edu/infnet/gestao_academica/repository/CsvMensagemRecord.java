package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvMensagemRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "TOPICO_ID")
    private Long topicoId;

    @CsvBindByName(column = "AUTOR_ID")
    private Long autorId;

    @CsvBindByName(column = "CONTEUDO")
    private String conteudo;

    @CsvBindByName(column = "DATA_ENVIO")
    private String dataEnvio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicoId() {
        return topicoId;
    }

    public void setTopicoId(Long topicoId) {
        this.topicoId = topicoId;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
