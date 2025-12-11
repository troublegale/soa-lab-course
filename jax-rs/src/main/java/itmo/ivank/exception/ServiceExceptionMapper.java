package itmo.ivank.exception;

import itmo.ivank.dto.AppError;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    @Override
    public Response toResponse(ServiceException e) {
        return Response.status(500)
                .entity(new AppError(500, e.getMessage()))
                .type(MediaType.APPLICATION_XML)
                .build();
    }
}
