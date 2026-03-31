package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvTopicoRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "TURMA_ID")
    private Long turmaId;

    @CsvBindByName(column = "TITULO")
    private String titulo;

    @CsvBindByName(column = "DATA_CRIACAO")
    private String dataCriacao;

    @CsvBindByName(column = "STATUS")
    private String status;

    @CsvBindByName(column = "AUTOR_ID")
    private Long autorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }
}
