package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.ZipUtil;
import br.com.torquato.rinha.application.Extratos;
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
public class ExtratosJDBC implements Extratos {

    @Inject
    DataSource dataSource;

    @Inject
    Set<Integer> cacheClientes;

    @Override
    public Resposta buscar(final int idCliente) {
        if (!this.cacheClientes.contains(idCliente)) {
            return CLIENTE_INVALIDO;
        }

        try (final var connection = this.dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.retorna_extrato(?,?)}")) {
            stmt.setInt(1, idCliente);
            stmt.registerOutParameter(2, Types.VARBINARY);
            stmt.execute();
            return new Resposta(ZipUtil.decompressSQLCompression(stmt.getBytes(2)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void onStart(@Observes StartupEvent evt) {
        this.buscar(this.cacheClientes.stream().findFirst().get());
        log.warn("Extrato warn up!");
    }
}
