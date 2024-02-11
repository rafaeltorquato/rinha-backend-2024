package br.com.torquato.rinha.delivery.rest.error;

import io.vertx.ext.web.handler.HttpException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;

@Produces
@Slf4j
public class CustomExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable throwable) {
        return switch (throwable) {
            case HttpException httpException -> Response.status(422).build();
            default -> {
                throwable.printStackTrace();
                log.error(throwable.getMessage(), throwable);
                yield Response.serverError().build();
            }
        };
    }
}
