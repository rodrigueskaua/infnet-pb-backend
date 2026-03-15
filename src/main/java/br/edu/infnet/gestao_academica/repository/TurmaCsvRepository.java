package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Turma;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TurmaCsvRepository {

    private final Path csvFilePath;

    public TurmaCsvRepository(@Value("${app.csv.turmas-file}") String csvFile) {
        this.csvFilePath = Path.of(csvFile);
    }

    @PostConstruct
    public void init() throws IOException {
        Path parent = csvFilePath.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (!Files.exists(csvFilePath)) {
            try (var writer = Files.newBufferedWriter(csvFilePath)) {
                writer.write("ID,CODIGO,NOME_DISCIPLINA,SEMESTRE,PROFESSOR_ID,ALUNOS_IDS");
                writer.newLine();
            }
        }
    }

    public List<Turma> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvTurmaRecord>(reader)
                    .withType(CsvTurmaRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toTurma)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Turma> findById(Long id) {
        return findAll().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<Turma> findByProfessorId(Long professorId) {
        return findAll().stream().filter(t -> professorId.equals(t.getProfessorId())).toList();
    }

    public List<Turma> findByAlunoId(Long alunoId) {
        return findAll().stream()
                .filter(t -> t.getAlunosIds().contains(alunoId))
                .toList();
    }

    public synchronized Turma save(Turma turma) {
        List<Turma> turmas = new ArrayList<>(findAll());
        if (turma.getId() == null) {
            long nextId = turmas.stream().mapToLong(Turma::getId).max().orElse(0) + 1;
            turma.setId(nextId);
            turmas.add(turma);
        } else {
            turmas.removeIf(t -> t.getId().equals(turma.getId()));
            turmas.add(turma);
        }
        writeAll(turmas);
        return turma;
    }

    public synchronized void deleteById(Long id) {
        List<Turma> turmas = new ArrayList<>(findAll());
        turmas.removeIf(t -> t.getId().equals(id));
        writeAll(turmas);
    }

    private void writeAll(List<Turma> turmas) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvTurmaRecord>(writer)
                    .build()
                    .write(turmas.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever CSV de turmas", e);
        }
    }

    private Turma toTurma(CsvTurmaRecord r) {
        Turma t = new Turma();
        t.setId(r.getId());
        t.setCodigo(r.getCodigo());
        t.setNomeDisciplina(r.getNomeDisciplina());
        t.setSemestre(r.getSemestre());
        t.setProfessorId(r.getProfessorId());
        if (r.getAlunosIds() != null && !r.getAlunosIds().isBlank()) {
            List<Long> ids = Arrays.stream(r.getAlunosIds().split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            t.setAlunosIds(ids);
        }
        return t;
    }

    private CsvTurmaRecord toRecord(Turma t) {
        CsvTurmaRecord r = new CsvTurmaRecord();
        r.setId(t.getId());
        r.setCodigo(t.getCodigo());
        r.setNomeDisciplina(t.getNomeDisciplina());
        r.setSemestre(t.getSemestre());
        r.setProfessorId(t.getProfessorId());
        if (t.getAlunosIds() != null && !t.getAlunosIds().isEmpty()) {
            r.setAlunosIds(t.getAlunosIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(";")));
        }
        return r;
    }
}
