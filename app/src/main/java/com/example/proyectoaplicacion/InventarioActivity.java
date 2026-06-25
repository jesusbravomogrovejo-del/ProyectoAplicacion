package com.example.proyectoaplicacion;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventarioActivity extends AppCompatActivity {
    ListView lvInventario;
    DatabaseHelper db;
    List<Producto> listaCompleta;
    ProductoAdapter adapter;
    Button btnFiltroTodos, btnFiltroCriticos, btnFiltroProximos, btnFiltroOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);
        db = new DatabaseHelper(this);
        lvInventario = findViewById(R.id.lvInventario);
        btnFiltroTodos = findViewById(R.id.btnFiltroTodos);
        btnFiltroCriticos = findViewById(R.id.btnFiltroCriticos);
        btnFiltroProximos = findViewById(R.id.btnFiltroProximos);
        btnFiltroOk = findViewById(R.id.btnFiltroOk);

        cargarProductosDesdeBD();

        btnFiltroTodos.setOnClickListener(v -> aplicarFiltro("TODOS", btnFiltroTodos));
        btnFiltroCriticos.setOnClickListener(v -> aplicarFiltro("CRITICO", btnFiltroCriticos));
        btnFiltroProximos.setOnClickListener(v -> aplicarFiltro("PROXIMO", btnFiltroProximos));
        btnFiltroOk.setOnClickListener(v -> aplicarFiltro("OK", btnFiltroOk));

        FloatingActionButton btnExportarPDF = findViewById(R.id.btnExportarPDF);
        btnExportarPDF.setOnClickListener(v -> exportarPDF());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_inventario);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(InventarioActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_inventario) {
                return true;
            } else if (id == R.id.nav_registrar) {
                startActivity(new Intent(InventarioActivity.this, RegistroActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_alertas) {
                startActivity(new Intent(InventarioActivity.this, AlertasActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void cargarProductosDesdeBD() {
        listaCompleta = new ArrayList<>();

        android.content.SharedPreferences prefsSession = getSharedPreferences("MiniMarketPrefs", MODE_PRIVATE);
        int currentUserId = prefsSession.getInt("usuario_actual_id", -1);
        Cursor cursor = db.getAllProducts(currentUserId);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calHoy = Calendar.getInstance();
        calHoy.set(Calendar.HOUR_OF_DAY, 0); calHoy.set(Calendar.MINUTE, 0); calHoy.set(Calendar.SECOND, 0); calHoy.set(Calendar.MILLISECOND, 0);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(2);
                String categoria = cursor.getString(3);
                String lote = cursor.getString(4);
                String fechaCaducidad = cursor.getString(5);
                String ubicacion = cursor.getString(6);
                int cantidad = cursor.getInt(7);
                long diasRestantes = 999;
                String estado = "OK";
                if (!fechaCaducidad.equals("00.00.0000")) {
                    try {
                        Date fecha = sdf.parse(fechaCaducidad);
                        if (fecha != null) {
                            long diffMillis = fecha.getTime() - calHoy.getTimeInMillis();
                            diasRestantes = diffMillis / (1000 * 60 * 60 * 24);
                            if (diasRestantes <= 3) estado = "CRITICO";
                            else if (diasRestantes <= 7) estado = "PROXIMO";
                            else estado = "OK";
                        }
                    } catch (ParseException e) { e.printStackTrace(); }
                }
                listaCompleta.add(new Producto(id, nombre, categoria, lote, fechaCaducidad, ubicacion, cantidad, diasRestantes, estado));
            } while (cursor.moveToNext());
            cursor.close();
        }
        aplicarFiltro("TODOS", btnFiltroTodos);
    }

    private void aplicarFiltro(String estadoFiltro, Button botonActivo) {
        btnFiltroTodos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#333333")));
        btnFiltroCriticos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#333333")));
        btnFiltroProximos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#333333")));
        btnFiltroOk.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#333333")));
        botonActivo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1976D2")));
        List<Producto> listaFiltrada = new ArrayList<>();
        if (estadoFiltro.equals("TODOS")) {
            listaFiltrada.addAll(listaCompleta);
        } else {
            for (Producto p : listaCompleta) {
                if (estadoFiltro.equals("OK") && p.fechaCaducidad.equals("00.00.0000")) {
                    listaFiltrada.add(p);
                } else if (p.estado.equals(estadoFiltro)) {
                    listaFiltrada.add(p);
                }
            }
        }
        adapter = new ProductoAdapter(this, listaFiltrada);
        lvInventario.setAdapter(adapter);
    }

    private void exportarPDF() {
        if (adapter == null || adapter.getCount() == 0) {
            Toast.makeText(this, "No hay productos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint tituloPaint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        tituloPaint.setTextAlign(Paint.Align.CENTER);
        tituloPaint.setTextSize(18f);
        tituloPaint.setFakeBoldText(true);
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText("Reporte de Inventario - " + fechaHoy, 595 / 2f, 50, tituloPaint);

        paint.setTextSize(12f);
        paint.setFakeBoldText(true);
        int y = 100;
        canvas.drawText("Producto", 40, y, paint);
        canvas.drawText("Lote", 200, y, paint);
        canvas.drawText("Vence", 320, y, paint);
        canvas.drawText("Stock", 430, y, paint);
        canvas.drawText("Estado", 480, y, paint);
        paint.setFakeBoldText(false);
        y += 20;
        canvas.drawLine(40, y, 550, y, paint);
        y += 20;

        for (int i = 0; i < adapter.getCount(); i++) {
            Producto p = adapter.getItem(i);
            String nombre = p.nombre;
            if (nombre.length() > 20) nombre = nombre.substring(0, 17) + "...";
            String lote = p.lote;
            if (lote.length() > 15) lote = lote.substring(0, 12) + "...";
            canvas.drawText(nombre, 40, y, paint);
            canvas.drawText(lote, 200, y, paint);
            canvas.drawText(p.fechaCaducidad.equals("00.00.0000") ? "N/A" : p.fechaCaducidad, 320, y, paint);
            canvas.drawText(String.valueOf(p.cantidad), 430, y, paint);
            canvas.drawText(p.estado, 480, y, paint);
            y += 25;

            if (y > 800 && i < adapter.getCount() - 1) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }
        pdfDocument.finishPage(page);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Reporte_Mermas_" + timeStamp + ".pdf";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Guardado en Documentos: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el PDF", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }
}