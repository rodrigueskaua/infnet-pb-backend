package br.edu.infnet.gestao_academica.auth;

import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class AutenticacaoFilter extends OncePerRequestFilter {

    private static final Set<String> ROTAS_PUBLICAS = Set.of(
            "/api/usuarios/registrar",
            "/api/usuarios/login"
    );

    private final SessaoStore sessaoStore;

    public AutenticacaoFilter(SessaoStore sessaoStore) {
        this.sessaoStore = sessaoStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (ROTAS_PUBLICAS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("X-Token");
        UsuarioResponseDTO usuario = sessaoStore.buscarPorToken(token).orElse(null);

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\": \"Não autenticado. Faça login e use o header X-Token.\"}");
            return;
        }

        request.setAttribute("usuarioLogado", usuario);
        chain.doFilter(request, response);
    }
}
