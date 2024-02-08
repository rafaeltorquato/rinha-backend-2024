package br.com.torquato;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class CacheClientesProducer {

    @Inject
    DataSource dataSource;

    @Produces
    public Set<Integer> clientesCache() {
        final HashSet<Integer> set;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("select id from rinha.cliente");
            ResultSet resultSet = preparedStatement.executeQuery();
            set = new HashSet<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                set.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.safeClose(preparedStatement);
            JdbcUtil.safeClose(connection);
        }
        return set;
    }
}
