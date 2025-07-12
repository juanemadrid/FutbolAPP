package com.lamasia.applamasia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetalleJugadorActivity extends AppCompatActivity {

    private ImageView ivFoto;
    private TextView tvNombre, tvEdad, tvCategoria, tvEPS, tvContactoEmergencia, tvTelefonoEmergencia;
    private Button btnVerDocumento;

    private String urlDocumento = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_jugador);

        ivFoto = findViewById(R.id.ivFotoDetalle);
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvEdad = findViewById(R.id.tvEdadDetalle);
        tvCategoria = findViewById(R.id.tvCategoriaDetalle);
        tvEPS = findViewById(R.id.tvEPSDetalle);
        tvContactoEmergencia = findViewById(R.id.tvContactoEmergenciaDetalle);
        tvTelefonoEmergencia = findViewById(R.id.tvTelefonoEmergenciaDetalle);
        btnVerDocumento = findViewById(R.id.btnVerDocumento);

        String jugadorKey = getIntent().getStringExtra("jugadorKey");

        if (jugadorKey == null || jugadorKey.isEmpty()) {
            Toast.makeText(this, "❌ No se pudo cargar el jugador", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosJugador(jugadorKey);

        btnVerDocumento.setOnClickListener(v -> {
            if (urlDocumento != null && !urlDocumento.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(urlDocumento), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                Toast.makeText(this, "⚠️ Este jugador no tiene documento subido.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosJugador(String jugadorKey) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jugadores").child(jugadorKey);

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nombre = snapshot.child("nombre").getValue(String.class);
                String edad = snapshot.child("edad").getValue(String.class);
                String categoria = snapshot.child("categoria").getValue(String.class);
                String eps = snapshot.child("eps").getValue(String.class);
                String contacto = snapshot.child("contactoEmergencia").getValue(String.class);
                String telefono = snapshot.child("telefonoEmergencia").getValue(String.class);
                String urlFoto = snapshot.child("urlFoto").getValue(String.class);
                urlDocumento = snapshot.child("urlDocumento").getValue(String.class);

                tvNombre.setText(nombre != null ? nombre : "N/A");
                tvEdad.setText("Edad: " + (edad != null ? edad : "N/A"));
                tvCategoria.setText("Categoría: " + (categoria != null ? categoria : "N/A"));
                tvEPS.setText("EPS: " + (eps != null ? eps : "N/A"));
                tvContactoEmergencia.setText("Contacto emergencia: " + (contacto != null ? contacto : "N/A"));
                tvTelefonoEmergencia.setText("Teléfono emergencia: " + (telefono != null ? telefono : "N/A"));

                if (urlFoto != null && !urlFoto.isEmpty()) {
                    Glide.with(this).load(urlFoto).into(ivFoto);
                } else {
                    ivFoto.setImageResource(R.drawable.ic_person);
                }

            } else {
                Toast.makeText(this, "⚠️ Jugador no encontrado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "❌ Error al cargar datos", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
