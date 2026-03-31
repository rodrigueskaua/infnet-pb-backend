package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvAtividadeRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "TITULO")
    private String titulo;

    @CsvBindByName(column = "DESCRICAO")
    private String descricao;

    @CsvBindByName(column = "DATA_PRAZO")
    private String dataPrazo;

    @CsvBindByName(column = "STATUS")
    private String status;

    @CsvBindByName(column = "ARQUIVO_APOIO")
    private String arquivoApoio;

    @CsvBindByName(column = "EM_GRUPO")
    private Boolean emGrupo;

    @CsvBindByName(column = "TURMA_ID")
    private Long turmaId;

    @CsvBindByName(column = "PROFESSOR_ID")
    private Long professorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getDataPrazo() { return dataPrazo; }
    public void setDataPrazo(String dataPrazo) { this.dataPrazo = dataPrazo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getArquivoApoio() { return arquivoApoio; }
    public void setArquivoApoio(String arquivoApoio) { this.arquivoApoio = arquivoApoio; }

    public Boolean getEmGrupo() { return emGrupo; }
    public void setEmGrupo(Boolean emGrupo) { this.emGrupo = emGrupo; }

    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
}
