package br.com.torquato.rinha.application;

public interface Extratos {

    Extratos.Resposta CLIENTE_INVALIDO = new Extratos.Resposta(Status.CLIENTE_INVALIDO);


    Resposta buscar(final int idCliente);

    record Resposta(Status status, byte[] extratoCliente) {
        public Resposta(Status status) {
            this(status, null);
        }

        public Resposta(byte[] extratoCliente) {
            this(Status.OK, extratoCliente);
        }
    }

    enum Status {
        OK,
        CLIENTE_INVALIDO
    }
}
