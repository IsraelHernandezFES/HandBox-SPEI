package com.sandbox.spei.Validation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Representa un solo error  individual
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemErrorResponse {
    private String codigo;
    private String campo;
    private String mensaje;
}
//genera este tipo de bloque JSON
//{
//  "codigo": "PRX-005",
//  "campo": "monto",
//  "mensaje": "El importe excede 1,000,000.00 o tiene mas de dos decimales"
//}