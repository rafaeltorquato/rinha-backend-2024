package br.com.torquato.rinha.application.impl;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Basedo no requisito da Rinha, o número de clientes é fixo e conhecido.
 */
@Startup
@ApplicationScoped
public class CacheProducerJDBC {

    @Inject
    DataSource dataSource;

    @Produces
    @ApplicationScoped
    public Set<Integer> clientesCache() {
        try (final var connection = this.dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement("select id from rinha.cliente");
             final var resultSet = preparedStatement.executeQuery()) {

            final HashSet<Integer> set = new HashSet<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                set.add(resultSet.getInt(1));
            }
            return set;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
