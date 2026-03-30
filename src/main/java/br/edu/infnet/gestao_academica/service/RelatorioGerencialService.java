package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.model.Atividade;
import br.edu.infnet.gestao_academica.model.Submissao;
import br.edu.infnet.gestao_academica.model.Turma;
import br.edu.infnet.gestao_academica.repository.AtividadeCsvRepository;
import br.edu.infnet.gestao_academica.repository.SubmissaoCsvRepository;
import br.edu.infnet.gestao_academica.repository.TurmaCsvRepository;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RelatorioGerencialService {

    private final TurmaCsvRepository turmaRepository;
    private final UsuarioCsvRepository usuarioRepository;
    private final AtividadeCsvRepository atividadeRepository;
    private final SubmissaoCsvRepository submissaoRepository;

    public RelatorioGerencialService(TurmaCsvRepository turmaRepository,
                                     UsuarioCsvRepository usuarioRepository,
                                     AtividadeCsvRepository atividadeRepository,
                                     SubmissaoCsvRepository submissaoRepository) {
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
        this.atividadeRepository = atividadeRepository;
        this.submissaoRepository = submissaoRepository;
    }

    public byte[] gerarCsvEngajamentoPorTurma(Long turmaId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada: " + turmaId));

        String nomeProfessor = turma.getProfessorId() != null
                ? usuarioRepository.findById(turma.getProfessorId()).map(u -> u.getNome()).orElse("Professor nao encontrado")
                : "Sem professor";

        List<Atividade> atividadesDaTurma = atividadeRepository.findByTurmaId(turmaId);
        int totalAtividades = atividadesDaTurma.size();

        Set<Long> atividadeIdsDaTurma = atividadesDaTurma.stream()
                .map(Atividade::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, LocalDateTime> prazoPorAtividade = atividadesDaTurma.stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Atividade::getId, Atividade::getDataPrazo, (v1, v2) -> v1));

        List<Submissao> submissoesDaTurma = atividadesDaTurma.stream()
                .flatMap(a -> submissaoRepository.findByAtividadeId(a.getId()).stream())
                .toList();

        Map<Long, List<Submissao>> submissoesPorAluno = submissoesDaTurma.stream()
                .filter(s -> s.getAlunoId() != null)
                .collect(Collectors.groupingBy(Submissao::getAlunoId));

        Set<Long> alunosRelatorio = new LinkedHashSet<>();
        if (turma.getAlunosIds() != null) {
            alunosRelatorio.addAll(turma.getAlunosIds());
        }
        alunosRelatorio.addAll(submissoesPorAluno.keySet());

        List<Long> alunosOrdenados = new ArrayList<>(alunosRelatorio);
        alunosOrdenados.sort(Comparator.naturalOrder());

        StringBuilder csv = new StringBuilder();
        csv.append("TURMA_ID,TURMA_CODIGO,DISCIPLINA,PROFESSOR_ID,PROFESSOR_NOME,ALUNO_ID,ALUNO_NOME,TOTAL_ATIVIDADES,ATIVIDADES_ENTREGUES,ENTREGAS_NO_PRAZO,PERCENTUAL_ENTREGA,PERCENTUAL_NO_PRAZO,REALIZOU_ATIVIDADE,MEDIA_NOTAS,PONTUACAO_PROATIVIDADE");
        csv.append('\n');

        for (Long alunoId : alunosOrdenados) {
            List<Submissao> submissoesAluno = submissoesPorAluno.getOrDefault(alunoId, List.of());

            Map<Long, List<Submissao>> porAtividade = submissoesAluno.stream()
                    .filter(s -> s.getAtividadeId() != null && atividadeIdsDaTurma.contains(s.getAtividadeId()))
                    .collect(Collectors.groupingBy(Submissao::getAtividadeId));

            int atividadesEntregues = porAtividade.size();
            int entregasNoPrazo = contarEntregasNoPrazo(porAtividade, prazoPorAtividade);

            double percentualEntrega = totalAtividades == 0 ? 0.0 : (atividadesEntregues * 100.0) / totalAtividades;
            double percentualNoPrazo = totalAtividades == 0 ? 0.0 : (entregasNoPrazo * 100.0) / totalAtividades;
            String realizouAtividade = atividadesEntregues > 0 ? "SIM" : "NAO";
            double mediaNotas = calcularMediaNotas(porAtividade);
            double pontuacaoProatividade = (percentualEntrega * 0.7) + (percentualNoPrazo * 0.3);

            String nomeAluno = usuarioRepository.findById(alunoId)
                    .map(u -> u.getNome())
                    .orElse("Aluno nao encontrado");

            List<String> colunas = List.of(
                    String.valueOf(turma.getId()),
                    valorOuVazio(turma.getCodigo()),
                    valorOuVazio(turma.getNomeDisciplina()),
                    turma.getProfessorId() != null ? String.valueOf(turma.getProfessorId()) : "",
                    nomeProfessor,
                    String.valueOf(alunoId),
                    nomeAluno,
                    String.valueOf(totalAtividades),
                    String.valueOf(atividadesEntregues),
                    String.valueOf(entregasNoPrazo),
                    formatarDecimal(percentualEntrega),
                    formatarDecimal(percentualNoPrazo),
                    realizouAtividade,
                    formatarDecimal(mediaNotas),
                    formatarDecimal(pontuacaoProatividade)
            );

            csv.append(colunas.stream().map(this::escaparCsv).collect(Collectors.joining(",")));
            csv.append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private int contarEntregasNoPrazo(Map<Long, List<Submissao>> porAtividade, Map<Long, LocalDateTime> prazoPorAtividade) {
        int noPrazo = 0;

        for (Map.Entry<Long, List<Submissao>> entry : porAtividade.entrySet()) {
            LocalDateTime prazo = prazoPorAtividade.get(entry.getKey());

            if (prazo == null) {
                noPrazo++;
                continue;
            }

            LocalDateTime primeiraEntrega = entry.getValue().stream()
                    .map(Submissao::getDataEntrega)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            if (primeiraEntrega != null && !primeiraEntrega.isAfter(prazo)) {
                noPrazo++;
            }
        }

        return noPrazo;
    }

    private double calcularMediaNotas(Map<Long, List<Submissao>> porAtividade) {
        List<Double> notas = porAtividade.values().stream()
                .map(lista -> lista.stream()
                        .max(Comparator.comparing(Submissao::getId))
                        .orElse(null))
                .filter(Objects::nonNull)
                .map(Submissao::getNota)
                .filter(Objects::nonNull)
                .toList();

        if (notas.isEmpty()) {
            return 0.0;
        }

        return notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    private String formatarDecimal(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private String escaparCsv(String valor) {
        String seguro = valorOuVazio(valor);
        if (seguro.contains(",") || seguro.contains("\"") || seguro.contains("\n") || seguro.contains("\r")) {
            return '"' + seguro.replace("\"", "\"\"") + '"';
        }
        return seguro;
    }
}