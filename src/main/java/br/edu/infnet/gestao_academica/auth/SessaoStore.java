package br.edu.infnet.gestao_academica.auth;

import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessaoStore {

    private final Map<String, UsuarioResponseDTO> sessoes = new ConcurrentHashMap<>();

    public String criarSessao(UsuarioResponseDTO usuario) {
        String token = UUID.randomUUID().toString();
        sessoes.put(token, usuario);
        return token;
    }

    public Optional<UsuarioResponseDTO> buscarPorToken(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(sessoes.get(token));
    }

    public void removerSessao(String token) {
        sessoes.remove(token);
    }
}
