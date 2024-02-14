package br.com.torquato.rinha.delivery.rest;

import br.com.torquato.rinha.application.Extratos;
import br.com.torquato.rinha.application.Transacoes;
import br.com.torquato.rinha.domain.model.TransacaoPendente;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.jaxrs.RestResponseBuilderImpl;

import static org.jboss.resteasy.reactive.RestResponse.Status.NOT_FOUND;

@Slf4j
@Startup
@Path("/clientes")
public class ClientesApi {
    private static final RestResponse<byte[]> STATUS_422 = RestResponse.status(
            422
    );
    private static final RestResponse<byte[]> STATUS_404 = RestResponse.status(
            NOT_FOUND.getStatusCode()
    );

    @Inject
    Transacoes transacoes;

    @Inject
    Extratos extratos;

    @GET
    @Path(("/{id}/extrato"))
    @RunOnVirtualThread
    public RestResponse<byte[]> getExtrato(@PathParam("id") final int id) {
        final var resposta = this.extratos.buscar(id);

        return switch (resposta.status()) {
            case OK -> {
                byte[] bytes = resposta.extratoCliente();
                yield new RestResponseBuilderImpl<byte[]>()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_LENGTH, bytes.length)
                        .encoding("deflate")
                        .entity(bytes)
                        .build();
            }
            case CLIENTE_INVALIDO -> STATUS_404;
        };
    }

    @POST
    @Path(("/{id}/transacoes"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public RestResponse<byte[]> postTransacao(@PathParam("id") final int id,
                                              final TransacaoPendente transacaoPendente) {
        final var solicitacao = new Transacoes.Solicitacao(id, transacaoPendente);
        final var resposta = this.transacoes.processar(solicitacao);
        return switch (resposta.status()) {
            case OK -> {
                byte[] bytes = resposta.saldo();
                yield new RestResponseBuilderImpl<byte[]>()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_LENGTH, bytes.length)
                        .encoding("deflate")
                        .entity(bytes)
                        .build();
            }
            case SEM_SALDO, TRANSACAO_INVALIDA -> STATUS_422;
            case CLIENTE_INVALIDO -> STATUS_404;
        };
    }
}
