package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.SubmissaoResponseDTO;
import br.edu.infnet.gestao_academica.model.Submissao;
import br.edu.infnet.gestao_academica.repository.SubmissaoCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissaoService {

    private final SubmissaoCsvRepository repository;
    private final AtividadeService atividadeService;
    private final NotificacaoService notificacaoService;

    public SubmissaoService(SubmissaoCsvRepository repository,
                            AtividadeService atividadeService,
                            NotificacaoService notificacaoService) {
        this.repository = repository;
        this.atividadeService = atividadeService;
        this.notificacaoService = notificacaoService;
    }

    public SubmissaoResponseDTO submeter(Long atividadeId, Long alunoId, String caminhoArquivo) {
        if (!atividadeService.isPrazoValido(atividadeId)) {
            throw new IllegalStateException("Prazo da atividade encerrado.");
        }

        Submissao submissao = new Submissao();
        submissao.setAtividadeId(atividadeId);
        submissao.setAlunoId(alunoId);
        submissao.setArquivoResposta(caminhoArquivo);
        submissao.setDataEntrega(LocalDateTime.now());

        Submissao salva = repository.save(submissao);

        Long professorId = atividadeService.buscarEntidadePorId(atividadeId).getProfessorId();
        if (professorId != null) {
            notificacaoService.notificarUsuario(
                    professorId,
                    "Aluno ID " + alunoId + " submeteu resposta para atividade ID " + atividadeId
            );
        }

        return toResponse(salva);
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

    private SubmissaoResponseDTO toResponse(Submissao s) {
        return new SubmissaoResponseDTO(
                s.getId(),
                s.getAtividadeId(),
                s.getAlunoId(),
                s.getArquivoResposta(),
                s.getDataEntrega() != null ? s.getDataEntrega().toString() : null,
                s.getNota(),
                s.getFeedback()
        );
    }
}
