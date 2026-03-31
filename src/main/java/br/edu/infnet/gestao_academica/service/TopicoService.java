package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.MensagemTopicoResponseDTO;
import br.edu.infnet.gestao_academica.dto.TopicoTimelineResponseDTO;
import br.edu.infnet.gestao_academica.model.Mensagem;
import br.edu.infnet.gestao_academica.model.StatusTopico;
import br.edu.infnet.gestao_academica.model.Topico;
import br.edu.infnet.gestao_academica.model.Turma;
import br.edu.infnet.gestao_academica.repository.MensagemCsvRepository;
import br.edu.infnet.gestao_academica.repository.TopicoCsvRepository;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TopicoService {

    private final TopicoCsvRepository topicoRepository;
    private final MensagemCsvRepository mensagemRepository;
    private final TurmaCsvRepository turmaRepository;
    private final UsuarioCsvRepository usuarioRepository;

    public TopicoService(TopicoCsvRepository topicoRepository,
                         MensagemCsvRepository mensagemRepository,
                         TurmaCsvRepository turmaRepository,
                         UsuarioCsvRepository usuarioRepository) {
        this.topicoRepository = topicoRepository;
        this.mensagemRepository = mensagemRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public TopicoTimelineResponseDTO criarTopico(Long professorId, Long turmaId, String titulo) {
        if (turmaId == null) {
            throw new IllegalArgumentException("Turma é obrigatória.");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório.");
        }

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));

        if (!Objects.equals(turma.getProfessorId(), professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Professor só pode criar tópico na própria turma.");
        }

        Topico topico = new Topico();
        topico.setTurmaId(turmaId);
        topico.setTitulo(titulo.trim());
        topico.setDataCriacao(LocalDateTime.now());
        topico.setStatus(StatusTopico.ABERTO);
        topico.setAutorId(professorId);

        return toTimeline(topicoRepository.save(topico));
    }

    public MensagemTopicoResponseDTO responderTopico(Long topicoId, Long alunoId, String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem é obrigatória.");
        }

        Topico topico = topicoRepository.findById(topicoId)
                .orElseThrow(() -> new IllegalArgumentException("Tópico não encontrado: " + topicoId));

        if (topico.getStatus() != StatusTopico.ABERTO) {
            throw new IllegalStateException("Não é permitido responder tópico com status " + topico.getStatus());
        }

        Turma turma = turmaRepository.findById(topico.getTurmaId())
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + topico.getTurmaId()));

        if (turma.getAlunosIds() == null || !turma.getAlunosIds().contains(alunoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Aluno só pode responder tópico da turma em que está matriculado.");
        }

        Mensagem resposta = new Mensagem();
        resposta.setTopicoId(topicoId);
        resposta.setAutorId(alunoId);
        resposta.setConteudo(mensagem.trim());
        resposta.setDataEnvio(LocalDateTime.now());

        return toMensagemResponse(mensagemRepository.save(resposta));
    }

    public List<TopicoTimelineResponseDTO> timelineGeral() {
        return topicoRepository.findAll().stream()
                .sorted(Comparator.comparing(Topico::getDataCriacao, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .map(this::toTimeline)
                .toList();
    }

    public List<TopicoTimelineResponseDTO> timelinePorTurma(Long turmaId) {
        turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));

        return topicoRepository.findByTurmaId(turmaId).stream()
                .sorted(Comparator.comparing(Topico::getDataCriacao, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .map(this::toTimeline)
                .toList();
    }

    private TopicoTimelineResponseDTO toTimeline(Topico topico) {
        String autorNome = topico.getAutorId() != null
                ? usuarioRepository.findById(topico.getAutorId()).map(u -> u.getNome()).orElse("Usuário não encontrado")
                : null;

        String perfilAutor = topico.getAutorId() != null
                ? usuarioRepository.findById(topico.getAutorId()).map(u -> u.getPerfil().name()).orElse(null)
                : null;

        List<MensagemTopicoResponseDTO> respostas = mensagemRepository.findByTopicoId(topico.getId()).stream()
                .sorted(Comparator.comparing(Mensagem::getDataEnvio, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(this::toMensagemResponse)
                .toList();

        return new TopicoTimelineResponseDTO(
                topico.getId(),
                topico.getTurmaId(),
                topico.getTitulo(),
                topico.getDataCriacao() != null ? topico.getDataCriacao().toString() : null,
                topico.getStatus() != null ? topico.getStatus().name() : null,
                topico.getAutorId(),
                autorNome,
                perfilAutor,
                respostas
        );
    }

    private MensagemTopicoResponseDTO toMensagemResponse(Mensagem mensagem) {
        String autorNome = mensagem.getAutorId() != null
                ? usuarioRepository.findById(mensagem.getAutorId()).map(u -> u.getNome()).orElse("Usuário não encontrado")
                : null;

        String perfilAutor = mensagem.getAutorId() != null
                ? usuarioRepository.findById(mensagem.getAutorId()).map(u -> u.getPerfil().name()).orElse(null)
                : null;

        return new MensagemTopicoResponseDTO(
                mensagem.getId(),
                mensagem.getTopicoId(),
                mensagem.getAutorId(),
                autorNome,
                perfilAutor,
                mensagem.getConteudo(),
                mensagem.getDataEnvio() != null ? mensagem.getDataEnvio().toString() : null
        );
    }
}
