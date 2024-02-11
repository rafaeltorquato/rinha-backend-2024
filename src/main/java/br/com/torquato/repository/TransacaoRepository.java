package br.com.torquato.repository;

import br.com.torquato.api.data.Extrato;
import br.com.torquato.api.data.Saldo;
import br.com.torquato.api.data.TransacaoPendente;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Singleton
public class TransacaoRepository {
    @Inject
    DataSource dataSource;

    public Extrato extratoPorCliente(final int idCliente) {
        try (final var connection = dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.retorna_extrato(?)}")) {
            stmt.setInt(1, idCliente);
            stmt.execute();
            final ResultSet rsSaldo = stmt.getResultSet();
            rsSaldo.next();
            Extrato.Saldo saldo = new Extrato.Saldo(
                    rsSaldo.getInt("limite"),
                    rsSaldo.getInt("saldo"),
                    rsSaldo.getTimestamp("data_extrato").toLocalDateTime()
            );
            List<Extrato.Transacao> transacoes = Collections.emptyList();
            if (stmt.getMoreResults()) {
                ResultSet resultTransacoes = stmt.getResultSet();
                transacoes = new ArrayList<>(resultTransacoes.getFetchSize());
                while (resultTransacoes.next()) {
                    transacoes.add(new Extrato.Transacao(
                            resultTransacoes.getInt("valor"),
                            resultTransacoes.getString("tipo"),
                            resultTransacoes.getString("descricao"),
                            resultTransacoes.getTimestamp("realizada_em").toLocalDateTime()
                    ));
                }

            }
            return new Extrato(saldo, transacoes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Saldo salvarTransacao(int idCliente, TransacaoPendente transacaoPendente) {
        try (final var connection = dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.processa_transacao(?,?,?,?,?,?)}");) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, transacaoPendente.valor());
            stmt.setString(3, transacaoPendente.descricao());
            stmt.setString(4, transacaoPendente.tipo());
            stmt.registerOutParameter(5, Types.VARCHAR); //saldo
            stmt.registerOutParameter(6, Types.VARCHAR); //limite
            stmt.execute();
            int limite = stmt.getInt(5);
            if (limite != 0 || !stmt.wasNull()) {
                return new Saldo(limite, stmt.getInt(6));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


}
