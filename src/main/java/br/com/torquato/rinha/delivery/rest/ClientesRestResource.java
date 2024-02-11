package br.com.torquato.rinha.delivery.rest;

import br.com.torquato.rinha.application.ExecutorBuscaExtrato;
import br.com.torquato.rinha.application.ProcessadorTransacoes;
import br.com.torquato.rinha.domain.model.ExtratoCliente;
import br.com.torquato.rinha.domain.model.SaldoPosTransacao;
import br.com.torquato.rinha.domain.model.TransacaoPendente;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

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
    public RestResponse<ExtratoCliente> getExtrato(@PathParam("id") final int id) {
        final var resposta = executorBuscaExtrato.buscar(id);
        return switch (resposta.status()) {
            case OK -> RestResponse.ok(resposta.extratoCliente());
            case CLIENTE_INVALIDO -> RespostasHttp.STATUS_404_EXTRATO;
        };
    }

    @POST
    @Path(("/{id}/transacoes"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public RestResponse<SaldoPosTransacao> postTransacao(@PathParam("id") final int id,
                                                         final TransacaoPendente transacaoPendente) {
        final var solicitacao = new ProcessadorTransacoes.Solicitacao(id, transacaoPendente);
        final var resposta = processadorTransacoes.processar(solicitacao);
        return switch (resposta.status()) {
            case OK -> RestResponse.ok(resposta.saldo());
            case SEM_SALDO, TRANSACAO_INVALIDA -> RespostasHttp.STATUS_422_SALDO;
            case CLIENTE_INVALIDO -> RespostasHttp.STATUS_404_SALDO;
        };
    }
}
