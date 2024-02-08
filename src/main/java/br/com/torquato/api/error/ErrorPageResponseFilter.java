package br.com.torquato.api.error;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(9999)
public class ErrorPageResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        int status = responseContext.getStatus();
        if (status ==  Response.Status.BAD_REQUEST.getStatusCode()) {
            responseContext.setStatus(422);
        }
    }
}