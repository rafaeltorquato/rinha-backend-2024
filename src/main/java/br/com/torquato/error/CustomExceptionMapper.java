package br.com.torquato.error;

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
        log.error(throwable.getMessage(), throwable);
        return switch (throwable) {
            case HttpException httpException -> Response.status(422).entity(httpException.getMessage()).build();
            default -> Response.serverError().build();
        };
    }
}
