package br.edu.infnet.gestao_academica.config;

import br.edu.infnet.gestao_academica.model.Aluno;
import br.edu.infnet.gestao_academica.model.Diretor;
import br.edu.infnet.gestao_academica.model.Professor;
import br.edu.infnet.gestao_academica.model.Turma;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final UsuarioCsvRepository usuarioRepository;
    private final TurmaCsvRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioCsvRepository usuarioRepository, TurmaCsvRepository turmaRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void carregar() {
        if (!usuarioRepository.findAll().isEmpty()) return;

        String senhaHash = passwordEncoder.encode("123456");

        Professor prof1 = new Professor(null, "Ana Lima", "ana@professor.com", senhaHash, "Engenharia de Software");
        Professor prof2 = new Professor(null, "Bruno Costa", "bruno@professor.com", senhaHash, "Análise e Desenvolvimento de Sistemas");
        Aluno aluno1 = new Aluno(null, "Carlos Silva", "carlos@aluno.com", senhaHash, "ADS-2024-001");
        Aluno aluno2 = new Aluno(null, "Diana Souza", "diana@aluno.com", senhaHash, "ADS-2024-002");
        Aluno aluno3 = new Aluno(null, "Eduardo Reis", "eduardo@aluno.com", senhaHash, "ADS-2024-003");
        Diretor dir1 = new Diretor(null, "Marcos Diretor", "marcos@diretor.com", senhaHash, "Diretor Acadêmico");

        usuarioRepository.save(prof1);
        usuarioRepository.save(prof2);
        usuarioRepository.save(aluno1);
        usuarioRepository.save(aluno2);
        usuarioRepository.save(aluno3);
        usuarioRepository.save(dir1);

        if (!turmaRepository.findAll().isEmpty()) return;

        Turma turma1 = new Turma();
        turma1.setCodigo("ES-2026.1");
        turma1.setNomeDisciplina("Engenharia de Software");
        turma1.setSemestre("2026.1");
        turma1.setProfessorId(prof1.getId());
        turma1.getAlunosIds().add(aluno1.getId());
        turma1.getAlunosIds().add(aluno2.getId());
        turmaRepository.save(turma1);

        Turma turma2 = new Turma();
        turma2.setCodigo("ADS-2026.1");
        turma2.setNomeDisciplina("Análise e Desenvolvimento de Sistemas");
        turma2.setSemestre("2026.1");
        turma2.setProfessorId(prof2.getId());
        turma2.getAlunosIds().add(aluno2.getId());
        turma2.getAlunosIds().add(aluno3.getId());
        turmaRepository.save(turma2);
    }
}
