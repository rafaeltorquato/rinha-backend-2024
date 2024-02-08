package br.com.torquato;

import br.com.torquato.api.Extrato;
import br.com.torquato.api.Saldo;
import br.com.torquato.api.TransacaoPendente;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Optional;

@Path("/clientes")
public class ClientesResource {

    @Inject
    TransacaoRepository transacaoRepository;

    @GET
    @Path(("/{id}/extrato"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Extrato extratoPorCliente(@PathParam("id") final int id) {
        return transacaoRepository.extratoPorCliente(id);
    }

    @POST
    @Path(("/{id}/transacoes"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public Saldo processar(@PathParam("id") final int id, final TransacaoPendente transacaoPendente) {
        if (!transacaoPendente.isValida()) {
            throw new HttpException(422);
        }
        return transacaoRepository.salvarTransacao(id, transacaoPendente);
    }
}
