package com.lamasia.applamasia;

public class Pago {
    private String mes;
    private String estado;
    private int monto;

    public Pago() {}

    public Pago(String mes, String estado, int monto) {
        this.mes = mes;
        this.estado = estado;
        this.monto = monto;
    }

    public String getMes() {
        return mes;
    }

    public String getEstado() {
        return estado;
    }

    public int getMonto() {
        return monto;
    }
}
