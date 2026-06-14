package com.example.proyectoaplicacion;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistroUsuarioActivity extends AppCompatActivity {

    EditText etNuevoUsuario, etNuevaPassword;
    Button btnCrearUsuario;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);

        db = new DatabaseHelper(this);
        etNuevoUsuario = findViewById(R.id.etNuevoUsuario);
        etNuevaPassword = findViewById(R.id.etNuevaPassword);
        btnCrearUsuario = findViewById(R.id.btnCrearUsuario);

        btnCrearUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etNuevoUsuario.getText().toString();
                String pass = etNuevaPassword.getText().toString();

                if(user.isEmpty() || pass.isEmpty()){
                    Toast.makeText(RegistroUsuarioActivity.this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // insertUser encriptará la contraseña automáticamente
                boolean isInserted = db.insertUser(user, pass);
                if(isInserted){
                    Toast.makeText(RegistroUsuarioActivity.this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra esta ventana y regresa al Login
                } else {
                    Toast.makeText(RegistroUsuarioActivity.this, "Error al crear el usuario. Intenta otro nombre.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}