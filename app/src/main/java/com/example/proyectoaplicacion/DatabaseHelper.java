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
    private static final int DATABASE_VERSION = 3; // Version 3 para soportar multicuenta

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla Usuarios
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)");

        // Usuario por defecto
        String defaultPassword = hashPassword("12345");
        db.execSQL("INSERT INTO users (username, password) VALUES ('admin', '" + defaultPassword + "')");

        // Tabla Productos con el nuevo campo user_id
        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "nombre TEXT, " +
                "categoria TEXT, " +
                "lote TEXT, " +
                "fecha_caducidad TEXT, " +
                "ubicacion TEXT, " +
                "cantidad INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

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

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", hashPassword(password));
        long result = db.insert("users", null, contentValues);
        return result != -1;
    }

    // Ahora devuelve el ID del usuario en lugar de boolean
    public int checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String encryptedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND password = ?", new String[]{username, encryptedPassword});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // Recibe el userId
    public boolean insertProduct(int userId, String nombre, String categoria, String lote, String fecha, String ubicacion, int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", userId);
        contentValues.put("nombre", nombre);
        contentValues.put("categoria", categoria);
        contentValues.put("lote", lote);
        contentValues.put("fecha_caducidad", fecha);
        contentValues.put("ubicacion", ubicacion);
        contentValues.put("cantidad", cantidad);
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
        contentValues.put("cantidad", cantidad);
        long result = db.update("products", contentValues, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete("products", "id=?", new String[]{String.valueOf(id)});
        return result != -1;
    }

    // Filtra los productos por userId
    public Cursor getAllProducts(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM products WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }
}