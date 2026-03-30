package br.edu.infnet.gestao_academica.auth;

import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AutorizacaoHelper {

    private AutorizacaoHelper() {}

    public static UsuarioResponseDTO getUsuarioLogado(HttpServletRequest request) {
        UsuarioResponseDTO usuario = (UsuarioResponseDTO) request.getAttribute("usuarioLogado");
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado.");
        }
        return usuario;
    }

    public static UsuarioResponseDTO exigirPerfil(HttpServletRequest request, String... perfisPermitidos) {
        UsuarioResponseDTO usuario = getUsuarioLogado(request);
        for (String perfil : perfisPermitidos) {
            if (perfil.equalsIgnoreCase(usuario.perfil())) return usuario;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Acesso negado. Perfil necessário: " + String.join(" ou ", perfisPermitidos));
    }
}
