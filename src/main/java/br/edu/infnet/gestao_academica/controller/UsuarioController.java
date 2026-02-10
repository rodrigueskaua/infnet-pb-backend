package br.edu.infnet.gestao_academica.controller;

import br.edu.infnet.gestao_academica.dto.LoginRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioRequestDTO;
import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import br.edu.infnet.gestao_academica.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = service.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        UsuarioResponseDTO response = service.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
