package br.com.torquato.rinha.application;

import br.com.torquato.rinha.domain.model.ExtratoCliente;

public interface ExecutorBuscaExtrato {


    Resposta buscar(final int idCliente);

    record Resposta(Status status, ExtratoCliente extratoCliente) {
        public Resposta(Status status) {
            this(status, null);
        }
    }

    enum Status {
        OK,
        CLIENTE_INVALIDO
    }
}
