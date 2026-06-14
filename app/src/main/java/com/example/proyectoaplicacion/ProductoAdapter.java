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

public class ProductoAdapter extends ArrayAdapter<Producto> {

    public ProductoAdapter(@NonNull Context context, @NonNull List<Producto> productos) {
        super(context, 0, productos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Producto producto = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_producto, parent, false);
        }

        if (producto != null) {
            ImageView ivEstadoColor = convertView.findViewById(R.id.ivEstadoColor);
            TextView tvNombreProd = convertView.findViewById(R.id.tvNombreProd);
            TextView tvDetalleProd = convertView.findViewById(R.id.tvDetalleProd);
            TextView tvBadgeVencimiento = convertView.findViewById(R.id.tvBadgeVencimiento);

            tvNombreProd.setText(producto.nombre);
            tvDetalleProd.setText(String.format(Locale.getDefault(), "%s • %s", producto.categoria, producto.ubicacion));

            // Aplicar colores y textos según el estado y el stock
            if ("CRITICO".equals(producto.estado)) {
                ivEstadoColor.setColorFilter(Color.parseColor("#EF5350")); // Rojo
                tvBadgeVencimiento.setBackgroundColor(Color.parseColor("#FFCDD2"));
                tvBadgeVencimiento.setTextColor(Color.parseColor("#B71C1C"));

                // --- NUEVA LÓGICA: SEPARACIÓN DE VENCIDO Y VENCE HOY ---
                if (producto.diasRestantes < 0) {
                    tvBadgeVencimiento.setText("VENCIDO");
                } else if (producto.diasRestantes == 0) {
                    tvBadgeVencimiento.setText("VENCE HOY");
                } else {
                    tvBadgeVencimiento.setText(String.format(Locale.getDefault(), "%d día%s", producto.diasRestantes, producto.diasRestantes > 1 ? "s" : ""));
                }
                // -------------------------------------------------------

            } else if ("PROXIMO".equals(producto.estado)) {
                ivEstadoColor.setColorFilter(Color.parseColor("#FFA726")); // Naranja
                tvBadgeVencimiento.setText(String.format(Locale.getDefault(), "%d días", producto.diasRestantes));
                tvBadgeVencimiento.setBackgroundColor(Color.parseColor("#FFF9C4"));
                tvBadgeVencimiento.setTextColor(Color.parseColor("#F57F17"));

            } else if ("STOCK_BAJO".equals(producto.estado) || producto.cantidad <= 5) {
                // Diseño AZUL para control de stock
                ivEstadoColor.setColorFilter(Color.parseColor("#29B6F6"));
                tvBadgeVencimiento.setText(String.format(Locale.getDefault(), "Stock: %d", producto.cantidad));
                tvBadgeVencimiento.setBackgroundColor(Color.parseColor("#E1F5FE"));
                tvBadgeVencimiento.setTextColor(Color.parseColor("#0277BD"));

            } else if ("00.00.0000".equals(producto.fechaCaducidad)) {
                ivEstadoColor.setColorFilter(Color.parseColor("#66BB6A")); // Verde
                tvBadgeVencimiento.setText("No perecedero");
                tvBadgeVencimiento.setBackgroundColor(Color.parseColor("#1B5E20"));
                tvBadgeVencimiento.setTextColor(Color.parseColor("#A5D6A7"));
            } else {
                ivEstadoColor.setColorFilter(Color.parseColor("#66BB6A")); // Verde
                tvBadgeVencimiento.setText("En buen estado");
                tvBadgeVencimiento.setBackgroundColor(Color.parseColor("#C8E6C9"));
                tvBadgeVencimiento.setTextColor(Color.parseColor("#2E7D32"));
            }
        }

        return convertView;
    }
}