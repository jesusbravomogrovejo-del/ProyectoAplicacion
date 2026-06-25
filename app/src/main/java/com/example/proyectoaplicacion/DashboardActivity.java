package com.example.proyectoaplicacion;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvVencenHoy, tvVencenSemana, tvStockTotal;
    private EditText etBuscarProducto;
    private ListView lvProductos;
    private PieChartView pieChart;
    private DatabaseHelper db;
    private final List<Producto> listaTodos = new ArrayList<>();
    private final List<Producto> listaCriticos = new ArrayList<>();
    private ProductoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        db = new DatabaseHelper(this);
        tvVencenHoy = findViewById(R.id.tvVencenHoy);
        tvVencenSemana = findViewById(R.id.tvVencenSemana);
        tvStockTotal = findViewById(R.id.tvStockTotal);
        lvProductos = findViewById(R.id.lvProductos);
        etBuscarProducto = findViewById(R.id.etBuscarProducto);
        pieChart = findViewById(R.id.pieChart);

        ImageView ivCerrarSesion = findViewById(R.id.ivCerrarSesion);
        if (ivCerrarSesion != null) {
            ivCerrarSesion.setOnClickListener(v -> {
                Toast.makeText(DashboardActivity.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
        cargarMetricasYLista();
        lvProductos.setOnItemClickListener((parent, view, position, idItem) -> {
            Producto productoSeleccionado = adapter.getItem(position);
            if (productoSeleccionado == null) return;
            Dialog dialog = new Dialog(DashboardActivity.this);
            dialog.setContentView(R.layout.dialog_detalle_producto);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            TextView tvNombre = dialog.findViewById(R.id.tvDialogNombre);
            TextView tvVencimiento = dialog.findViewById(R.id.tvDialogVencimiento);
            TextView tvDias = dialog.findViewById(R.id.tvDialogDias);
            TextView tvUbicacion = dialog.findViewById(R.id.tvDialogUbicacion);
            TextView tvCategoria = dialog.findViewById(R.id.tvDialogCategoria);
            TextView tvStock = dialog.findViewById(R.id.tvDialogStock);
            Button btnEditar = dialog.findViewById(R.id.btnDialogEditar);
            Button btnEliminar = dialog.findViewById(R.id.btnDialogEliminar);
            tvNombre.setText(productoSeleccionado.nombre);
            tvUbicacion.setText(String.format("Ubicación: %s", productoSeleccionado.ubicacion));
            tvCategoria.setText(String.format("Categoría: %s", productoSeleccionado.categoria));

            if (tvStock != null) {
                tvStock.setText(String.format("Stock en sistema: %d unidades", productoSeleccionado.cantidad));
            }
            if ("00.00.0000".equals(productoSeleccionado.fechaCaducidad)) {
                tvVencimiento.setText("Fecha de vencimiento: No perecedero");
                tvDias.setText("Días restantes: N/A");
            } else {
                tvVencimiento.setText(String.format("Fecha de vencimiento: %s", productoSeleccionado.fechaCaducidad));
                tvDias.setText(String.format("Estado: %d días (%s)", productoSeleccionado.diasRestantes, productoSeleccionado.estado));
            }
            btnEliminar.setOnClickListener(v -> {
                if (db.deleteProduct(productoSeleccionado.id)) {
                    Toast.makeText(DashboardActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    etBuscarProducto.setText("");
                    cargarMetricasYLista();
                }
            });
            btnEditar.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(DashboardActivity.this, RegistroActivity.class);
                intent.putExtra("ID_PRODUCTO", productoSeleccionado.id);
                intent.putExtra("NOMBRE", productoSeleccionado.nombre);
                intent.putExtra("CATEGORIA", productoSeleccionado.categoria);
                intent.putExtra("LOTE", productoSeleccionado.lote);
                intent.putExtra("FECHA", productoSeleccionado.fechaCaducidad);
                intent.putExtra("UBICACION", productoSeleccionado.ubicacion);
                intent.putExtra("CANTIDAD", productoSeleccionado.cantidad);
                startActivity(intent);
            });
            dialog.show();
        });
        etBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarProducto(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_inicio);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int idNav = item.getItemId();
            if (idNav == R.id.nav_inicio) {
                return true;
            } else if (idNav == R.id.nav_inventario) {
                startActivity(new Intent(DashboardActivity.this, InventarioActivity.class));
                finish();
                return true;
            } else if (idNav == R.id.nav_registrar) {
                startActivity(new Intent(DashboardActivity.this, RegistroActivity.class));
                finish();
                return true;
            } else if (idNav == R.id.nav_alertas) {
                startActivity(new Intent(DashboardActivity.this, AlertasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void cargarMetricasYLista() {
        SharedPreferences prefsSession = getSharedPreferences("MiniMarketPrefs", MODE_PRIVATE);
        int currentUserId = prefsSession.getInt("usuario_actual_id", -1);
        Cursor cursor = db.getAllProducts(currentUserId);

        int stockTotal = 0, vencenHoy = 0, vencenSemana = 0;
        int countCriticos = 0, countProximos = 0, countOk = 0;
        listaTodos.clear();
        listaCriticos.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calHoy = Calendar.getInstance();
        calHoy.set(Calendar.HOUR_OF_DAY, 0); calHoy.set(Calendar.MINUTE, 0); calHoy.set(Calendar.SECOND, 0); calHoy.set(Calendar.MILLISECOND, 0);

        if (cursor != null) {
            // Nota: el indice 1 ahora es user_id, los datos se desplazan una posicion
            while (cursor.moveToNext()) {
                stockTotal++;
                int id = cursor.getInt(0);
                String nombre = cursor.getString(2);
                String categoria = cursor.getString(3);
                String lote = cursor.getString(4);
                String fechaCaducidad = cursor.getString(5);
                String ubicacion = cursor.getString(6);
                int cantidad = cursor.getInt(7);
                long diasRestantes = 999;
                String estado = "OK";
                if (fechaCaducidad != null && !fechaCaducidad.isEmpty() && !"00.00.0000".equals(fechaCaducidad)) {
                    try {
                        Date fecha = sdf.parse(fechaCaducidad);
                        if (fecha != null) {
                            long diffMillis = fecha.getTime() - calHoy.getTimeInMillis();
                            diasRestantes = diffMillis / (1000 * 60 * 60 * 24);
                            if (diasRestantes <= 3) estado = "CRITICO";
                            else if (diasRestantes <= 7) estado = "PROXIMO";
                            else estado = "OK";

                            if (diasRestantes == 0) vencenHoy++;
                            else if (diasRestantes > 0 && diasRestantes <= 7) vencenSemana++;
                        }
                    } catch (ParseException e) {
                        Log.e("DashboardActivity", "Error al parsear fecha: " + fechaCaducidad);
                    }
                }

                if ("CRITICO".equals(estado)) countCriticos++;
                else if ("PROXIMO".equals(estado)) countProximos++;
                else countOk++;
                Producto p = new Producto(id, nombre, categoria, lote, fechaCaducidad, ubicacion, cantidad, diasRestantes, estado);
                listaTodos.add(p);
                if ("CRITICO".equals(estado) || "PROXIMO".equals(estado)) {
                    listaCriticos.add(p);
                }
            }
            cursor.close();
        }
        tvStockTotal.setText(String.valueOf(stockTotal));
        tvVencenHoy.setText(String.valueOf(vencenHoy));
        tvVencenSemana.setText(String.valueOf(vencenSemana));

        if (pieChart != null) {
            pieChart.setValores(countOk, countProximos, countCriticos);
        }
        adapter = new ProductoAdapter(this, listaCriticos);
        lvProductos.setAdapter(adapter);

        if (vencenHoy > 0 || !listaCriticos.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("MiniMarketPrefs", MODE_PRIVATE);
            String fechaUltimaNotificacion = prefs.getString("fechaNotificacion", "");
            String fechaActual = sdf.format(new Date());
            if (!fechaActual.equals(fechaUltimaNotificacion)) {
                enviarNotificacionPush(listaCriticos.size());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fechaNotificacion", fechaActual);
                editor.apply();
            }
        }
    }

    private void enviarNotificacionPush(int cantidadCriticos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String canalId = "CANAL_ALERTAS";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(canalId, "Alertas de Vencimiento", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canalId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("¡Atención: Vencimiento próximo!")
                .setContentText("Tienes " + cantidadCriticos + " producto(s) en riesgo. Toca Alertas para revisarlos.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        manager.notify(1, builder.build());
    }

    private void buscarProducto(String textoBusqueda) {
        List<Producto> listaFiltrada = new ArrayList<>();
        if (textoBusqueda.isEmpty()) {
            listaFiltrada.addAll(listaCriticos);
        } else {
            String query = textoBusqueda.toLowerCase();
            for (Producto p : listaTodos) {
                if (p.nombre.toLowerCase().contains(query) ||
                        p.categoria.toLowerCase().contains(query) ||
                        p.lote.toLowerCase().contains(query)) {
                    listaFiltrada.add(p);
                }
            }
        }
        adapter = new ProductoAdapter(this, listaFiltrada);
        lvProductos.setAdapter(adapter);
    }
}