package br.com.torquato.api;

import br.com.torquato.api.data.Extrato;
import br.com.torquato.api.data.Saldo;
import br.com.torquato.api.data.TransacaoPendente;
import br.com.torquato.repository.TransacaoRepository;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Set;

@Path("/clientes")
public class ClientesResource {

    @Inject
    TransacaoRepository transacaoRepository;
    @Inject
    Set<Integer> cacheClientes;

    @GET
    @Path(("/{id}/extrato"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Extrato extratoPorCliente(@PathParam("id") final int id) {
        if (!this.cacheClientes.contains(id)) {
            throw new HttpException(Response.Status.NOT_FOUND.getStatusCode());
        }
        return transacaoRepository.extratoPorCliente(id);
    }

    @POST
    @Path(("/{id}/transacoes"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Saldo processar(@PathParam("id") final int id, final TransacaoPendente transacaoPendente) {
        if (!this.cacheClientes.contains(id)) {
            throw new HttpException(Response.Status.NOT_FOUND.getStatusCode());
        }
        if (!transacaoPendente.isValida()) {
            throw new HttpException(422);
        }
        return this.transacaoRepository.salvarTransacao(id, transacaoPendente);
    }
}
