package br.edu.infnet.gestao_academica.dto;

public record LoginResponseDTO(String token, Long id, String nome, String email, String perfil) {}
