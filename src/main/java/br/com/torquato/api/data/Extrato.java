package br.com.torquato.api.data;

import java.time.LocalDateTime;
import java.util.List;

public record Extrato(Saldo saldo, List<Transacao> ultimas_transacoes) {

    public record Saldo(int limite,
                        int total,
                        LocalDateTime data_extrato) {
    }

    public record Transacao(
            int valor,
            TipoTransacao tipo,
            String descricao,
            LocalDateTime realizada_em) {
    }
}
