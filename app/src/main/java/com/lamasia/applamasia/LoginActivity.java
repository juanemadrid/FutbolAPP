package com.lamasia.applamasia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;

public class LoginActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "LoginActivity";

    private EditText etCorreo, etContrasena;
    private Button btnIniciarSesion;
    private TextView tvOlvidasteContrasena;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        tvOlvidasteContrasena = findViewById(R.id.tvOlvidasteContrasena);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");

        solicitarPermisoNotificaciones();

        obtenerYGuardarTokenFCM();

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        tvOlvidasteContrasena.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            if (correo.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void obtenerYGuardarTokenFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        FirebaseFirestore.getInstance()
                                .collection("user_tokens")
                                .document(token)
                                .set(Collections.singletonMap("token", token));
                        Log.d(TAG, "Token FCM obtenido y guardado: " + token);
                    } else {
                        Log.w(TAG, "No se pudo obtener el token FCM", task.getException());
                    }
                });
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Log.d(TAG, "Usuario autenticado, UID: " + uid);

                            mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        Toast.makeText(LoginActivity.this, "Usuario no registrado", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String rol = snapshot.child("rol").getValue(String.class);
                                    String categoria = snapshot.child("categoria").getValue(String.class);

                                    Log.d(TAG, "Rol: " + rol + ", Categoria: " + categoria);

                                    if (rol == null || rol.isEmpty()) {
                                        Toast.makeText(LoginActivity.this, "Rol no definido", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (rol.equalsIgnoreCase("admin")) {
                                        FirebaseMessaging.getInstance().subscribeToTopic("admin")
                                                .addOnCompleteListener(subTask -> {
                                                    if (subTask.isSuccessful()) {
                                                        Log.d(TAG, "Suscripción al topic 'admin' exitosa");
                                                    } else {
                                                        Log.w(TAG, "Error al suscribir al topic 'admin'", subTask.getException());
                                                    }
                                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                                    finish();
                                                });
                                    } else {
                                        if (categoria == null || categoria.isEmpty()) {
                                            Toast.makeText(LoginActivity.this, "Categoría no asignada al usuario", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        String topicCategoria = "eventos_" + categoria.toLowerCase().replaceAll("\\s+", "_");

                                        FirebaseMessaging.getInstance().subscribeToTopic("usuarios")
                                                .addOnCompleteListener(subTaskUsuarios -> {
                                                    if (subTaskUsuarios.isSuccessful()) {
                                                        Log.d(TAG, "Suscripción al topic 'usuarios' exitosa");
                                                    } else {
                                                        Log.w(TAG, "Error al suscribir al topic 'usuarios'", subTaskUsuarios.getException());
                                                    }
                                                });

                                        FirebaseMessaging.getInstance().subscribeToTopic(topicCategoria)
                                                .addOnCompleteListener(subTaskCategoria -> {
                                                    if (subTaskCategoria.isSuccessful()) {
                                                        Log.d(TAG, "Suscripción al topic '" + topicCategoria + "' exitosa");
                                                    } else {
                                                        Log.w(TAG, "Error al suscribir al topic '" + topicCategoria + "'", subTaskCategoria.getException());
                                                    }

                                                    Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                                                    intent.putExtra("categoria", categoria);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, "Error al leer datos", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error al leer datos de usuario: ", error.toException());
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: usuario nulo después de login", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error de autenticación", task.getException());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificación concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
