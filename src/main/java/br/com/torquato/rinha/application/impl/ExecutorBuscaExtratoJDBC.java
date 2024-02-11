package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.ExecutorBuscaExtrato;
import br.com.torquato.rinha.domain.model.ExtratoCliente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ExecutorBuscaExtratoJDBC implements ExecutorBuscaExtrato {

    private final ExecutorBuscaExtrato.Resposta clienteInvalido = new ExecutorBuscaExtrato.Resposta(
            Status.CLIENTE_INVALIDO
    );

    @Inject
    DataSource dataSource;

    @Inject
    Set<Integer> cacheClientes;

    @Override
    public Resposta buscar(final int idCliente) {
        if (!cacheClientes.contains(idCliente)) {
            return clienteInvalido;
        }

        try (final var connection = dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.retorna_extrato(?)}")) {
            stmt.setInt(1, idCliente);
            stmt.execute();
            final ResultSet rsSaldo = stmt.getResultSet();
            rsSaldo.next();
            final ExtratoCliente.Saldo saldo = new ExtratoCliente.Saldo(
                    rsSaldo.getInt("limite"),
                    rsSaldo.getInt("saldo"),
                    rsSaldo.getTimestamp("data_extrato").toLocalDateTime()
            );
            List<ExtratoCliente.Transacao> transacoes = Collections.emptyList();
            if (stmt.getMoreResults()) {
                final ResultSet resultTransacoes = stmt.getResultSet();
                transacoes = new ArrayList<>(resultTransacoes.getFetchSize());
                while (resultTransacoes.next()) {
                    transacoes.add(new ExtratoCliente.Transacao(
                            resultTransacoes.getInt("valor"),
                            resultTransacoes.getString("tipo"),
                            resultTransacoes.getString("descricao"),
                            resultTransacoes.getTimestamp("realizada_em").toLocalDateTime()
                    ));
                }
            }
            return new Resposta(
                    Status.OK,
                    new ExtratoCliente(saldo, transacoes)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
