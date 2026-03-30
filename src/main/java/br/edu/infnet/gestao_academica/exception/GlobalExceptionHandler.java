package br.edu.infnet.gestao_academica.exception;

import br.edu.infnet.gestao_academica.dto.UsuarioResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex,
                                                                     HttpServletRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String reason = ex.getReason() != null ? ex.getReason() : "Erro na requisição.";

        if (statusCode.value() == HttpStatus.FORBIDDEN.value()) {
            Object usuarioLogado = request.getAttribute("usuarioLogado");
            String perfilAtual = usuarioLogado instanceof UsuarioResponseDTO usuario
                    ? usuario.perfil()
                    : "DESCONHECIDO";

            String mensagem = reason + " Perfil atual: " + perfilAtual + ".";
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", mensagem));
        }

        return ResponseEntity.status(statusCode)
                .body(Map.of("erro", reason));
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioJaExiste(UsuarioJaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, String>> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "Arquivo excede o limite permitido de 20MB."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(MultipartException ex) {
        String mensagem = ex.getMessage() != null ? ex.getMessage() : "";
        if (mensagem.contains("FileSizeLimitExceededException")
                || mensagem.contains("size")
                || mensagem.contains("exceeds its maximum permitted size")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Arquivo excede o limite permitido de 20MB."));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "Erro ao processar upload do arquivo."));
    }
}
