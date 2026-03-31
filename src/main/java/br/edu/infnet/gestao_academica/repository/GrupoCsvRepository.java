package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Grupo;
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
public class GrupoCsvRepository {

    private final Path csvFilePath;

    public GrupoCsvRepository(@Value("${app.csv.grupos-file}") String csvFile) {
        this.csvFilePath = Path.of(csvFile);
    }

    @PostConstruct
    public void init() throws IOException {
        Path parent = csvFilePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(csvFilePath)) {
            try (var writer = Files.newBufferedWriter(csvFilePath)) {
                writer.write("ID,TURMA_ID,NOME,LIDER_ID,ALUNOS_IDS,FINALIZADO");
                writer.newLine();
            }
        }
    }

    public List<Grupo> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvGrupoRecord>(reader)
                    .withType(CsvGrupoRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toGrupo)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Grupo> findById(Long id) {
        return findAll().stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public List<Grupo> findByTurmaId(Long turmaId) {
        return findAll().stream().filter(g -> turmaId.equals(g.getTurmaId())).toList();
    }

    public Optional<Grupo> findByTurmaIdAndAlunoId(Long turmaId, Long alunoId) {
        return findByTurmaId(turmaId).stream()
                .filter(g -> g.getAlunosIds() != null && g.getAlunosIds().contains(alunoId))
                .findFirst();
    }

    public synchronized Grupo save(Grupo grupo) {
        List<Grupo> grupos = new ArrayList<>(findAll());

        if (grupo.getId() == null) {
            long nextId = grupos.stream().mapToLong(Grupo::getId).max().orElse(0) + 1;
            grupo.setId(nextId);
            grupos.add(grupo);
        } else {
            grupos.removeIf(g -> g.getId().equals(grupo.getId()));
            grupos.add(grupo);
        }

        writeAll(grupos);
        return grupo;
    }

    private void writeAll(List<Grupo> grupos) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvGrupoRecord>(writer)
                    .build()
                    .write(grupos.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever CSV de grupos", e);
        }
    }

    private Grupo toGrupo(CsvGrupoRecord record) {
        Grupo grupo = new Grupo();
        grupo.setId(record.getId());
        grupo.setTurmaId(record.getTurmaId());
        grupo.setNome(record.getNome());
        grupo.setLiderId(record.getLiderId());
        grupo.setFinalizado(record.getFinalizado() != null && record.getFinalizado());

        if (record.getAlunosIds() != null && !record.getAlunosIds().isBlank()) {
            grupo.setAlunosIds(Arrays.stream(record.getAlunosIds().split(";"))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList()));
        }

        return grupo;
    }

    private CsvGrupoRecord toRecord(Grupo grupo) {
        CsvGrupoRecord record = new CsvGrupoRecord();
        record.setId(grupo.getId());
        record.setTurmaId(grupo.getTurmaId());
        record.setNome(grupo.getNome());
        record.setLiderId(grupo.getLiderId());
        record.setFinalizado(grupo.getFinalizado() != null && grupo.getFinalizado());

        if (grupo.getAlunosIds() != null && !grupo.getAlunosIds().isEmpty()) {
            record.setAlunosIds(grupo.getAlunosIds().stream().map(String::valueOf).collect(Collectors.joining(";")));
        }

        return record;
    }
}
