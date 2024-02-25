package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.Clientes;
import br.com.torquato.rinha.application.Transacoes;
import br.com.torquato.rinha.domain.model.TransacaoPendente;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Types;

@Startup
@Slf4j
@ApplicationScoped
public class TransacoesJDBC implements Transacoes {

    @Inject
    DataSource dataSource;

    @Inject
    Clientes clientes;

    @Override
    public Resposta processar(final Solicitacao solicitacao) {
        if (!this.clientes.existe(solicitacao.idCliente())) {
            return CLIENTE_INVALIDO;
        }
        final TransacaoPendente transacaoPendente = solicitacao.transacaoPendente();
        if (!transacaoPendente.isValida()) {
            return TRANSACAO_INVALIDA;
        }
        try (final var connection = this.dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.processa_transacao(?,?,?,?,?)}")) {
            stmt.setInt(1, solicitacao.idCliente());
            stmt.setInt(2, (int) transacaoPendente.valor());
            stmt.setString(3, transacaoPendente.descricao());
            stmt.setString(4, transacaoPendente.tipo());
            stmt.registerOutParameter(5, Types.VARBINARY);
            stmt.execute();
            final String saldo = stmt.getString(5);
            if (saldo != null) {
                return new Resposta(saldo);
            }
            return SEM_SALDO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
