package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.NotificacaoResponseDTO;
import br.edu.infnet.gestao_academica.model.Notificacao;
import br.edu.infnet.gestao_academica.repository.NotificacaoCsvRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoCsvRepository repository;

    public NotificacaoService(NotificacaoCsvRepository repository) {
        this.repository = repository;
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
