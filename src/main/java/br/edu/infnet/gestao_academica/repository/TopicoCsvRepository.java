package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.StatusTopico;
import br.edu.infnet.gestao_academica.model.Topico;
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
public class TopicoCsvRepository {

    private final Path csvFilePath;

    public TopicoCsvRepository(@Value("${app.csv.topicos-file}") String csvFile) {
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
                writer.write("ID,TURMA_ID,TITULO,DATA_CRIACAO,STATUS,AUTOR_ID");
                writer.newLine();
            }
        }
    }

    public List<Topico> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvTopicoRecord>(reader)
                    .withType(CsvTopicoRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toTopico)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Topico> findById(Long id) {
        return findAll().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<Topico> findByTurmaId(Long turmaId) {
        return findAll().stream().filter(t -> turmaId.equals(t.getTurmaId())).toList();
    }

    public synchronized Topico save(Topico topico) {
        List<Topico> topicos = new ArrayList<>(findAll());
        if (topico.getId() == null) {
            long nextId = topicos.stream().mapToLong(Topico::getId).max().orElse(0) + 1;
            topico.setId(nextId);
            topicos.add(topico);
        } else {
            topicos.removeIf(t -> t.getId().equals(topico.getId()));
            topicos.add(topico);
        }

        writeAll(topicos);
        return topico;
    }

    private void writeAll(List<Topico> topicos) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvTopicoRecord>(writer)
                    .build()
                    .write(topicos.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever CSV de topicos", e);
        }
    }

    private Topico toTopico(CsvTopicoRecord record) {
        Topico topico = new Topico();
        topico.setId(record.getId());
        topico.setTurmaId(record.getTurmaId());
        topico.setTitulo(record.getTitulo());
        topico.setDataCriacao(record.getDataCriacao() != null ? LocalDateTime.parse(record.getDataCriacao()) : null);
        topico.setStatus(record.getStatus() != null ? StatusTopico.valueOf(record.getStatus()) : StatusTopico.ABERTO);
        topico.setAutorId(record.getAutorId());
        return topico;
    }

    private CsvTopicoRecord toRecord(Topico topico) {
        CsvTopicoRecord record = new CsvTopicoRecord();
        record.setId(topico.getId());
        record.setTurmaId(topico.getTurmaId());
        record.setTitulo(topico.getTitulo());
        record.setDataCriacao(topico.getDataCriacao() != null ? topico.getDataCriacao().toString() : null);
        record.setStatus(topico.getStatus() != null ? topico.getStatus().name() : null);
        record.setAutorId(topico.getAutorId());
        return record;
    }
}
