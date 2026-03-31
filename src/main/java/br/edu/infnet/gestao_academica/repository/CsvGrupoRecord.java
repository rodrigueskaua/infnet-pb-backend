package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvGrupoRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "TURMA_ID")
    private Long turmaId;

    @CsvBindByName(column = "NOME")
    private String nome;

    @CsvBindByName(column = "LIDER_ID")
    private Long liderId;

    @CsvBindByName(column = "ALUNOS_IDS")
    private String alunosIds;

    @CsvBindByName(column = "FINALIZADO")
    private Boolean finalizado;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getLiderId() {
        return liderId;
    }

    public void setLiderId(Long liderId) {
        this.liderId = liderId;
    }

    public String getAlunosIds() {
        return alunosIds;
    }

    public void setAlunosIds(String alunosIds) {
        this.alunosIds = alunosIds;
    }

    public Boolean getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(Boolean finalizado) {
        this.finalizado = finalizado;
    }
}
