package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.GrupoRequestDTO;
import br.edu.infnet.gestao_academica.dto.GrupoResponseDTO;
import br.edu.infnet.gestao_academica.model.Grupo;
import br.edu.infnet.gestao_academica.model.Turma;
import br.edu.infnet.gestao_academica.repository.GrupoCsvRepository;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
public class GrupoService {

    private final GrupoCsvRepository grupoRepository;
    private final TurmaCsvRepository turmaRepository;
    private final UsuarioCsvRepository usuarioRepository;

    public GrupoService(GrupoCsvRepository grupoRepository,
                        TurmaCsvRepository turmaRepository,
                        UsuarioCsvRepository usuarioRepository) {
        this.grupoRepository = grupoRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public GrupoResponseDTO criarGrupo(GrupoRequestDTO dto, Long alunoLogadoId) {
        if (dto.turmaId() == null) {
            throw new IllegalArgumentException("Turma é obrigatória.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do grupo é obrigatório.");
        }

        Turma turma = turmaRepository.findById(dto.turmaId())
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + dto.turmaId()));

        validarAlunoMatriculado(turma, alunoLogadoId);

        List<Grupo> gruposDaTurma = grupoRepository.findByTurmaId(dto.turmaId());
        if (gruposDaTurma.stream().anyMatch(g -> Boolean.TRUE.equals(g.getFinalizado()))) {
            throw new IllegalStateException("Os grupos desta turma já foram finalizados pelo professor.");
        }

        LinkedHashSet<Long> integrantes = new LinkedHashSet<>();
        integrantes.add(alunoLogadoId);
        if (dto.integrantesIds() != null) {
            integrantes.addAll(dto.integrantesIds());
        }

        if (integrantes.size() < 2) {
            throw new IllegalArgumentException("Grupo deve ter pelo menos 2 alunos.");
        }

        List<Long> alunosTurma = turma.getAlunosIds() == null ? List.of() : turma.getAlunosIds();
        for (Long integranteId : integrantes) {
            if (!alunosTurma.contains(integranteId)) {
                throw new IllegalArgumentException("Aluno " + integranteId + " não está matriculado na turma.");
            }
        }

        for (Grupo grupo : gruposDaTurma) {
            for (Long integranteId : integrantes) {
                if (grupo.getAlunosIds() != null && grupo.getAlunosIds().contains(integranteId)) {
                    throw new IllegalStateException("Aluno " + integranteId + " já pertence a um grupo da turma.");
                }
            }
        }

        Grupo grupo = new Grupo();
        grupo.setTurmaId(dto.turmaId());
        grupo.setNome(dto.nome().trim());
        grupo.setLiderId(alunoLogadoId);
        grupo.setAlunosIds(new ArrayList<>(integrantes));
        grupo.setFinalizado(false);

        return toResponse(grupoRepository.save(grupo));
    }

    public List<GrupoResponseDTO> listarPorTurma(Long turmaId, Long usuarioLogadoId, String perfil) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));

        if ("ALUNO".equalsIgnoreCase(perfil)) {
            validarAlunoMatriculado(turma, usuarioLogadoId);
        }

        if ("PROFESSOR".equalsIgnoreCase(perfil) && !Objects.equals(turma.getProfessorId(), usuarioLogadoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor só pode listar grupos da própria turma.");
        }

        return grupoRepository.findByTurmaId(turmaId).stream().map(this::toResponse).toList();
    }

    public List<GrupoResponseDTO> finalizarPorTurma(Long turmaId, Long professorId, String perfil) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada: " + turmaId));

        if ("PROFESSOR".equalsIgnoreCase(perfil) && !Objects.equals(turma.getProfessorId(), professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor só pode finalizar grupos da própria turma.");
        }

        List<Grupo> grupos = grupoRepository.findByTurmaId(turmaId);
        if (grupos.isEmpty()) {
            throw new IllegalStateException("Não há grupos cadastrados para finalizar nesta turma.");
        }

        return grupos.stream().map(grupo -> {
            grupo.setFinalizado(true);
            return toResponse(grupoRepository.save(grupo));
        }).toList();
    }

    public Grupo buscarGrupoFinalizadoDoAluno(Long turmaId, Long alunoId) {
        return grupoRepository.findByTurmaIdAndAlunoId(turmaId, alunoId)
                .filter(g -> Boolean.TRUE.equals(g.getFinalizado()))
                .orElse(null);
    }

    private void validarAlunoMatriculado(Turma turma, Long alunoId) {
        if (turma.getAlunosIds() == null || !turma.getAlunosIds().contains(alunoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Aluno só pode formar/listar grupos na turma em que está matriculado.");
        }
    }

    private GrupoResponseDTO toResponse(Grupo grupo) {
        String nomeLider = grupo.getLiderId() != null
                ? usuarioRepository.findById(grupo.getLiderId()).map(u -> u.getNome()).orElse("Lider não encontrado")
                : null;

        return new GrupoResponseDTO(
                grupo.getId(),
                grupo.getTurmaId(),
                grupo.getNome(),
                grupo.getLiderId(),
                nomeLider,
                grupo.getAlunosIds() == null ? List.of() : grupo.getAlunosIds(),
                grupo.getFinalizado() != null && grupo.getFinalizado()
        );
    }
}
