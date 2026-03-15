package br.edu.infnet.gestao_academica.service;

import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    public void notificarProfessor(Long professorId, String mensagem) {
        System.out.printf("[NOTIFICAÇÃO] Professor ID %d: %s%n", professorId, mensagem);
    }
}
