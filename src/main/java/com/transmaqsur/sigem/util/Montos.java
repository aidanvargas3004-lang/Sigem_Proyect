package com.transmaqsur.sigem.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cálculos monetarios. Los precios se registran sin IGV. */
public final class Montos {

    public static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    private Montos() {
    }

    public static BigDecimal redondear(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiplicar(BigDecimal cantidad, BigDecimal precio) {
        return redondear(cantidad.multiply(precio));
    }

    public static BigDecimal igv(BigDecimal subtotal) {
        return redondear(subtotal.multiply(TASA_IGV));
    }

    public static BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
