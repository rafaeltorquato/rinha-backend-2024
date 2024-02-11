package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.ExecutorBuscaExtrato;
import br.com.torquato.rinha.domain.model.ExtratoCliente;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Startup
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
        if (!this.cacheClientes.contains(idCliente)) {
            return this.clienteInvalido;
        }

        try (final var connection = this.dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.retorna_extrato(?)}")) {
            stmt.setInt(1, idCliente);
            stmt.execute();

            final ExtratoCliente.Saldo saldo = getSaldo(stmt);
            final List<ExtratoCliente.Transacao> transacoes = getTransacoes(stmt);
            return new Resposta(
                    Status.OK,
                    new ExtratoCliente(saldo, transacoes)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ExtratoCliente.Saldo getSaldo(CallableStatement stmt) throws SQLException {
        final ResultSet rsSaldo = stmt.getResultSet();
        rsSaldo.next();
        return new ExtratoCliente.Saldo(
                rsSaldo.getInt("limite"),
                rsSaldo.getInt("saldo"),
                rsSaldo.getTimestamp("data_extrato").toLocalDateTime()
        );
    }

    private static List<ExtratoCliente.Transacao> getTransacoes(CallableStatement stmt) throws SQLException {
        if (!stmt.getMoreResults()) {
            return Collections.emptyList();
        }

        final ResultSet rsTransacoes = stmt.getResultSet();
        final List<ExtratoCliente.Transacao> transacoes = new ArrayList<>(rsTransacoes.getFetchSize());

        while (rsTransacoes.next()) {
            transacoes.add(new ExtratoCliente.Transacao(
                    rsTransacoes.getInt("valor"),
                    rsTransacoes.getString("tipo"),
                    rsTransacoes.getString("descricao"),
                    rsTransacoes.getTimestamp("realizada_em").toLocalDateTime()
            ));
        }
        return transacoes;
    }

    void onStart(@Observes StartupEvent evt) {
        this.buscar(this.cacheClientes.stream().findFirst().get());
        log.warn("Extrato warn up!");
    }
}
