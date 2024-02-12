package br.com.torquato.rinha.application.impl;

import br.com.torquato.rinha.application.ProcessadorTransacoes;
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
public class ProcessadorTransacoesJDBC implements ProcessadorTransacoes {

    private final Resposta transacaoInvalida = new Resposta(
            Status.TRANSACAO_INVALIDA
    );
    private final Resposta semSaldo = new Resposta(
            Status.SEM_SALDO
    );

    private final Resposta clienteInvalido = new Resposta(
            Status.CLIENTE_INVALIDO
    );

    @Inject
    DataSource dataSource;

    @Inject
    Set<Integer> cacheClientes;

    @Override
    public Resposta processar(Solicitacao solicitacao) {
        if (!this.cacheClientes.contains(solicitacao.idCliente())) {
            return this.clienteInvalido;
        }
        final TransacaoPendente transacaoPendente = solicitacao.transacaoPendente();
        if (!transacaoPendente.isValida()) {
            return this.transacaoInvalida;
        }
        try (final var connection = this.dataSource.getConnection();
             final var stmt = connection.prepareCall("{call rinha.processa_transacao(?,?,?,?,?)}");) {
            stmt.setInt(1, solicitacao.idCliente());
            stmt.setInt(2, (int) transacaoPendente.valor());
            stmt.setString(3, transacaoPendente.descricao());
            stmt.setString(4, transacaoPendente.tipo());
            stmt.registerOutParameter(5, Types.VARCHAR); //saldo
            stmt.execute();
            final String saldoJson = stmt.getString(5);
            if (saldoJson != null) {
                return new Resposta(Status.OK, saldoJson);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.semSaldo;
    }

    void onStart(@Observes StartupEvent evt) {
        this.processar(new Solicitacao(
                this.cacheClientes.stream().findFirst().get(),
                new TransacaoPendente(Integer.MAX_VALUE, "d", "abc"))
        );
        log.warn("Transacao warn up!");
    }
}
