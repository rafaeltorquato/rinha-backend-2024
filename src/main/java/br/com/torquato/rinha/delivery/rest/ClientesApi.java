package br.com.torquato.rinha.delivery.rest;

import br.com.torquato.rinha.application.Extratos;
import br.com.torquato.rinha.application.Transacoes;
import br.com.torquato.rinha.domain.model.TransacaoPendente;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
    private static final RestResponse<String> STATUS_422 = new RestResponseBuilderImpl<String>()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .status(422)
            .build();
    private static final RestResponse<String> STATUS_404 = new RestResponseBuilderImpl<String>()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .status(NOT_FOUND.getStatusCode())
            .build();

    @Inject
    Transacoes transacoes;

    @Inject
    Extratos extratos;

    @GET
    @Path(("/{id}/extrato"))
    @RunOnVirtualThread
    public RestResponse<String> getExtrato(@PathParam("id") final int id) {
        final var resposta = this.extratos.buscar(id);

        return switch (resposta.status()) {
            case OK -> {
                final String json = resposta.extratoCliente();
                final byte[] bytes = json.getBytes();
                yield new RestResponseBuilderImpl<String>()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_LENGTH, bytes.length)
                        .entity(json)
                        .build();
            }
            case CLIENTE_INVALIDO -> STATUS_404;
        };
    }

    @POST
    @Path(("/{id}/transacoes"))
    @RunOnVirtualThread
    public RestResponse<String> postTransacao(@PathParam("id") final int id,
                                              final TransacaoPendente transacaoPendente) {
        final var solicitacao = new Transacoes.Solicitacao(id, transacaoPendente);
        final var resposta = this.transacoes.processar(solicitacao);
        return switch (resposta.status()) {
            case OK -> {
                final String json = resposta.saldo();
                final byte[] bytes = json.getBytes();
                yield new RestResponseBuilderImpl<String>()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_LENGTH, bytes.length)
                        .entity(json)
                        .build();
            }
            case SEM_SALDO, TRANSACAO_INVALIDA -> STATUS_422;
            case CLIENTE_INVALIDO -> STATUS_404;
        };
    }
}
