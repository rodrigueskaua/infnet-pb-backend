package br.edu.infnet.gestao_academica.model;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {

    private String matricula;
    private List<Grupo> grupos = new ArrayList<>();
    private List<Turma> turmas = new ArrayList<>();
    private List<Notificacao> notificacoes = new ArrayList<>();

    public Aluno() {
        setPerfil(PerfilUsuario.ALUNO);
    }

    public Aluno(Long id, String nome, String email, String senha, String matricula) {
        super(id, nome, email, senha, PerfilUsuario.ALUNO);
        this.matricula = matricula;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public void setTurmas(List<Turma> turmas) {
        this.turmas = turmas;
    }

    public List<Notificacao> getNotificacoes() {
        return notificacoes;
    }

    public void setNotificacoes(List<Notificacao> notificacoes) {
        this.notificacoes = notificacoes;
    }
}