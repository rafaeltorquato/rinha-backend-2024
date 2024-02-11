package br.com.torquato.api;

import br.com.torquato.api.data.Extrato;
import br.com.torquato.api.data.Saldo;
import br.com.torquato.api.data.TransacaoPendente;
import br.com.torquato.repository.TransacaoRepository;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Set;

@Path("/clientes")
public class ClientesResource {

    public static final RestResponse<Saldo> STATUS_422_SALDO = RestResponse.status(422);
    public static final RestResponse<Saldo> STATUS_404_SALDO = RestResponse.status(RestResponse.Status.NOT_FOUND.getStatusCode());

    public static final RestResponse<Extrato> STATUS_404_EXTRATO = RestResponse.status(RestResponse.Status.NOT_FOUND.getStatusCode());
    @Inject
    TransacaoRepository transacaoRepository;
    @Inject
    Set<Integer> cacheClientes;

    @GET
    @Path(("/{id}/extrato"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public RestResponse<Extrato> extratoPorCliente(@PathParam("id") final int id) {
        if (!this.cacheClientes.contains(id)) {
            return STATUS_404_EXTRATO;
        }
        return RestResponse.ok(transacaoRepository.extratoPorCliente(id));
    }

    @POST
    @Path(("/{id}/transacoes"))
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public RestResponse<Saldo> processar(@PathParam("id") final int id, final TransacaoPendente transacaoPendente) {
        if (!this.cacheClientes.contains(id)) {
            return STATUS_404_SALDO;
        }
        if (!transacaoPendente.isValida()) {
            return STATUS_422_SALDO;
        }
        final Saldo saldo = this.transacaoRepository.salvarTransacao(id, transacaoPendente);
        if (saldo == null) {
            return STATUS_422_SALDO;
        }
        return RestResponse.ok(saldo);
    }
}
