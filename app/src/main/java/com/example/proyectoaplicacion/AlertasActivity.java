package com.example.proyectoaplicacion;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertasActivity extends AppCompatActivity {

    ListView lvAlertas;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);

        db = new DatabaseHelper(this);
        lvAlertas = findViewById(R.id.lvAlertas);

        cargarAlertas();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_alertas);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(AlertasActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_inventario) {
                startActivity(new Intent(AlertasActivity.this, InventarioActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_registrar) {
                startActivity(new Intent(AlertasActivity.this, RegistroActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_alertas) {
                return true;
            }
            return false;
        });
    }

    private void cargarAlertas() {
        Cursor cursor = db.getAllProducts();
        List<Producto> listaAlertas = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calHoy = Calendar.getInstance();
        calHoy.set(Calendar.HOUR_OF_DAY, 0); calHoy.set(Calendar.MINUTE, 0); calHoy.set(Calendar.SECOND, 0); calHoy.set(Calendar.MILLISECOND, 0);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String fechaCaducidad = cursor.getString(4);
                int cantidad = cursor.getInt(6); // Leemos el stock actual
                long diasRestantes = 999;

                if (fechaCaducidad != null && !fechaCaducidad.equals("00.00.0000") && !fechaCaducidad.isEmpty()) {
                    try {
                        Date fecha = sdf.parse(fechaCaducidad);
                        if (fecha != null) {
                            long diffMillis = fecha.getTime() - calHoy.getTimeInMillis();
                            diasRestantes = diffMillis / (1000 * 60 * 60 * 24);
                        }
                    } catch (ParseException e) { e.printStackTrace(); }
                }

                // --- LÓGICA DE ALERTAS (Vencimiento O Stock Bajo) ---
                // Generamos alerta si vence en 7 días o menos, O si quedan 5 unidades o menos
                if (diasRestantes <= 7 || cantidad <= 5) {
                    String estado = "OK";

                    // Prioridad 1: Vencimiento
                    if (diasRestantes <= 3) {
                        estado = "CRITICO";
                    } else if (diasRestantes <= 7) {
                        estado = "PROXIMO";
                    }
                    // Prioridad 2: Si no está por vencer, pero casi no hay stock
                    else if (cantidad <= 5) {
                        estado = "STOCK_BAJO";
                    }

                    listaAlertas.add(new Producto(
                            cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                            cursor.getString(3), fechaCaducidad, cursor.getString(5),
                            cantidad, diasRestantes, estado
                    ));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        AlertaAdapter adapter = new AlertaAdapter(this, listaAlertas);
        lvAlertas.setAdapter(adapter);
    }
}