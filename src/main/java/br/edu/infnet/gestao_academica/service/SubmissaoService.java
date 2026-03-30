package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.SubmissaoResponseDTO;
import br.edu.infnet.gestao_academica.model.Submissao;
import br.edu.infnet.gestao_academica.repository.SubmissaoCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissaoService {

    private final SubmissaoCsvRepository repository;
    private final AtividadeService atividadeService;
    private final NotificacaoService notificacaoService;
    private final UsuarioCsvRepository usuarioRepository;

    public SubmissaoService(SubmissaoCsvRepository repository,
                            AtividadeService atividadeService,
                            NotificacaoService notificacaoService,
                            UsuarioCsvRepository usuarioRepository) {
        this.repository = repository;
        this.atividadeService = atividadeService;
        this.notificacaoService = notificacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    public SubmissaoResponseDTO submeter(Long atividadeId, Long alunoId, String caminhoArquivo) {
        if (!atividadeService.isPrazoValido(atividadeId)) {
            throw new IllegalStateException("Prazo da atividade encerrado.");
        }

        validarReenvioComSubmissaoJaCorrigida(atividadeId, alunoId);

        Submissao submissao = new Submissao();
        submissao.setAtividadeId(atividadeId);
        submissao.setAlunoId(alunoId);
        submissao.setArquivoResposta(caminhoArquivo);
        submissao.setDataEntrega(LocalDateTime.now());

        Submissao salva = repository.save(submissao);

        var atividade = atividadeService.buscarEntidadePorId(atividadeId);
        String nomeAluno = usuarioRepository.findById(alunoId).map(u -> u.getNome()).orElse("Aluno");

        if (atividade.getProfessorId() != null) {
            notificacaoService.notificarUsuario(
                    atividade.getProfessorId(),
                    nomeAluno + " submeteu resposta para \"" + atividade.getTitulo() + "\""
            );
        }

        return toResponse(salva);
    }

    private void validarReenvioComSubmissaoJaCorrigida(Long atividadeId, Long alunoId) {
        boolean possuiSubmissaoCorrigida = repository.findByAtividadeId(atividadeId).stream()
                .filter(s -> alunoId.equals(s.getAlunoId()))
                .anyMatch(s -> s.getNota() != null || (s.getFeedback() != null && !s.getFeedback().isBlank()));

        if (possuiSubmissaoCorrigida) {
            throw new IllegalStateException("Submissão já corrigida. Não é permitido reenviar esta atividade.");
        }
    }

    public List<SubmissaoResponseDTO> listarPorAtividade(Long atividadeId) {
        return repository.findByAtividadeId(atividadeId).stream().map(this::toResponse).toList();
    }

    public List<SubmissaoResponseDTO> listarPorAluno(Long alunoId) {
        return repository.findByAlunoId(alunoId).stream().map(this::toResponse).toList();
    }

    public SubmissaoResponseDTO buscarPorId(Long id) {
        Submissao s = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submissão não encontrada: " + id));
        return toResponse(s);
    }

    public SubmissaoResponseDTO corrigir(Long id, Double nota, String feedback) {
        Submissao s = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submissão não encontrada: " + id));
        s.setNota(nota);
        s.setFeedback(feedback);

        Submissao salva = repository.save(s);
        notificarAlunoCorrecao(salva, nota, feedback);
        return toResponse(salva);
    }

        private void notificarAlunoCorrecao(Submissao submissao, Double nota, String feedback) {
        if (submissao.getAlunoId() == null) {
            return;
        }

        var atividade = submissao.getAtividadeId() != null
            ? atividadeService.buscarEntidadePorId(submissao.getAtividadeId())
            : null;

        String tituloAtividade = atividade != null ? atividade.getTitulo() : "atividade";
        String nomeProfessor = (atividade != null && atividade.getProfessorId() != null)
            ? usuarioRepository.findById(atividade.getProfessorId()).map(u -> u.getNome()).orElse("Professor")
            : "Professor";

        String mensagem = "Submissão de " + tituloAtividade + " foi corrigida";
        if (nota != null) {
            mensagem += " - Nota: " + nota;
        }
        if (feedback != null && !feedback.isBlank()) {
            mensagem += " - " + feedback;
        }

        notificacaoService.notificarUsuario(submissao.getAlunoId(), mensagem);
        }

    private SubmissaoResponseDTO toResponse(Submissao s) {
        String tituloAtividade = s.getAtividadeId() != null
                ? atividadeService.buscarEntidadePorId(s.getAtividadeId()).getTitulo()
                : null;

        String nomeAluno = s.getAlunoId() != null
                ? usuarioRepository.findById(s.getAlunoId()).map(u -> u.getNome()).orElse(null)
                : null;

        return new SubmissaoResponseDTO(
                s.getId(),
                s.getAtividadeId(),
                tituloAtividade,
                s.getAlunoId(),
                nomeAluno,
                s.getArquivoResposta(),
                s.getDataEntrega() != null ? s.getDataEntrega().toString() : null,
                s.getNota(),
                s.getFeedback()
        );
    }
}
