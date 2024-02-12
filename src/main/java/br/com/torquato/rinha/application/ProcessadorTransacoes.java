package br.com.torquato.rinha.application;

import br.com.torquato.rinha.domain.model.SaldoPosTransacao;
import br.com.torquato.rinha.domain.model.TransacaoPendente;

public interface ProcessadorTransacoes {

    Resposta processar(final Solicitacao solicitacao);

    record Resposta(Status status, String saldo) {
        public Resposta(Status status){
            this(status, null);
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
