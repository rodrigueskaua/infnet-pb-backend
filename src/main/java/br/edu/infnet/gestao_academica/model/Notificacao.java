package br.edu.infnet.gestao_academica.model;

import java.time.LocalDateTime;

public class Notificacao {

    private Long id;
    private String mensagem;
    private LocalDateTime dataEnvio;
    private Boolean lida;
    private Usuario usuario;

    public Notificacao() {
    }

    public Notificacao(Long id, String mensagem, LocalDateTime dataEnvio, Boolean lida) {
        this.id = id;
        this.mensagem = mensagem;
        this.dataEnvio = dataEnvio;
        this.lida = lida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}