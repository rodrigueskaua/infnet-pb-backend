package br.edu.infnet.gestao_academica.repository;

import com.opencsv.bean.CsvBindByName;

public class CsvTurmaRecord {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "CODIGO")
    private String codigo;

    @CsvBindByName(column = "NOME_DISCIPLINA")
    private String nomeDisciplina;

    @CsvBindByName(column = "SEMESTRE")
    private String semestre;

    @CsvBindByName(column = "PROFESSOR_ID")
    private Long professorId;

    @CsvBindByName(column = "ALUNOS_IDS")
    private String alunosIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public String getAlunosIds() { return alunosIds; }
    public void setAlunosIds(String alunosIds) { this.alunosIds = alunosIds; }
}
