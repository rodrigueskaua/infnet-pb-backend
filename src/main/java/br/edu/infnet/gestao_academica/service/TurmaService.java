package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.TurmaRequestDTO;
import br.edu.infnet.gestao_academica.dto.TurmaResponseDTO;
import br.edu.infnet.gestao_academica.model.Turma;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TurmaService {

    private final TurmaCsvRepository repository;
    private final UsuarioCsvRepository usuarioRepository;

    public TurmaService(TurmaCsvRepository repository, UsuarioCsvRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public TurmaResponseDTO criar(TurmaRequestDTO dto) {
        Turma turma = new Turma();
        turma.setCodigo(dto.codigo());
        turma.setNomeDisciplina(dto.nomeDisciplina());
        turma.setSemestre(dto.semestre());
        turma.setProfessorId(dto.professorId());
        return toResponse(repository.save(turma));
    }

    public TurmaResponseDTO buscarPorId(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + id)));
    }

    public List<TurmaResponseDTO> listarTodas() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<TurmaResponseDTO> listarPorProfessor(Long professorId) {
        return repository.findByProfessorId(professorId).stream().map(this::toResponse).toList();
    }

    public List<TurmaResponseDTO> listarPorAluno(Long alunoId) {
        return repository.findByAlunoId(alunoId).stream().map(this::toResponse).toList();
    }

    public TurmaResponseDTO matricularAluno(Long turmaId, Long alunoId) {
        Turma turma = repository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));
        if (!turma.getAlunosIds().contains(alunoId)) {
            turma.getAlunosIds().add(alunoId);
            repository.save(turma);
        }
        return toResponse(turma);
    }

    public void deletar(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + id));
        repository.deleteById(id);
    }

    private TurmaResponseDTO toResponse(Turma t) {
        String nomeProfessor = t.getProfessorId() != null
                ? usuarioRepository.findById(t.getProfessorId()).map(u -> u.getNome()).orElse(null)
                : null;

        return new TurmaResponseDTO(
                t.getId(),
                t.getCodigo(),
                t.getNomeDisciplina(),
                t.getSemestre(),
                t.getProfessorId(),
                nomeProfessor,
                t.getAlunosIds()
        );
    }
}
