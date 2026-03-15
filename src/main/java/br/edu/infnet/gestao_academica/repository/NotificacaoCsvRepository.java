package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Notificacao;
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
public class NotificacaoCsvRepository {

    private final Path csvFilePath;

    public NotificacaoCsvRepository(@Value("${app.csv.notificacoes-file}") String csvFile) {
        this.csvFilePath = Path.of(csvFile);
    }

    @PostConstruct
    public void init() throws IOException {
        Path parent = csvFilePath.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (!Files.exists(csvFilePath)) {
            try (var writer = Files.newBufferedWriter(csvFilePath)) {
                writer.write("ID,USUARIO_ID,MENSAGEM,DATA_ENVIO,LIDA");
                writer.newLine();
            }
        }
    }

    public List<Notificacao> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvNotificacaoRecord>(reader)
                    .withType(CsvNotificacaoRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toNotificacao)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Notificacao> findById(Long id) {
        return findAll().stream().filter(n -> n.getId().equals(id)).findFirst();
    }

    public List<Notificacao> findByUsuarioId(Long usuarioId) {
        return findAll().stream().filter(n -> usuarioId.equals(n.getUsuarioId())).toList();
    }

    public synchronized Notificacao save(Notificacao notificacao) {
        List<Notificacao> lista = new ArrayList<>(findAll());
        if (notificacao.getId() == null) {
            long nextId = lista.stream().mapToLong(Notificacao::getId).max().orElse(0) + 1;
            notificacao.setId(nextId);
            lista.add(notificacao);
        } else {
            lista.removeIf(n -> n.getId().equals(notificacao.getId()));
            lista.add(notificacao);
        }
        writeAll(lista);
        return notificacao;
    }

    private void writeAll(List<Notificacao> lista) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvNotificacaoRecord>(writer)
                    .build()
                    .write(lista.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever CSV de notificações", e);
        }
    }

    private Notificacao toNotificacao(CsvNotificacaoRecord r) {
        Notificacao n = new Notificacao();
        n.setId(r.getId());
        n.setUsuarioId(r.getUsuarioId());
        n.setMensagem(r.getMensagem());
        n.setDataEnvio(r.getDataEnvio() != null ? LocalDateTime.parse(r.getDataEnvio()) : null);
        n.setLida(r.getLida() != null && r.getLida());
        return n;
    }

    private CsvNotificacaoRecord toRecord(Notificacao n) {
        CsvNotificacaoRecord r = new CsvNotificacaoRecord();
        r.setId(n.getId());
        r.setUsuarioId(n.getUsuarioId());
        r.setMensagem(n.getMensagem());
        r.setDataEnvio(n.getDataEnvio() != null ? n.getDataEnvio().toString() : null);
        r.setLida(n.getLida());
        return r;
    }
}
