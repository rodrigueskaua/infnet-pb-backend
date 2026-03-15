package br.edu.infnet.gestao_academica.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Atividade {

    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataPrazo;
    private StatusAtividade status;
    private String arquivoApoio;
    private Turma turma;
    private List<Submissao> submissoes = new ArrayList<>();

    public Atividade() {
    }

    public Atividade(Long id, String titulo, String descricao, LocalDateTime dataPrazo, StatusAtividade status,
                     String arquivoApoio) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataPrazo = dataPrazo;
        this.status = status;
        this.arquivoApoio = arquivoApoio;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDateTime dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public StatusAtividade getStatus() {
        return status;
    }

    public void setStatus(StatusAtividade status) {
        this.status = status;
    }

    public String getArquivoApoio() {
        return arquivoApoio;
    }

    public void setArquivoApoio(String arquivoApoio) {
        this.arquivoApoio = arquivoApoio;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public List<Submissao> getSubmissoes() {
        return submissoes;
    }

    public void setSubmissoes(List<Submissao> submissoes) {
        this.submissoes = submissoes;
    }
}