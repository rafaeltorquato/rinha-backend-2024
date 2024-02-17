package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.Transacoes;
import br.com.torquato.rinha.domain.model.TransacaoPendente;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Set;

@Startup
@Slf4j
@ApplicationScoped
public class TransacoesJDBC implements Transacoes {

    @Inject
    DataSource dataSource;

    @Inject
    Set<Integer> cacheClientes;

    @Override
    public Resposta processar(final Solicitacao solicitacao) {
        if (!this.cacheClientes.contains(solicitacao.idCliente())) {
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
            stmt.registerOutParameter(5, Types.VARBINARY); //saldo
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

    void onStart(@Observes final StartupEvent evt) {
        this.processar(new Solicitacao(
                this.cacheClientes.stream().findFirst().get(),
                new TransacaoPendente(Integer.MAX_VALUE, "d", "abc"))
        );
        log.warn("Transacao warm up!");
    }
}
