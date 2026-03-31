package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.NotificacaoResponseDTO;
import br.edu.infnet.gestao_academica.model.Notificacao;
import br.edu.infnet.gestao_academica.model.Turma;
import br.edu.infnet.gestao_academica.repository.NotificacaoCsvRepository;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class NotificacaoService {

    private final NotificacaoCsvRepository repository;
    private final TurmaCsvRepository turmaRepository;

    public NotificacaoService(NotificacaoCsvRepository repository,
                              TurmaCsvRepository turmaRepository) {
        this.repository = repository;
        this.turmaRepository = turmaRepository;
    }

    public void notificarUsuario(Long usuarioId, String mensagem) {
        Notificacao n = new Notificacao();
        n.setUsuarioId(usuarioId);
        n.setMensagem(mensagem);
        n.setDataEnvio(LocalDateTime.now());
        n.setLida(false);
        repository.save(n);
    }

    public List<NotificacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    public NotificacaoResponseDTO buscarPorId(Long id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada: " + id));
        return toResponse(n);
    }

    public NotificacaoResponseDTO marcarComoLida(Long id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada: " + id));
        n.setLida(true);
        repository.save(n);
        return toResponse(n);
    }

    public int notificarTurma(Long turmaId, String mensagem, Long remetenteId, String perfilRemetente) {
        if (turmaId == null) {
            throw new IllegalArgumentException("Turma é obrigatória.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem é obrigatória.");
        }

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));

        if ("PROFESSOR".equalsIgnoreCase(perfilRemetente)
                && !Objects.equals(turma.getProfessorId(), remetenteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Professor só pode notificar alunos das próprias turmas.");
        }

        if (turma.getAlunosIds() == null || turma.getAlunosIds().isEmpty()) {
            return 0;
        }

        turma.getAlunosIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(alunoId -> notificarUsuario(alunoId, mensagem.trim()));

        return (int) turma.getAlunosIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private NotificacaoResponseDTO toResponse(Notificacao n) {
        return new NotificacaoResponseDTO(
                n.getId(),
                n.getUsuarioId(),
                n.getMensagem(),
                n.getDataEnvio() != null ? n.getDataEnvio().toString() : null,
                n.getLida()
        );
    }
}
