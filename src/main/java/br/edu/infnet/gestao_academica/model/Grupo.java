package br.edu.infnet.gestao_academica.model;

import java.util.ArrayList;
import java.util.List;

public class Grupo {

    private Long id;
    private Long turmaId;
    private String nome;
    private Long liderId;
    private List<Long> alunosIds = new ArrayList<>();
    private Boolean finalizado = false;
    private List<Aluno> alunos = new ArrayList<>();
    private Aluno lider;

    public Grupo() {
    }

    public Grupo(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public Long getLiderId() {
        return liderId;
    }

    public void setLiderId(Long liderId) {
        this.liderId = liderId;
    }

    public List<Long> getAlunosIds() {
        return alunosIds;
    }

    public void setAlunosIds(List<Long> alunosIds) {
        this.alunosIds = alunosIds;
    }

    public Boolean getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(Boolean finalizado) {
        this.finalizado = finalizado;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }

    public void setAlunos(List<Aluno> alunos) {
        this.alunos = alunos;
    }

    public Aluno getLider() {
        return lider;
    }

    public void setLider(Aluno lider) {
        this.lider = lider;
    }
}