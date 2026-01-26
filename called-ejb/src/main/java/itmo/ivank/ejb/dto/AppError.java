package itmo.ivank.ejb.dto;

import java.io.Serializable;

public record AppError(
        Integer code,
        String message
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
