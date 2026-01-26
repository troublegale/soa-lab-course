package itmo.ivank.ejb.dto;

import java.io.Serializable;
import java.util.List;

public record TypeCountResponse(
        List<TypeCount> types
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
