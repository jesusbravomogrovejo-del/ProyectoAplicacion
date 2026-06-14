package com.example.proyectoaplicacion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Minimarket.db";
    private static final int DATABASE_VERSION = 2; // Subimos la versión por los cambios

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla Usuarios
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)");

        // Usuario por defecto (admin, 12345) guardado ya con SHA-256
        String defaultPassword = hashPassword("12345");
        db.execSQL("INSERT INTO users (username, password) VALUES ('admin', '" + defaultPassword + "')");

        // Tabla Productos (AHORA CON EL CAMPO CANTIDAD AL FINAL)
        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "categoria TEXT, " +
                "lote TEXT, " +
                "fecha_caducidad TEXT, " +
                "ubicacion TEXT, " +
                "cantidad INTEGER)"); // <-- CAMPO NUEVO
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

    // --- SEGURIDAD: MÉTODO SHA-256 ---
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encriptando contraseña", e);
        }
    }

    // --- MÉTODOS DE USUARIO ---
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", hashPassword(password)); // Se encripta antes de guardar
        long result = db.insert("users", null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String encryptedPassword = hashPassword(password); // Se encripta para comparar con la BD
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, encryptedPassword});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // --- MÉTODOS DE PRODUCTO ---
    public boolean insertProduct(String nombre, String categoria, String lote, String fecha, String ubicacion, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("nombre", nombre);
        contentValues.put("categoria", categoria);
        contentValues.put("lote", lote);
        contentValues.put("fecha_caducidad", fecha);
        contentValues.put("ubicacion", ubicacion);
        contentValues.put("cantidad", cantidad); // <-- NUEVO
        long result = db.insert("products", null, contentValues);
        return result != -1;
    }

    public boolean updateProduct(int id, String nombre, String categoria, String lote, String fecha, String ubicacion, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("nombre", nombre);
        contentValues.put("categoria", categoria);
        contentValues.put("lote", lote);
        contentValues.put("fecha_caducidad", fecha);
        contentValues.put("ubicacion", ubicacion);
        contentValues.put("cantidad", cantidad); // <-- NUEVO
        long result = db.update("products", contentValues, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete("products", "id=?", new String[]{String.valueOf(id)});
        return result != -1;
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM products", null);
    }
}