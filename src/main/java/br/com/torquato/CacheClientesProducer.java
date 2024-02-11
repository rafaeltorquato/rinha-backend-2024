package br.com.torquato;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class CacheClientesProducer {

    @Inject
    DataSource dataSource;

    @Produces
    public Set<Integer> clientesCache() {
        try (final var connection = dataSource.getConnection();
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
