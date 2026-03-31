package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Mensagem;
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

@Repository
public class MensagemCsvRepository {

    private final Path csvFilePath;

    public MensagemCsvRepository(@Value("${app.csv.mensagens-file}") String csvFile) {
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
                writer.write("ID,TOPICO_ID,AUTOR_ID,CONTEUDO,DATA_ENVIO");
                writer.newLine();
            }
        }
    }

    public List<Mensagem> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvMensagemRecord>(reader)
                    .withType(CsvMensagemRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toMensagem)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<Mensagem> findByTopicoId(Long topicoId) {
        return findAll().stream().filter(m -> topicoId.equals(m.getTopicoId())).toList();
    }

    public synchronized Mensagem save(Mensagem mensagem) {
        List<Mensagem> mensagens = new ArrayList<>(findAll());

        if (mensagem.getId() == null) {
            long nextId = mensagens.stream().mapToLong(Mensagem::getId).max().orElse(0) + 1;
            mensagem.setId(nextId);
            mensagens.add(mensagem);
        } else {
            mensagens.removeIf(m -> m.getId().equals(mensagem.getId()));
            mensagens.add(mensagem);
        }

        writeAll(mensagens);
        return mensagem;
    }

    private void writeAll(List<Mensagem> mensagens) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvMensagemRecord>(writer)
                    .build()
                    .write(mensagens.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever CSV de mensagens", e);
        }
    }

    private Mensagem toMensagem(CsvMensagemRecord record) {
        Mensagem mensagem = new Mensagem();
        mensagem.setId(record.getId());
        mensagem.setTopicoId(record.getTopicoId());
        mensagem.setAutorId(record.getAutorId());
        mensagem.setConteudo(record.getConteudo());
        mensagem.setDataEnvio(record.getDataEnvio() != null ? LocalDateTime.parse(record.getDataEnvio()) : null);
        return mensagem;
    }

    private CsvMensagemRecord toRecord(Mensagem mensagem) {
        CsvMensagemRecord record = new CsvMensagemRecord();
        record.setId(mensagem.getId());
        record.setTopicoId(mensagem.getTopicoId());
        record.setAutorId(mensagem.getAutorId());
        record.setConteudo(mensagem.getConteudo());
        record.setDataEnvio(mensagem.getDataEnvio() != null ? mensagem.getDataEnvio().toString() : null);
        return record;
    }
}
