package itmo.ivank.web.exception;

import itmo.ivank.ejb.exception.InvalidSearchQueryException;
import itmo.ivank.ejb.exception.NotFoundException;
import itmo.ivank.web.dto.AppErrorDto;
import jakarta.ejb.EJBException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        logger.log(Level.SEVERE, "Exception caught: " + exception.getClass().getName() + ": " + exception.getMessage(), exception);
        
        // Unwrap EJBException
        Throwable cause = exception;
        if (exception instanceof EJBException && exception.getCause() != null) {
            cause = exception.getCause();
            logger.log(Level.SEVERE, "Unwrapped EJBException to: " + cause.getClass().getName() + ": " + cause.getMessage(), cause);
        }

        if (cause instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new AppErrorDto(404, cause.getMessage()))
                    .type(MediaType.APPLICATION_XML)
                    .build();
        }

        if (cause instanceof InvalidSearchQueryException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AppErrorDto(400, cause.getMessage()))
                    .type(MediaType.APPLICATION_XML)
                    .build();
        }

        if (cause instanceof ConstraintViolationException cve) {
            StringBuilder message = new StringBuilder();
            cve.getConstraintViolations().forEach(cv ->
                    message.append(cv.getPropertyPath()).append(" ").append(cv.getMessage()).append("\n"));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AppErrorDto(400, message.toString().trim()))
                    .type(MediaType.APPLICATION_XML)
                    .build();
        }

        if (cause instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AppErrorDto(400, cause.getMessage()))
                    .type(MediaType.APPLICATION_XML)
                    .build();
        }

        // Default error
        String errorMessage = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new AppErrorDto(500, "Internal server error: " + errorMessage))
                .type(MediaType.APPLICATION_XML)
                .build();
    }
}
