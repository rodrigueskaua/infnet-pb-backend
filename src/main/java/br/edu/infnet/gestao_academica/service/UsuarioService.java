package br.edu.infnet.gestao_academica.service;

import br.edu.infnet.gestao_academica.dto.LoginRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.exception.UsuarioJaExisteException;
import br.edu.infnet.gestao_academica.exception.UsuarioNaoEncontradoException;
import br.edu.infnet.gestao_academica.model.Usuario;
import br.edu.infnet.gestao_academica.repository.UsuarioCsvRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UsuarioService {

    private static final Set<String> PAPEIS_VALIDOS = Set.of("ALUNO", "PROFESSOR", "ADMIN");

    private final UsuarioCsvRepository repository;

    public UsuarioService(UsuarioCsvRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponseDTO registrar(UsuarioRequestDTO dto) {
        if (repository.findByEmail(dto.email()).isPresent()) {
            throw new UsuarioJaExisteException("Já existe um usuário com o email: " + dto.email());
        }

        String papel = dto.papel().toUpperCase();
        if (!PAPEIS_VALIDOS.contains(papel)) {
            throw new IllegalArgumentException("Papel inválido: " + dto.papel() + ". Use: ALUNO, PROFESSOR ou ADMIN");
        }

        Usuario usuario = new Usuario(null, dto.nome(), dto.email(), dto.senha(), papel);
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);
    }

    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = repository.findByEmail(dto.email())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com email: " + dto.email()));

        if (!usuario.getSenha().equals(dto.senha())) {
            throw new UsuarioNaoEncontradoException("Credenciais inválidas");
        }

        return toResponse(usuario);
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream()
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

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel());
    }
}
