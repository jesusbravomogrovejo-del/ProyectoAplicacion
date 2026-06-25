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
        android.content.SharedPreferences prefsSession = getSharedPreferences("MiniMarketPrefs", MODE_PRIVATE);
        int currentUserId = prefsSession.getInt("usuario_actual_id", -1);
        Cursor cursor = db.getAllProducts(currentUserId);

        List<Producto> listaAlertas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calHoy = Calendar.getInstance();
        calHoy.set(Calendar.HOUR_OF_DAY, 0); calHoy.set(Calendar.MINUTE, 0); calHoy.set(Calendar.SECOND, 0); calHoy.set(Calendar.MILLISECOND, 0);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String fechaCaducidad = cursor.getString(5);
                int cantidad = cursor.getInt(7);
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

                if (diasRestantes <= 7 || cantidad <= 5) {
                    String estado = "OK";
                    if (diasRestantes <= 3) {
                        estado = "CRITICO";
                    } else if (diasRestantes <= 7) {
                        estado = "PROXIMO";
                    } else if (cantidad <= 5) {
                        estado = "STOCK_BAJO";
                    }
                    listaAlertas.add(new Producto(
                            cursor.getInt(0), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), fechaCaducidad, cursor.getString(6),
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