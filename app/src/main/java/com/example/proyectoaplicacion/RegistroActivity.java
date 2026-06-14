package com.example.proyectoaplicacion;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etLote, etUbicacion, etFechaCaducidad, etCantidad;
    Spinner spinnerCategoria;
    SwitchCompat cbPerecedero;
    Button btnGuardar;
    DatabaseHelper db;

    int productoId = -1;

    private Handler disconnectHandler = new Handler();
    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(RegistroActivity.this, "Sesión cerrada por inactividad", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        db = new DatabaseHelper(this);

        etNombre = findViewById(R.id.etNombre);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        etLote = findViewById(R.id.etLote);
        etUbicacion = findViewById(R.id.etUbicacion);
        etFechaCaducidad = findViewById(R.id.etFechaCaducidad);
        etCantidad = findViewById(R.id.etCantidad);
        cbPerecedero = findViewById(R.id.cbPerecedero);
        btnGuardar = findViewById(R.id.btnGuardar);

        resetDisconnectTimer();

        String[] opcionesCategorias = {"Abarrotes", "Lácteos", "Bebidas", "Limpieza", "Snacks", "Cuidado Personal", "Otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, opcionesCategorias);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerCategoria.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent.hasExtra("ID_PRODUCTO")) {
            productoId = intent.getIntExtra("ID_PRODUCTO", -1);

            etNombre.setText(intent.getStringExtra("NOMBRE"));
            etLote.setText(intent.getStringExtra("LOTE"));
            etUbicacion.setText(intent.getStringExtra("UBICACION"));

            // PRE-LLENAR CANTIDAD
            int cantidadExtra = intent.getIntExtra("CANTIDAD", 0);
            etCantidad.setText(String.valueOf(cantidadExtra));

            String categoriaExtra = intent.getStringExtra("CATEGORIA");
            if (categoriaExtra != null) {
                int spinnerPosition = adapter.getPosition(categoriaExtra);
                spinnerCategoria.setSelection(spinnerPosition);
            }

            String fechaExtra = intent.getStringExtra("FECHA");
            if (fechaExtra != null && !fechaExtra.equals("00.00.0000")) {
                cbPerecedero.setChecked(true);
                etFechaCaducidad.setText(fechaExtra);
                etFechaCaducidad.setEnabled(true);
            } else {
                cbPerecedero.setChecked(false);
                etFechaCaducidad.setText("00.00.0000");
                etFechaCaducidad.setEnabled(false);
            }

            btnGuardar.setText("ACTUALIZAR REGISTRO");
        }

        etFechaCaducidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbPerecedero.isChecked()) {
                    Calendar calendario = Calendar.getInstance();
                    int anio = calendario.get(Calendar.YEAR);
                    int mes = calendario.get(Calendar.MONTH);
                    int dia = calendario.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(RegistroActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            String fechaFormateada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                            etFechaCaducidad.setText(fechaFormateada);
                        }
                    }, anio, mes, dia);
                    datePickerDialog.show();
                }
            }
        });

        cbPerecedero.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etFechaCaducidad.setEnabled(true);
                    if (productoId == -1) etFechaCaducidad.setText("");
                    etFechaCaducidad.setHint("dd/mm/aaaa");
                } else {
                    etFechaCaducidad.setEnabled(false);
                    etFechaCaducidad.setText("00.00.0000");
                }
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString();
                String categoria = spinnerCategoria.getSelectedItem().toString();
                String lote = etLote.getText().toString();
                String ubicacion = etUbicacion.getText().toString();
                String fecha = etFechaCaducidad.getText().toString();

                // ATRAPAMOS LA CANTIDAD QUE ESCRIBIÓ EL USUARIO
                String cantidadStr = etCantidad.getText().toString();
                int cantidad = 0;
                if (!cantidadStr.isEmpty()) {
                    try {
                        cantidad = Integer.parseInt(cantidadStr);
                    } catch (NumberFormatException e) {
                        cantidad = 0;
                    }
                }

                if (nombre.isEmpty() || lote.isEmpty() || fecha.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Nombre, Lote y Fecha son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (productoId == -1) {
                    // PASAMOS LA CANTIDAD AL INSERTAR
                    boolean isInserted = db.insertProduct(nombre, categoria, lote, fecha, ubicacion, cantidad);
                    if (isInserted) {
                        Toast.makeText(RegistroActivity.this, "Producto registrado", Toast.LENGTH_SHORT).show();
                        etNombre.setText("");
                        spinnerCategoria.setSelection(0);
                        etLote.setText("");
                        etUbicacion.setText("");
                        etCantidad.setText("");
                        if (cbPerecedero.isChecked()) etFechaCaducidad.setText("");
                    } else {
                        Toast.makeText(RegistroActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // PASAMOS LA CANTIDAD AL ACTUALIZAR
                    boolean isUpdated = db.updateProduct(productoId, nombre, categoria, lote, fecha, ubicacion, cantidad);
                    if (isUpdated) {
                        Toast.makeText(RegistroActivity.this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistroActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegistroActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_registrar);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_inicio) {
                    Intent intent = new Intent(RegistroActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.nav_inventario) {
                    Intent intent = new Intent(RegistroActivity.this, InventarioActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.nav_registrar) {
                    if (productoId != -1) {
                        Intent intent = new Intent(RegistroActivity.this, RegistroActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_alertas) {
                    Intent intent = new Intent(RegistroActivity.this, AlertasActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetDisconnectTimer();
    }

    private void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, 600000);
    }
}