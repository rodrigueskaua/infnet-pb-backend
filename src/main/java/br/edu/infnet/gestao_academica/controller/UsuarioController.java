package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.auth.AutorizacaoHelper;
import br.edu.infnet.gestao_academica.auth.SessaoStore;
import br.edu.infnet.gestao_academica.dto.LoginRequestDTO;
import br.edu.infnet.gestao_academica.dto.LoginResponseDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final SessaoStore sessaoStore;

    public UsuarioController(UsuarioService service, SessaoStore sessaoStore) {
        this.service = service;
        this.sessaoStore = sessaoStore;
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        UsuarioResponseDTO usuario = service.login(dto);
        String token = sessaoStore.criarSessao(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.id(), usuario.nome(), usuario.email(), usuario.papel()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("X-Token");
        sessaoStore.removerSessao(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(HttpServletRequest request) {
        return ResponseEntity.ok(AutorizacaoHelper.getUsuarioLogado(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
            @RequestParam(required = false) String papel,
            HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR");
        if (papel != null && !papel.isBlank()) {
            return ResponseEntity.ok(service.listarPorPerfil(papel));
        }
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id, HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR", "PROFESSOR");
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpServletRequest request) {
        AutorizacaoHelper.exigirPerfil(request, "DIRETOR");
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
