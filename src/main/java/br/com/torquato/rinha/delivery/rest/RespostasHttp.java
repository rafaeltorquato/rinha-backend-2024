package br.com.torquato.rinha.delivery.rest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RespostasHttp {

    public static final RestResponse<String> STATUS_422 = RestResponse.status(
            422
    );
    public static final RestResponse<String> STATUS_404 = RestResponse.status(
            RestResponse.Status.NOT_FOUND.getStatusCode()
    );
}
