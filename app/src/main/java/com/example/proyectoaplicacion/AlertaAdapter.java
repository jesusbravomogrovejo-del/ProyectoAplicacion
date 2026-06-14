package com.example.proyectoaplicacion;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class AlertaAdapter extends ArrayAdapter<Producto> {

    public AlertaAdapter(@NonNull Context context, @NonNull List<Producto> productos) {
        super(context, 0, productos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Producto producto = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_alerta, parent, false);
        }

        if (producto != null) {
            ImageView ivIconoAlerta = convertView.findViewById(R.id.ivIconoAlerta);
            TextView tvTituloAlerta = convertView.findViewById(R.id.tvTituloAlerta);
            TextView tvMensajeAlerta = convertView.findViewById(R.id.tvMensajeAlerta);

            // Armamos el diseño dependiendo de la urgencia
            if ("CRITICO".equals(producto.estado)) {
                ivIconoAlerta.setColorFilter(Color.parseColor("#EF5350")); // Rojo
                tvTituloAlerta.setText("¡Vencimiento Crítico!");
                tvTituloAlerta.setTextColor(Color.parseColor("#EF5350"));
                tvMensajeAlerta.setText(String.format(Locale.getDefault(),
                        "El lote '%s' de %s ha vencido o vence el día de hoy. Retíralo de la ubicación: %s.",
                        producto.lote, producto.nombre, producto.ubicacion));

            } else if ("PROXIMO".equals(producto.estado)) {
                ivIconoAlerta.setColorFilter(Color.parseColor("#FFA726")); // Naranja
                tvTituloAlerta.setText("¡Vencimiento Próximo!");
                tvTituloAlerta.setTextColor(Color.parseColor("#FFA726"));
                tvMensajeAlerta.setText(String.format(Locale.getDefault(),
                        "El lote '%s' de %s vence en %d días. Revisa el estante en %s.",
                        producto.lote, producto.nombre, producto.diasRestantes, producto.ubicacion));

            } else if ("STOCK_BAJO".equals(producto.estado)) {
                ivIconoAlerta.setColorFilter(Color.parseColor("#29B6F6")); // Azul
                tvTituloAlerta.setText("¡Stock Crítico!");
                tvTituloAlerta.setTextColor(Color.parseColor("#29B6F6"));
                tvMensajeAlerta.setText(String.format(Locale.getDefault(),
                        "Solo quedan %d unidades de %s. Se requiere reposición urgente en %s.",
                        producto.cantidad, producto.nombre, producto.ubicacion));
            }
        }

        return convertView;
    }
}