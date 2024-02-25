package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.Clientes;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
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
public class ClientesJDBC implements Clientes {

    @Inject
    DataSource dataSource;
    Set<Integer> idsClientes;

    @PostConstruct
    void carregarClientes() {
        psqlPreWarm();
        carregar();
    }

    private void psqlPreWarm() {
        try (final var connection = this.dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement("select pg_prewarm('rinha.cliente')");
             final var resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void carregar() {
        try (final var connection = this.dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement("select id from rinha.cliente");
             final var resultSet = preparedStatement.executeQuery()) {

            this.idsClientes = new HashSet<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                this.idsClientes.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public boolean existe(int idCliente) {
        return this.idsClientes.contains(idCliente);
    }
}
