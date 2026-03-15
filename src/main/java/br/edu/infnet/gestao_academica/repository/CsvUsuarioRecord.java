package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvUsuarioRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "NOME")
    private String nome;

    @CsvBindByName(column = "EMAIL")
    private String email;

    @CsvBindByName(column = "SENHA")
    private String senha;

    @CsvBindByName(column = "PERFIL")
    private String perfil;

    @CsvBindByName(column = "DEPARTAMENTO")
    private String departamento;

    @CsvBindByName(column = "MATRICULA")
    private String matricula;

    @CsvBindByName(column = "CARGO")
    private String cargo;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}