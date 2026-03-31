package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.SubmissaoResponseDTO;
import br.edu.infnet.gestao_academica.model.Atividade;
import br.edu.infnet.gestao_academica.model.Grupo;
import br.edu.infnet.gestao_academica.model.Submissao;
import br.edu.infnet.gestao_academica.repository.GrupoCsvRepository;
import br.edu.infnet.gestao_academica.repository.SubmissaoCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class SubmissaoService {

    private final SubmissaoCsvRepository repository;
    private final AtividadeService atividadeService;
    private final GrupoCsvRepository grupoRepository;
    private final NotificacaoService notificacaoService;
    private final UsuarioCsvRepository usuarioRepository;

    public SubmissaoService(SubmissaoCsvRepository repository,
                            AtividadeService atividadeService,
                            GrupoCsvRepository grupoRepository,
                            NotificacaoService notificacaoService,
                            UsuarioCsvRepository usuarioRepository) {
        this.repository = repository;
        this.atividadeService = atividadeService;
        this.grupoRepository = grupoRepository;
        this.notificacaoService = notificacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    public SubmissaoResponseDTO submeter(Long atividadeId, Long alunoId, String caminhoArquivo) {
        if (!atividadeService.isPrazoValido(atividadeId)) {
            throw new IllegalStateException("Prazo da atividade encerrado.");
        }

        Atividade atividade = atividadeService.buscarEntidadePorId(atividadeId);

        if (atividade.getEmGrupo() != null && atividade.getEmGrupo()) {
            return submeterEmGrupo(atividade, alunoId, caminhoArquivo);
        }

        validarReenvioComSubmissaoJaCorrigida(atividadeId, alunoId);

        Submissao submissao = new Submissao();
        submissao.setAtividadeId(atividadeId);
        submissao.setAlunoId(alunoId);
        submissao.setArquivoResposta(caminhoArquivo);
        submissao.setDataEntrega(LocalDateTime.now());

        Submissao salva = repository.save(submissao);

        String nomeAluno = usuarioRepository.findById(alunoId).map(u -> u.getNome()).orElse("Aluno");

        if (atividade.getProfessorId() != null) {
            notificacaoService.notificarUsuario(
                    atividade.getProfessorId(),
                    nomeAluno + " submeteu resposta para \"" + atividade.getTitulo() + "\""
            );
        }

        return toResponse(salva);
    }

    private SubmissaoResponseDTO submeterEmGrupo(Atividade atividade, Long alunoId, String caminhoArquivo) {
        if (atividade.getTurmaId() == null) {
            throw new IllegalStateException("Atividade em grupo sem turma vinculada.");
        }

        Grupo grupo = grupoRepository.findByTurmaIdAndAlunoId(atividade.getTurmaId(), alunoId)
                .filter(g -> Boolean.TRUE.equals(g.getFinalizado()))
                .orElseThrow(() -> new IllegalStateException("Para esta atividade em grupo, o aluno deve estar em grupo finalizado."));

        List<Long> integrantes = grupo.getAlunosIds() == null ? List.of() : grupo.getAlunosIds();
        if (integrantes.isEmpty()) {
            throw new IllegalStateException("Grupo sem integrantes.");
        }

        for (Long integranteId : integrantes) {
            validarReenvioComSubmissaoJaCorrigida(atividade.getId(), integranteId);
        }

        LocalDateTime agora = LocalDateTime.now();
        Submissao submissaoDoAlunoLogado = null;

        for (Long integranteId : integrantes) {
            Submissao submissao = repository.findByAtividadeId(atividade.getId()).stream()
                    .filter(s -> integranteId.equals(s.getAlunoId()))
                    .max(Comparator.comparing(Submissao::getId))
                    .orElseGet(Submissao::new);

            submissao.setAtividadeId(atividade.getId());
            submissao.setAlunoId(integranteId);
            submissao.setArquivoResposta(caminhoArquivo);
            submissao.setDataEntrega(agora);

            Submissao salva = repository.save(submissao);
            if (Objects.equals(integranteId, alunoId)) {
                submissaoDoAlunoLogado = salva;
            }
        }

        String nomeAluno = usuarioRepository.findById(alunoId).map(u -> u.getNome()).orElse("Aluno");
        if (atividade.getProfessorId() != null) {
            notificacaoService.notificarUsuario(
                    atividade.getProfessorId(),
                    nomeAluno + " submeteu resposta em grupo para \"" + atividade.getTitulo() + "\""
            );
        }

        if (submissaoDoAlunoLogado == null) {
            throw new IllegalStateException("Falha ao registrar submissão em grupo.");
        }

        return toResponse(submissaoDoAlunoLogado);
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

        Atividade atividade = s.getAtividadeId() != null
                ? atividadeService.buscarEntidadePorId(s.getAtividadeId())
                : null;

        if (atividade != null && atividade.getEmGrupo() != null && atividade.getEmGrupo()) {
            corrigirSubmissoesDoGrupo(atividade, s, nota, feedback);
            Submissao atualizada = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Submissão não encontrada: " + id));
            return toResponse(atualizada);
        }

        s.setNota(nota);
        s.setFeedback(feedback);

        Submissao salva = repository.save(s);
        notificarAlunoCorrecao(salva, nota, feedback);
        return toResponse(salva);
    }

    private void corrigirSubmissoesDoGrupo(Atividade atividade, Submissao referencia, Double nota, String feedback) {
        if (atividade.getTurmaId() == null || referencia.getAlunoId() == null) {
            throw new IllegalStateException("Submissão em grupo inválida para correção.");
        }

        Grupo grupo = grupoRepository.findByTurmaIdAndAlunoId(atividade.getTurmaId(), referencia.getAlunoId())
                .orElseThrow(() -> new IllegalStateException("Grupo da submissão não encontrado."));

        List<Long> integrantes = grupo.getAlunosIds() == null ? List.of() : grupo.getAlunosIds();
        for (Long integranteId : integrantes) {
            repository.findByAtividadeId(atividade.getId()).stream()
                    .filter(s -> integranteId.equals(s.getAlunoId()))
                    .forEach(sub -> {
                        sub.setNota(nota);
                        sub.setFeedback(feedback);
                        repository.save(sub);
                        notificarAlunoCorrecao(sub, nota, feedback);
                    });
        }
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
