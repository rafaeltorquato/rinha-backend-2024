package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.Extratos;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;

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
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Resposta buscar(final int idCliente) {
        if (!this.cacheClientes.contains(idCliente)) {
            return CLIENTE_INVALIDO;
        }

        try (final var connection = this.dataSource.getConnection();
             final var stmt = connection.prepareCall("call rinha.retorna_extrato(?,?)")) {
            stmt.setInt(1, idCliente);
            stmt.registerOutParameter(2, Types.OTHER);
            stmt.execute();
            return new Resposta(((PGobject)stmt.getObject(2)).getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    void onStart(@Observes final StartupEvent evt) {
//        this.buscar(this.cacheClientes.stream().findFirst().get());
//        log.warn("Extrato warm up!");
//    }
}
