package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.AtividadeRequestDTO;
import br.edu.infnet.gestao_academica.dto.AtividadeResponseDTO;
import br.edu.infnet.gestao_academica.model.Atividade;
import br.edu.infnet.gestao_academica.model.StatusAtividade;
import br.edu.infnet.gestao_academica.repository.AtividadeCsvRepository;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AtividadeService {

    private final AtividadeCsvRepository repository;
    private final UsuarioCsvRepository usuarioRepository;
    private final TurmaCsvRepository turmaRepository;
    private final NotificacaoService notificacaoService;

    public AtividadeService(AtividadeCsvRepository repository,
                            UsuarioCsvRepository usuarioRepository,
                            TurmaCsvRepository turmaRepository,
                            NotificacaoService notificacaoService) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.notificacaoService = notificacaoService;
    }

    public AtividadeResponseDTO criar(AtividadeRequestDTO dto, Long professorId) {
        Atividade atividade = new Atividade();
        atividade.setTitulo(dto.titulo());
        atividade.setDescricao(dto.descricao());
        atividade.setDataPrazo(dto.dataPrazo() != null ? LocalDateTime.parse(dto.dataPrazo()) : null);
        atividade.setStatus(StatusAtividade.PUBLICADA);
        atividade.setArquivoApoio(dto.arquivoApoio());
        atividade.setTurmaId(dto.turmaId());
        atividade.setProfessorId(professorId);

        Atividade salva = repository.save(atividade);
        notificarAlunosDaTurma(salva);
        return toResponse(salva);
    }

    public AtividadeResponseDTO buscarPorId(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada: " + id)));
    }

    public List<AtividadeResponseDTO> listarPorTurma(Long turmaId) {
        return repository.findByTurmaId(turmaId).stream().map(this::toResponse).toList();
    }

    public List<AtividadeResponseDTO> listarTodas() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<AtividadeResponseDTO> listarPorProfessor(Long professorId) {
        return repository.findByProfessorId(professorId).stream().map(this::toResponse).toList();
    }

    public Atividade buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada: " + id));
    }

    public boolean isPrazoValido(Long atividadeId) {
        Atividade atividade = buscarEntidadePorId(atividadeId);
        return atividade.getDataPrazo() == null || LocalDateTime.now().isBefore(atividade.getDataPrazo());
    }

    private void notificarAlunosDaTurma(Atividade atividade) {
        if (atividade.getTurmaId() == null) {
            return;
        }

        turmaRepository.findById(atividade.getTurmaId()).ifPresent(turma -> {
            if (turma.getAlunosIds() == null || turma.getAlunosIds().isEmpty()) {
                return;
            }

            String nomeProfessor = atividade.getProfessorId() != null
                    ? usuarioRepository.findById(atividade.getProfessorId()).map(u -> u.getNome()).orElse("Professor")
                    : "Professor";

            String disciplina = turma.getNomeDisciplina() != null ? turma.getNomeDisciplina() : "sua turma";
            String mensagem = "Nova atividade publicada em \"" + disciplina + "\": \""
                    + atividade.getTitulo() + "\" (professor: " + nomeProfessor + ")";

            turma.getAlunosIds().forEach(alunoId -> notificacaoService.notificarUsuario(alunoId, mensagem));
        });
    }

    private AtividadeResponseDTO toResponse(Atividade a) {
        String nomeProfessor = a.getProfessorId() != null
                ? usuarioRepository.findById(a.getProfessorId()).map(u -> u.getNome()).orElse(null)
                : null;

        String nomeDisciplina = a.getTurmaId() != null
                ? turmaRepository.findById(a.getTurmaId()).map(t -> t.getNomeDisciplina()).orElse(null)
                : null;

        return new AtividadeResponseDTO(
                a.getId(),
                a.getTitulo(),
                a.getDescricao(),
                a.getDataPrazo() != null ? a.getDataPrazo().toString() : null,
                a.getStatus() != null ? a.getStatus().name() : null,
                a.getArquivoApoio(),
                a.getTurmaId(),
                nomeDisciplina,
                a.getProfessorId(),
                nomeProfessor
        );
    }
}
