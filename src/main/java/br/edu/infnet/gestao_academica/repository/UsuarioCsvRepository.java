package br.edu.infnet.gestao_academica.repository;

import br.edu.infnet.gestao_academica.model.Aluno;
import br.edu.infnet.gestao_academica.model.Diretor;
import br.edu.infnet.gestao_academica.model.PerfilUsuario;
import br.edu.infnet.gestao_academica.model.Professor;
import br.edu.infnet.gestao_academica.model.Usuario;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioCsvRepository {

    private final Path csvFilePath;

    public UsuarioCsvRepository(@Value("${app.csv.usuarios-file}") String csvFile) {
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
                writer.write("ID,NOME,EMAIL,SENHA,PERFIL,DEPARTAMENTO,MATRICULA,CARGO");
                writer.newLine();
            }
        }
    }

    public List<Usuario> findAll() {
        try (var reader = Files.newBufferedReader(csvFilePath)) {
            return new CsvToBeanBuilder<CsvUsuarioRecord>(reader)
                    .withType(CsvUsuarioRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()
                    .stream()
                    .map(this::toUsuario)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Usuario> findById(Long id) {
        return findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public Optional<Usuario> findByEmail(String email) {
        return findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public synchronized Usuario save(Usuario usuario) {
        List<Usuario> usuarios = new ArrayList<>(findAll());

        if (usuario.getId() == null) {
            long nextId = usuarios.stream()
                    .mapToLong(Usuario::getId)
                    .max()
                    .orElse(0) + 1;
            usuario.setId(nextId);
            usuarios.add(usuario);
        } else {
            usuarios.removeIf(u -> u.getId().equals(usuario.getId()));
            usuarios.add(usuario);
        }

        writeAll(usuarios);
        return usuario;
    }

    public synchronized void deleteById(Long id) {
        List<Usuario> usuarios = new ArrayList<>(findAll());
        usuarios.removeIf(u -> u.getId().equals(id));
        writeAll(usuarios);
    }

    private void writeAll(List<Usuario> usuarios) {
        try (var writer = Files.newBufferedWriter(csvFilePath)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<CsvUsuarioRecord>(writer)
                    .build();
            beanToCsv.write(usuarios.stream().map(this::toRecord).toList());
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao escrever arquivo CSV", e);
        }
    }

    private Usuario toUsuario(CsvUsuarioRecord record) {
        PerfilUsuario perfil = parsePerfil(record.getPerfil());

        return switch (perfil) {
            case ALUNO -> new Aluno(record.getId(), record.getNome(), record.getEmail(), record.getSenha(),
                    record.getMatricula());
            case PROFESSOR -> new Professor(record.getId(), record.getNome(), record.getEmail(), record.getSenha(),
                    record.getDepartamento());
            case DIRETOR -> new Diretor(record.getId(), record.getNome(), record.getEmail(), record.getSenha(),
                    record.getCargo());
        };
    }

    private CsvUsuarioRecord toRecord(Usuario usuario) {
        CsvUsuarioRecord record = new CsvUsuarioRecord();
        record.setId(usuario.getId());
        record.setNome(usuario.getNome());
        record.setEmail(usuario.getEmail());
        record.setSenha(usuario.getSenha());
        record.setPerfil(usuario.getPerfil().name());

        if (usuario instanceof Professor professor) {
            record.setDepartamento(professor.getDepartamento());
        }

        if (usuario instanceof Aluno aluno) {
            record.setMatricula(aluno.getMatricula());
        }

        if (usuario instanceof Diretor diretor) {
            record.setCargo(diretor.getCargo());
        }

        return record;
    }

    private PerfilUsuario parsePerfil(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Perfil de usuário não informado no CSV");
        }

        if ("ADMIN".equalsIgnoreCase(valor)) {
            return PerfilUsuario.DIRETOR;
        }

        return PerfilUsuario.valueOf(valor.toUpperCase());
    }
}
