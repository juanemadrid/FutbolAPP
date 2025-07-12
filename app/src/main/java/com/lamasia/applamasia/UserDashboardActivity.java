package com.lamasia.applamasia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class UserDashboardActivity extends AppCompatActivity {

    private Button btnPerfilNino, btnPagos, btnCalendario, btnAvisos, btnSoporte;
    private String rolUsuario = "";
    private String categoriaUsuario = "";
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        btnPerfilNino = findViewById(R.id.btnPerfilNino);
        btnPagos = findViewById(R.id.btnPagos);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnAvisos = findViewById(R.id.btnAvisos);
        btnSoporte = findViewById(R.id.btnSoporte);

        btnPerfilNino.setEnabled(false);
        btnPagos.setEnabled(false);
        btnCalendario.setEnabled(false);
        btnAvisos.setEnabled(false);
        btnSoporte.setEnabled(false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            mostrarErrorYSalir("No se ha iniciado sesión correctamente.");
            return;
        }

        categoriaUsuario = getIntent().getStringExtra("categoria");
        if (categoriaUsuario == null) categoriaUsuario = "";

        obtenerRolYCategoriaDesdeFirebase();
    }

    private void obtenerRolYCategoriaDesdeFirebase() {
        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    mostrarErrorYSalir("Usuario no encontrado en la base de datos.");
                    return;
                }

                rolUsuario = snapshot.child("rol").getValue(String.class);
                String categoriaDesdeFirebase = snapshot.child("categoria").getValue(String.class);

                if (rolUsuario == null || rolUsuario.isEmpty()) {
                    mostrarErrorYSalir("Rol no definido.");
                    return;
                }

                if (categoriaDesdeFirebase != null && !categoriaDesdeFirebase.isEmpty()) {
                    categoriaUsuario = categoriaDesdeFirebase;
                }

                if (categoriaUsuario.isEmpty()) {
                    mostrarErrorYSalir("Categoría no asignada.");
                    return;
                }

                configurarBotones();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mostrarErrorYSalir("Error al conectar con Firebase: " + error.getMessage());
            }
        });
    }

    private void mostrarErrorYSalir(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(UserDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void configurarBotones() {
        btnPerfilNino.setOnClickListener(v -> {
            if ("admin".equals(rolUsuario)) {
                startActivity(new Intent(this, GestionPerfilJugadorActivity.class));
            } else {
                String uid = user.getUid();
                DatabaseReference jugadorRef = FirebaseDatabase.getInstance().getReference("jugadores").child(uid);

                jugadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            startActivity(new Intent(UserDashboardActivity.this, PerfilJugadorActivity.class));
                        } else {
                            jugadorRef.child("nombre").setValue("");
                            jugadorRef.child("identificacion").setValue("");
                            jugadorRef.child("edad").setValue("");
                            jugadorRef.child("categoria").setValue(categoriaUsuario);
                            jugadorRef.child("eps").setValue("");
                            jugadorRef.child("contactoEmergencia").setValue("");
                            jugadorRef.child("telefonoEmergencia").setValue("");

                            Toast.makeText(UserDashboardActivity.this, "📝 Perfil creado. Ahora puedes completarlo.", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(UserDashboardActivity.this, PerfilJugadorActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboardActivity.this, "Error al consultar perfil del jugador", Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE", "Error: " + error.getMessage());
                    }
                });
            }
        });

        btnPagos.setOnClickListener(v -> {
            Intent intent = new Intent(this, PagosActivity.class);
            intent.putExtra("categoria", categoriaUsuario);
            startActivity(intent);
        });

        btnCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarioEventosActivity.class);
            intent.putExtra("categoria", categoriaUsuario);
            startActivity(intent);
        });

        btnAvisos.setOnClickListener(v -> {
            Intent intent = new Intent(this, AvisosActivity.class);
            intent.putExtra("categoria", categoriaUsuario);
            startActivity(intent);
        });

        btnSoporte.setOnClickListener(v -> {
            // ✅ CAMBIA AQUÍ el número de WhatsApp de la administración (sin el símbolo +)
            String numeroAdmin = "573015768935"; // EJEMPLO: "573211234567"
            String mensaje = "Hola, necesito ayuda con mi perfil en La Masía.";
            String url = "https://wa.me/" + numeroAdmin + "?text=" + Uri.encode(mensaje);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        btnPerfilNino.setEnabled(true);
        btnPagos.setEnabled(true);
        btnCalendario.setEnabled(true);
        btnAvisos.setEnabled(true);
        btnSoporte.setEnabled(true);
    }
}
