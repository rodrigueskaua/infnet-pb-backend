package br.edu.infnet.gestao_academica.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Topico {

    private Long id;
    private String titulo;
    private LocalDateTime dataCriacao;
    private StatusTopico status;
    private List<Mensagem> mensagens = new ArrayList<>();
    private Usuario autor;

    public Topico() {
    }

    public Topico(Long id, String titulo, LocalDateTime dataCriacao, StatusTopico status) {
        this.id = id;
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public StatusTopico getStatus() {
        return status;
    }

    public void setStatus(StatusTopico status) {
        this.status = status;
    }

    public List<Mensagem> getMensagens() {
        return mensagens;
    }

    public void setMensagens(List<Mensagem> mensagens) {
        this.mensagens = mensagens;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }
}