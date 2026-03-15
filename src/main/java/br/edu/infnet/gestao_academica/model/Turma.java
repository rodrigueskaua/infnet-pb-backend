package br.edu.infnet.gestao_academica.model;

import java.util.ArrayList;
import java.util.List;

public class Turma {

    private Long id;
    private String codigo;
    private String nomeDisciplina;
    private String semestre;
    private Long professorId;
    private List<Long> alunosIds = new ArrayList<>();
    private Professor professor;
    private List<Aluno> alunosMatriculados = new ArrayList<>();
    private List<Atividade> atividades = new ArrayList<>();

    public Turma() {
    }

    public Turma(Long id, String codigo, String nomeDisciplina, String semestre) {
        this.id = id;
        this.codigo = codigo;
        this.nomeDisciplina = nomeDisciplina;
        this.semestre = semestre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public List<Long> getAlunosIds() {
        return alunosIds;
    }

    public void setAlunosIds(List<Long> alunosIds) {
        this.alunosIds = alunosIds;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public List<Aluno> getAlunosMatriculados() {
        return alunosMatriculados;
    }

    public void setAlunosMatriculados(List<Aluno> alunosMatriculados) {
        this.alunosMatriculados = alunosMatriculados;
    }

    public List<Atividade> getAtividades() {
        return atividades;
    }

    public void setAtividades(List<Atividade> atividades) {
        this.atividades = atividades;
    }
}