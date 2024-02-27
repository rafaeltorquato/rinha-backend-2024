package br.com.torquato.rinha.application;

public interface Extratos {

    Extratos.Resposta CLIENTE_INVALIDO = new Extratos.Resposta(Status.CLIENTE_INVALIDO);


    Resposta buscar(final short idCliente);

    record Resposta(Status status, String extratoCliente) {
        public Resposta(Status status) {
            this(status, null);
        }

        public Resposta(String extratoCliente) {
            this(Status.OK, extratoCliente);
        }
    }

    enum Status {
        OK,
        CLIENTE_INVALIDO
    }
}
