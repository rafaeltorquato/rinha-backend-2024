package br.com.torquato.repository;

import br.com.torquato.JdbcUtil;
import br.com.torquato.api.data.Extrato;
import br.com.torquato.api.data.Saldo;
import br.com.torquato.api.data.TipoTransacao;
import br.com.torquato.api.data.TransacaoPendente;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
                c.id = ? and (t.realizada_em is null or t.realizada_em <= ?)
            order by t.realizada_em desc limit 10
            """;

    public Extrato extratoPorCliente(final int idCliente) {
        final LocalDateTime dataHora = LocalDateTime.now();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(SQL_EXTRATO);
            preparedStatement.setInt(1, idCliente);

            preparedStatement.setTimestamp(2, Timestamp.valueOf(dataHora));
            resultSet = preparedStatement.executeQuery();
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
                        Math.abs(valor),
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
            JdbcUtil.safeClose(resultSet);
            JdbcUtil.safeClose(preparedStatement);
            JdbcUtil.safeClose(connection);
        }
    }

    public Saldo salvarTransacao(int idCliente, TransacaoPendente transacaoPendente) {

        final int valor = switch (transacaoPendente.tipo()) {
            case c -> transacaoPendente.valor();
            case d -> transacaoPendente.valor() * -1;
        };

        Connection connection = null;
        CallableStatement stmt = null;
        Saldo saldo = null;
        try {
            connection = dataSource.getConnection();
            stmt = connection.prepareCall("{call rinha.processa_transacao(?,?,?,?,?)}");

            stmt.setInt(1, idCliente);
            stmt.setInt(2, valor);
            stmt.setString(3, transacaoPendente.descricao());
            stmt.registerOutParameter(4, Types.INTEGER); //limite
            stmt.registerOutParameter(5, Types.INTEGER); //saldo
            stmt.execute();
            final int limite = stmt.getInt(4);
            if(limite != 0 || !stmt.wasNull()) {
                saldo = new Saldo(limite, stmt.getInt(5));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            JdbcUtil.safeClose(stmt);
            JdbcUtil.safeClose(connection);
        }
        return saldo;
    }


}
