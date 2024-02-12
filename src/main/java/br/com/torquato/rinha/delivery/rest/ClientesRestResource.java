package br.com.torquato.rinha.delivery.rest;

import br.com.torquato.rinha.application.ExecutorBuscaExtrato;
import br.com.torquato.rinha.application.ProcessadorTransacoes;
import br.com.torquato.rinha.domain.model.TransacaoPendente;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;

@Slf4j
@Startup
@Path("/clientes")
public class ClientesRestResource {

    @Inject
    ProcessadorTransacoes processadorTransacoes;

    @Inject
    ExecutorBuscaExtrato executorBuscaExtrato;

    @GET
    @Path(("/{id}/extrato"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public RestResponse<String> getExtrato(@PathParam("id") final int id) {
        final var resposta = executorBuscaExtrato.buscar(id);
        return switch (resposta.status()) {
            case OK -> RestResponse.ok(resposta.extratoCliente());
            case CLIENTE_INVALIDO -> RespostasHttp.STATUS_404;
        };
    }

    @POST
    @Path(("/{id}/transacoes"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public RestResponse<String> postTransacao(@PathParam("id") final int id,
                                              final TransacaoPendente transacaoPendente) {
        final var solicitacao = new ProcessadorTransacoes.Solicitacao(id, transacaoPendente);
        final var resposta = processadorTransacoes.processar(solicitacao);
        return switch (resposta.status()) {
            case OK -> RestResponse.ok(resposta.saldo());
            case SEM_SALDO, TRANSACAO_INVALIDA -> RespostasHttp.STATUS_422;
            case CLIENTE_INVALIDO -> RespostasHttp.STATUS_404;
        };
    }
}
