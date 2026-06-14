package com.example.proyectoaplicacion;

public class Producto {
    int id;
    String nombre, categoria, lote, fechaCaducidad, ubicacion;
    int cantidad;
    long diasRestantes;
    String estado;

    public Producto(int id, String nombre, String categoria, String lote, String fechaCaducidad, String ubicacion, int cantidad, long diasRestantes, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.lote = lote;
        this.fechaCaducidad = fechaCaducidad;
        this.ubicacion = ubicacion;
        this.cantidad = cantidad;
        this.diasRestantes = diasRestantes;
        this.estado = estado;
    }
}