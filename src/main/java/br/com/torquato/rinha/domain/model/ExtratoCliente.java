package br.com.torquato.rinha.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record ExtratoCliente(Saldo saldo, List<Transacao> ultimas_transacoes) {

    public record Saldo(int limite,
                        int total,
                        LocalDateTime data_extrato) {
    }

    public record Transacao(
            int valor,
            String tipo,
            String descricao,
            LocalDateTime realizada_em) {
    }
}
