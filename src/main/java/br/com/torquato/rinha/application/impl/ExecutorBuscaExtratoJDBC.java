package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.ExecutorBuscaExtrato;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Types;
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
             final var stmt = connection.prepareCall("{call rinha.retorna_extrato(?,?)}")) {
            stmt.setInt(1, idCliente);
            stmt.registerOutParameter(2, Types.VARCHAR);
            stmt.execute();
            return new Resposta(
                    Status.OK,
                    stmt.getString(2)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void onStart(@Observes StartupEvent evt) {
        this.buscar(this.cacheClientes.stream().findFirst().get());
        log.warn("Extrato warn up!");
    }
}
