package br.com.torquato.rinha.delivery.rest;

import br.com.torquato.rinha.domain.model.ExtratoCliente;
import br.com.torquato.rinha.domain.model.SaldoPosTransacao;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RespostasHttp {

    public static final RestResponse<SaldoPosTransacao> STATUS_422_SALDO = RestResponse.status(
            422
    );
    public static final RestResponse<SaldoPosTransacao> STATUS_404_SALDO = RestResponse.status(
            RestResponse.Status.NOT_FOUND.getStatusCode()
    );
    public static final RestResponse<ExtratoCliente> STATUS_404_EXTRATO = RestResponse.status(
            RestResponse.Status.NOT_FOUND.getStatusCode()
    );
}
