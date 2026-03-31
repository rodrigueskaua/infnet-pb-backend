package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Atividade;
import br.edu.infnet.gestao_academica.model.StatusAtividade;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AtividadeCsvRepository {

    private final Path csvFilePath;

    public AtividadeCsvRepository(@Value("${app.csv.atividades-file}") String csvFile) {
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
                writer.write("ID,TITULO,DESCRICAO,DATA_PRAZO,STATUS,ARQUIVO_APOIO,EM_GRUPO,TURMA_ID,PROFESSOR_ID");
                writer.newLine();
            }
        }
    }

    public List<Atividade> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvAtividadeRecord>(reader)
                    .withType(CsvAtividadeRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toAtividade)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Atividade> findById(Long id) {
        return findAll().stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public List<Atividade> findByTurmaId(Long turmaId) {
        return findAll().stream().filter(a -> turmaId.equals(a.getTurmaId())).toList();
    }

    public List<Atividade> findByProfessorId(Long professorId) {
        return findAll().stream().filter(a -> professorId.equals(a.getProfessorId())).toList();
    }

    public synchronized Atividade save(Atividade atividade) {
        List<Atividade> atividades = new ArrayList<>(findAll());

        if (atividade.getId() == null) {
            long nextId = atividades.stream().mapToLong(Atividade::getId).max().orElse(0) + 1;
            atividade.setId(nextId);
            atividades.add(atividade);
        } else {
            atividades.removeIf(a -> a.getId().equals(atividade.getId()));
            atividades.add(atividade);
        }

        writeAll(atividades);
        return atividade;
    }

    public synchronized void deleteById(Long id) {
        List<Atividade> atividades = new ArrayList<>(findAll());
        atividades.removeIf(a -> a.getId().equals(id));
        writeAll(atividades);
    }

    private void writeAll(List<Atividade> atividades) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvAtividadeRecord>(writer)
                    .build()
                    .write(atividades.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever arquivo CSV de atividades", e);
        }
    }

    private Atividade toAtividade(CsvAtividadeRecord record) {
        Atividade a = new Atividade();
        a.setId(record.getId());
        a.setTitulo(record.getTitulo());
        a.setDescricao(record.getDescricao());
        a.setDataPrazo(record.getDataPrazo() != null ? LocalDateTime.parse(record.getDataPrazo()) : null);
        a.setStatus(record.getStatus() != null ? StatusAtividade.valueOf(record.getStatus()) : StatusAtividade.PUBLICADA);
        a.setArquivoApoio(record.getArquivoApoio());
        a.setEmGrupo(record.getEmGrupo() != null && record.getEmGrupo());
        a.setTurmaId(record.getTurmaId());
        a.setProfessorId(record.getProfessorId());
        return a;
    }

    private CsvAtividadeRecord toRecord(Atividade a) {
        CsvAtividadeRecord record = new CsvAtividadeRecord();
        record.setId(a.getId());
        record.setTitulo(a.getTitulo());
        record.setDescricao(a.getDescricao());
        record.setDataPrazo(a.getDataPrazo() != null ? a.getDataPrazo().toString() : null);
        record.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        record.setArquivoApoio(a.getArquivoApoio());
        record.setEmGrupo(a.getEmGrupo() != null && a.getEmGrupo());
        record.setTurmaId(a.getTurmaId());
        record.setProfessorId(a.getProfessorId());
        return record;
    }
}
