package br.edu.infnet.gestao_academica.exception;

public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException(String message) {
        super(message);
    }
}
