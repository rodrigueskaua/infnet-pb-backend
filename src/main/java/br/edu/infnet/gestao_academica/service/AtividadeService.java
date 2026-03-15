package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.AtividadeRequestDTO;
import br.edu.infnet.gestao_academica.dto.AtividadeResponseDTO;
import br.edu.infnet.gestao_academica.model.Atividade;
import br.edu.infnet.gestao_academica.model.StatusAtividade;
import br.edu.infnet.gestao_academica.repository.AtividadeCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AtividadeService {

    private final AtividadeCsvRepository repository;

    public AtividadeService(AtividadeCsvRepository repository) {
        this.repository = repository;
    }

    public AtividadeResponseDTO criar(AtividadeRequestDTO dto) {
        Atividade atividade = new Atividade();
        atividade.setTitulo(dto.titulo());
        atividade.setDescricao(dto.descricao());
        atividade.setDataPrazo(dto.dataPrazo() != null ? LocalDateTime.parse(dto.dataPrazo()) : null);
        atividade.setStatus(StatusAtividade.PUBLICADA);
        atividade.setArquivoApoio(dto.arquivoApoio());
        atividade.setTurmaId(dto.turmaId());

        Atividade salva = repository.save(atividade);
        return toResponse(salva);
    }

    public AtividadeResponseDTO buscarPorId(Long id) {
        Atividade atividade = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada: " + id));
        return toResponse(atividade);
    }

    public List<AtividadeResponseDTO> listarPorTurma(Long turmaId) {
        return repository.findByTurmaId(turmaId).stream().map(this::toResponse).toList();
    }

    public List<AtividadeResponseDTO> listarTodas() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public Atividade buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada: " + id));
    }

    public boolean isPrazoValido(Long atividadeId) {
        Atividade atividade = buscarEntidadePorId(atividadeId);
        return atividade.getDataPrazo() == null || LocalDateTime.now().isBefore(atividade.getDataPrazo());
    }

    private AtividadeResponseDTO toResponse(Atividade a) {
        return new AtividadeResponseDTO(
                a.getId(),
                a.getTitulo(),
                a.getDescricao(),
                a.getDataPrazo() != null ? a.getDataPrazo().toString() : null,
                a.getStatus() != null ? a.getStatus().name() : null,
                a.getArquivoApoio(),
                a.getTurmaId()
        );
    }
}
