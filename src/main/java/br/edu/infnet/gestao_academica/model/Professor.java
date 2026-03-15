package br.edu.infnet.gestao_academica.model;

import java.util.ArrayList;
import java.util.List;

public class Professor extends Usuario {

    private String departamento;
    private List<Turma> turmas = new ArrayList<>();

    public Professor() {
        setPerfil(PerfilUsuario.PROFESSOR);
    }

    public Professor(Long id, String nome, String email, String senha, String departamento) {
        super(id, nome, email, senha, PerfilUsuario.PROFESSOR);
        this.departamento = departamento;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public void setTurmas(List<Turma> turmas) {
        this.turmas = turmas;
    }
}