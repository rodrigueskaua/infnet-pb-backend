package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Submissao;
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
public class SubmissaoCsvRepository {

    private final Path csvFilePath;

    public SubmissaoCsvRepository(@Value("${app.csv.submissoes-file}") String csvFile) {
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
                writer.write("ID,ATIVIDADE_ID,ALUNO_ID,ARQUIVO_RESPOSTA,DATA_ENTREGA,NOTA,FEEDBACK");
                writer.newLine();
            }
        }
    }

    public List<Submissao> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvSubmissaoRecord>(reader)
                    .withType(CsvSubmissaoRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toSubmissao)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Submissao> findById(Long id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public List<Submissao> findByAtividadeId(Long atividadeId) {
        return findAll().stream().filter(s -> atividadeId.equals(s.getAtividadeId())).toList();
    }

    public List<Submissao> findByAlunoId(Long alunoId) {
        return findAll().stream().filter(s -> alunoId.equals(s.getAlunoId())).toList();
    }

    public synchronized Submissao save(Submissao submissao) {
        List<Submissao> submissoes = new ArrayList<>(findAll());

        if (submissao.getId() == null) {
            long nextId = submissoes.stream().mapToLong(Submissao::getId).max().orElse(0) + 1;
            submissao.setId(nextId);
            submissoes.add(submissao);
        } else {
            submissoes.removeIf(s -> s.getId().equals(submissao.getId()));
            submissoes.add(submissao);
        }

        writeAll(submissoes);
        return submissao;
    }

    private void writeAll(List<Submissao> submissoes) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            new StatefulBeanToCsvBuilder<CsvSubmissaoRecord>(writer)
                    .build()
                    .write(submissoes.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever arquivo CSV de submissões", e);
        }
    }

    private Submissao toSubmissao(CsvSubmissaoRecord record) {
        Submissao s = new Submissao();
        s.setId(record.getId());
        s.setAtividadeId(record.getAtividadeId());
        s.setAlunoId(record.getAlunoId());
        s.setArquivoResposta(record.getArquivoResposta());
        s.setDataEntrega(record.getDataEntrega() != null ? LocalDateTime.parse(record.getDataEntrega()) : null);
        s.setNota(record.getNota());
        s.setFeedback(record.getFeedback());
        return s;
    }

    private CsvSubmissaoRecord toRecord(Submissao s) {
        CsvSubmissaoRecord record = new CsvSubmissaoRecord();
        record.setId(s.getId());
        record.setAtividadeId(s.getAtividadeId());
        record.setAlunoId(s.getAlunoId());
        record.setArquivoResposta(s.getArquivoResposta());
        record.setDataEntrega(s.getDataEntrega() != null ? s.getDataEntrega().toString() : null);
        record.setNota(s.getNota());
        record.setFeedback(s.getFeedback());
        return record;
    }
}
