package br.com.torquato;

import br.com.torquato.api.Extrato;
import br.com.torquato.api.Saldo;
import br.com.torquato.api.TipoTransacao;
import br.com.torquato.api.TransacaoPendente;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Singleton
public class TransacaoRepository {

    @Inject
    DataSource dataSource;
    private static final String SQL_EXTRATO = """
            select
                c.saldo,
                c.limite,
                t.valor,
                t.descricao,
                t.realizada_em
            from rinha.cliente c
                left join rinha.transacao t on c.id = t.id_cliente
            where
                c.id = ? and
                (t.realizada_em <= ? or t.realizada_em is null)
            order by t.realizada_em desc limit 10
            """;

    public Extrato extratoPorCliente(final int idCliente) {
        final LocalDateTime dataHora = LocalDateTime.now();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(SQL_EXTRATO);
            preparedStatement.setInt(1, idCliente);

            preparedStatement.setTimestamp(2, Timestamp.valueOf(dataHora));
            ResultSet resultSet = preparedStatement.executeQuery();
            Extrato.Saldo saldo = null;
            final List<Extrato.Transacao> transacoes = new ArrayList<>(resultSet.getFetchSize());
            while (resultSet.next()) {
                final int valorSaldo = resultSet.getInt("saldo");
                final int valorLimite = resultSet.getInt("limite");
                if (saldo == null) {
                    saldo = new Extrato.Saldo(valorLimite, valorSaldo, dataHora);
                }
                final Timestamp realizadaEm = resultSet.getTimestamp("realizada_em");
                if (realizadaEm == null) break;

                final int valor = resultSet.getInt("valor");
                final String descricao = resultSet.getString("descricao");
                final Extrato.Transacao transacao = new Extrato.Transacao(
                        valor,
                        valor > 0 ? TipoTransacao.c : TipoTransacao.d,
                        descricao,
                        dataHora
                );
                transacoes.add(transacao);

            }
            return new Extrato(saldo, transacoes);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    public Saldo salvarTransacao(int idCliente, TransacaoPendente transacaoPendente) {

        final int valor = switch (transacaoPendente.tipo()) {
            case c -> transacaoPendente.valor();
            case d -> transacaoPendente.valor() * -1;
        };

        Connection connection = null;
        CallableStatement stmt = null;
        Saldo saldo;
        try {
            connection = dataSource.getConnection();
            stmt = connection.prepareCall("{call rinha.processa_transacao(?,?,?,?,?)}");

            stmt.setInt(1, idCliente);
            stmt.setInt(2, valor);
            stmt.setString(3, transacaoPendente.descricao());
            stmt.registerOutParameter(4, Types.INTEGER); //limite
            stmt.registerOutParameter(5, Types.INTEGER); //saldo
            stmt.execute();
            saldo = new Saldo(stmt.getInt(4), stmt.getInt(5));
        } catch (Exception e) {
            if (e.getMessage().contains("invalido")) {
                throw new HttpException(422);
            }
            throw new RuntimeException(e);
        } finally {
            safeClose(stmt);
            safeClose(connection);
        }
        return saldo;
    }

    private void safeClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void safeClose(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
