package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.LoginRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.exception.CredenciaisInvalidasException;
import br.edu.infnet.gestao_academica.exception.UsuarioJaExisteException;
import br.edu.infnet.gestao_academica.exception.UsuarioNaoEncontradoException;
import br.edu.infnet.gestao_academica.model.Aluno;
import br.edu.infnet.gestao_academica.model.Diretor;
import br.edu.infnet.gestao_academica.model.PerfilUsuario;
import br.edu.infnet.gestao_academica.model.Professor;
import br.edu.infnet.gestao_academica.model.Usuario;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioCsvRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioCsvRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO registrar(UsuarioRequestDTO dto) {
        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new UsuarioJaExisteException("Já existe um usuário com o email: " + dto.email());
        }

        PerfilUsuario perfil = parsePerfil(dto.perfil());
        if (perfil == PerfilUsuario.DIRETOR) {
            throw new IllegalArgumentException("Papel inválido. Use: ALUNO ou PROFESSOR");
        }

        String senhaHash = passwordEncoder.encode(dto.senha());
        Usuario usuario = criarUsuario(dto, senhaHash, perfil);
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);
    }

    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = repository.findByEmail(dto.email())
                .orElseThrow(() -> new CredenciaisInvalidasException("Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }

        return toResponse(usuario);
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<UsuarioResponseDTO> listarPorPerfil(String papel) {
        return repository.findByPerfil(papel).stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com id: " + id));
        return toResponse(usuario);
    }

    public void deletar(Long id) {
        if (repository.findById(id).isEmpty()) {
            throw new UsuarioNaoEncontradoException("Usuário não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    private Usuario criarUsuario(UsuarioRequestDTO dto, String senhaHash, PerfilUsuario perfil) {
        return switch (perfil) {
            case ALUNO -> new Aluno(null, dto.nome(), dto.email(), senhaHash, null);
            case PROFESSOR -> new Professor(null, dto.nome(), dto.email(), senhaHash, null);
            case DIRETOR -> new Diretor(null, dto.nome(), dto.email(), senhaHash, null);
        };
    }

    private PerfilUsuario parsePerfil(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Papel inválido. Use: ALUNO ou PROFESSOR");
        }

        if ("ADMIN".equalsIgnoreCase(valor)) {
            throw new IllegalArgumentException("Papel inválido. Use: ALUNO ou PROFESSOR");
        }

        try {
            return PerfilUsuario.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Papel inválido: " + valor + ". Use: ALUNO ou PROFESSOR");
        }
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
    }
}
