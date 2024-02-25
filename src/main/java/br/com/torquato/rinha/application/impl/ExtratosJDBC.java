package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.Clientes;
import br.com.torquato.rinha.application.Extratos;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;

import javax.sql.DataSource;
import java.sql.Types;

@Slf4j
@Startup
@ApplicationScoped
public class ExtratosJDBC implements Extratos {

    @Inject
    DataSource dataSource;

    @Inject
    Clientes clientes;

    @Override
    public Resposta buscar(final int idCliente) {
        if (!this.clientes.existe(idCliente)) {
            return CLIENTE_INVALIDO;
        }

        try (final var connection = this.dataSource.getConnection();
             final var stmt = connection.prepareCall("call rinha.retorna_extrato(?,?)")) {
            stmt.setInt(1, idCliente);
            stmt.registerOutParameter(2, Types.OTHER);
            stmt.execute();
            PGobject pgObj = (PGobject) stmt.getObject(2);
            return new Resposta(pgObj.getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
