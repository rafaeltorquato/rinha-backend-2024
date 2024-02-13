package br.com.torquato.rinha.application;

public interface Extratos {


    Resposta buscar(final int idCliente);

    record Resposta(Status status, String extratoCliente) {
        public Resposta(Status status) {
            this(status, null);
        }
    }

    enum Status {
        OK,
        CLIENTE_INVALIDO
    }
}
