package br.com.torquato.rinha.application;

import br.com.torquato.rinha.domain.model.TransacaoPendente;

public interface Transacoes {

    Resposta TRANSACAO_INVALIDA = new Resposta(Status.TRANSACAO_INVALIDA);

    Resposta SEM_SALDO = new Resposta(Status.SEM_SALDO);

    Resposta CLIENTE_INVALIDO = new Resposta(Status.CLIENTE_INVALIDO);

    Resposta processar(final Solicitacao solicitacao);

    record Resposta(Status status, String saldo) {
        public Resposta(Status status) {
            this(status, null);
        }
        public Resposta(String saldo) {
            this(Status.OK, saldo);
        }
    }

    record Solicitacao(int idCliente, TransacaoPendente transacaoPendente) {
    }


    enum Status {
        OK,
        SEM_SALDO,
        TRANSACAO_INVALIDA,
        CLIENTE_INVALIDO
    }
}
