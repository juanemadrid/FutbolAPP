package com.lamasia.applamasia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView txtTotalJugadores, txtSinRegistroCivil, txtTotalEventos, txtTotalPagos;
    private Button btnGestionPerfilJugador, btnGestionCalendario, btnGestionAvisos, btnGestionPagos;
    private LinearLayout cardJugadores, cardEventos, cardPagos, cardSinDocumento;

    private String categoriaAsignada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        txtTotalJugadores = findViewById(R.id.txtTotalJugadores);
        txtSinRegistroCivil = findViewById(R.id.txtSinRegistroCivil);
        txtTotalEventos = findViewById(R.id.txtTotalEventos);
        txtTotalPagos = findViewById(R.id.txtTotalPagos);

        btnGestionPerfilJugador = findViewById(R.id.btnGestionPerfilJugador);
        btnGestionCalendario = findViewById(R.id.btnGestionCalendario);
        btnGestionAvisos = findViewById(R.id.btnGestionAvisos);
        btnGestionPagos = findViewById(R.id.btnGestionPagos);

        cardJugadores = findViewById(R.id.cardJugadores);
        cardEventos = findViewById(R.id.cardEventos);
        cardPagos = findViewById(R.id.cardPagos);
        cardSinDocumento = findViewById(R.id.cardSinDocumento);

        obtenerCategoriaDelAdmin();

        btnGestionPerfilJugador.setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionPerfilJugadorActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });

        btnGestionCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarioEventosActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });

        btnGestionAvisos.setOnClickListener(v -> startActivity(new Intent(this, AvisosActivity.class)));

        btnGestionPagos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaPagosActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });

        cardJugadores.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaJugadoresActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });

        cardEventos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResumenEventosActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });

        cardPagos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaPagosActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });

        cardSinDocumento.setOnClickListener(v -> {
            Intent intent = new Intent(this, JugadoresSinDocumentoActivity.class);
            intent.putExtra("categoria", categoriaAsignada);
            startActivity(intent);
        });
    }

    private void obtenerCategoriaDelAdmin() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriaAsignada = snapshot.child("categoria").getValue(String.class);
                if (categoriaAsignada != null && !categoriaAsignada.isEmpty()) {
                    cargarResumen();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Categoría no definida", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarResumen() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // ✅ Contar TODOS los jugadores registrados (sin filtrar por categoría)
        db.child("jugadores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int sinDocumento = 0;
                for (DataSnapshot jugador : snapshot.getChildren()) {
                    total++;
                    String urlDoc = jugador.child("urlDocumento").getValue(String.class);
                    if (urlDoc == null || urlDoc.isEmpty()) {
                        sinDocumento++;
                    }
                }
                txtTotalJugadores.setText(String.valueOf(total));
                txtSinRegistroCivil.setText(String.valueOf(sinDocumento));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Eventos + Avisos: solo los de la categoría asignada al admin
        db.child("eventos").orderByChild("categoria").equalTo(categoriaAsignada)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot eventosSnap) {
                        final int totalEventos = (int) eventosSnap.getChildrenCount();

                        db.child("avisos").orderByChild("categoria").equalTo(categoriaAsignada)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot avisosSnap) {
                                        int totalAvisos = (int) avisosSnap.getChildrenCount();
                                        int total = totalEventos + totalAvisos;
                                        txtTotalEventos.setText(String.valueOf(total));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Pagos pendientes (solo por categoría del admin)
        db.child("pagos").orderByChild("categoria").equalTo(categoriaAsignada)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pendientes = 0;
                        for (DataSnapshot pago : snapshot.getChildren()) {
                            String estado = pago.child("estadoPago").getValue(String.class);
                            if (estado == null || !estado.equalsIgnoreCase("pagado")) {
                                pendientes++;
                            }
                        }
                        txtTotalPagos.setText(String.valueOf(pendientes));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
