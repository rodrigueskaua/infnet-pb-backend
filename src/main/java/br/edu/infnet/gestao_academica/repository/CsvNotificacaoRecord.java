package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvNotificacaoRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "USUARIO_ID")
    private Long usuarioId;

    @CsvBindByName(column = "MENSAGEM")
    private String mensagem;

    @CsvBindByName(column = "DATA_ENVIO")
    private String dataEnvio;

    @CsvBindByName(column = "LIDA")
    private Boolean lida;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(String dataEnvio) { this.dataEnvio = dataEnvio; }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }
}
