package br.edu.infnet.gestao_academica.model;

public class Diretor extends Usuario {

    private String cargo;

    public Diretor() {
        setPerfil(PerfilUsuario.DIRETOR);
    }

    public Diretor(Long id, String nome, String email, String senha, String cargo) {
        super(id, nome, email, senha, PerfilUsuario.DIRETOR);
        this.cargo = cargo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}