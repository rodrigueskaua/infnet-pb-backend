package br.edu.infnet.gestao_academica.model;

import com.opencsv.bean.CsvBindByName;

public class Usuario {

    @CsvBindByName
    private Long id;

    @CsvBindByName
    private String nome;

    @CsvBindByName
    private String email;

    @CsvBindByName
    private String senha;

    @CsvBindByName
    private String papel;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String email, String senha, String papel) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
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

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }
}
