package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.AtividadeRequestDTO;
import br.edu.infnet.gestao_academica.dto.AtividadeResponseDTO;
import br.edu.infnet.gestao_academica.model.Atividade;
import br.edu.infnet.gestao_academica.model.StatusAtividade;
import br.edu.infnet.gestao_academica.repository.AtividadeCsvRepository;
import br.edu.infnet.gestao_academica.repository.SubmissaoCsvRepository;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AtividadeService {

    private final AtividadeCsvRepository repository;
    private final UsuarioCsvRepository usuarioRepository;
    private final TurmaCsvRepository turmaRepository;
    private final SubmissaoCsvRepository submissaoRepository;
    private final NotificacaoService notificacaoService;

    public AtividadeService(AtividadeCsvRepository repository,
                            UsuarioCsvRepository usuarioRepository,
                            TurmaCsvRepository turmaRepository,
                            SubmissaoCsvRepository submissaoRepository,
                            NotificacaoService notificacaoService) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.submissaoRepository = submissaoRepository;
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

    public List<AtividadeResponseDTO> listarPorTurmaComFiltroAluno(Long turmaId, Long alunoId, String status) {
        String filtro = normalizarStatusFiltro(status);
        List<Atividade> atividadesTurma = repository.findByTurmaId(turmaId);

        if (filtro == null) {
            return atividadesTurma.stream().map(this::toResponse).toList();
        }

        Set<Long> atividadesIds = atividadesTurma.stream()
                .map(Atividade::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, List<br.edu.infnet.gestao_academica.model.Submissao>> submissoesPorAtividade = submissaoRepository.findByAlunoId(alunoId).stream()
                .filter(s -> s.getAtividadeId() != null && atividadesIds.contains(s.getAtividadeId()))
                .collect(Collectors.groupingBy(br.edu.infnet.gestao_academica.model.Submissao::getAtividadeId));

        List<Atividade> filtradas = new ArrayList<>();
        for (Atividade atividade : atividadesTurma) {
            List<br.edu.infnet.gestao_academica.model.Submissao> submissoesAluno = submissoesPorAtividade
                    .getOrDefault(atividade.getId(), List.of());
            boolean possuiSubmissao = !submissoesAluno.isEmpty();
            boolean possuiCorrecao = submissoesAluno.stream()
                    .anyMatch(s -> s.getNota() != null || (s.getFeedback() != null && !s.getFeedback().isBlank()));

            if ("PENDENTES".equals(filtro) && !possuiSubmissao) {
                filtradas.add(atividade);
            }
            if ("ENTREGUES".equals(filtro) && possuiSubmissao && !possuiCorrecao) {
                filtradas.add(atividade);
            }
            if ("AVALIADAS".equals(filtro) && possuiCorrecao) {
                filtradas.add(atividade);
            }
        }

        return filtradas.stream().map(this::toResponse).toList();
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

    private String normalizarStatusFiltro(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String valor = status.trim().toUpperCase(Locale.ROOT);
        return switch (valor) {
            case "PENDENTES", "PENDENTE" -> "PENDENTES";
            case "ENTREGUES", "ENTREGUE" -> "ENTREGUES";
            case "AVALIADAS", "AVALIADA" -> "AVALIADAS";
            default -> throw new IllegalArgumentException("Status inválido. Use: Pendentes, Entregues ou Avaliadas.");
        };
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

            String mensagem = "Nova atividade: " + atividade.getTitulo() + " foi publicada";

            turma.getAlunosIds().forEach(alunoId -> notificacaoService.notificarUsuario(alunoId, mensagem));
        });
    }

    private AtividadeResponseDTO toResponse(Atividade a) {
        String nomeProfessor = a.getProfessorId() != null
                ? usuarioRepository.findById(a.getProfessorId()).map(u -> u.getNome()).orElse(null)
                : null;

        var turma = a.getTurmaId() != null
            ? turmaRepository.findById(a.getTurmaId()).orElse(null)
            : null;

        String nomeDisciplina = turma != null ? turma.getNomeDisciplina() : null;

        Set<Long> alunosMatriculados = turma != null && turma.getAlunosIds() != null
            ? new HashSet<>(turma.getAlunosIds())
            : Set.of();

        int quantosEntregaram = (int) submissaoRepository.findByAtividadeId(a.getId()).stream()
            .map(s -> s.getAlunoId())
            .filter(alunosMatriculados::contains)
            .distinct()
            .count();

        int quantosPendentes = Math.max(alunosMatriculados.size() - quantosEntregaram, 0);

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
                nomeProfessor,
                quantosEntregaram,
                quantosPendentes
        );
    }
}
