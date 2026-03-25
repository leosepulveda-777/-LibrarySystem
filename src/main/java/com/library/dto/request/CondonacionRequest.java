package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CondonacionRequest {

    @NotBlank(message = "El motivo de condonación es obligatorio")
    private String motivoCondonacion;
}
