package com.lamasia.applamasia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;

public class GestionPerfilJugadorActivity extends AppCompatActivity {

    private EditText etIdentificacionBuscar;
    private Button btnBuscarJugador, btnDescargarDocumento;
    private ImageView ivFotoJugador;
    private TextView tvNombre, tvEdad, tvCategoria, tvEPS, tvContactoEmergencia, tvTelefonoEmergencia;
    private LinearLayout contenedorDatos;

    private DatabaseReference dbRef;
    private String documentoJugadorUrl;
    private String jugadorKeyActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_perfil_jugador);

        etIdentificacionBuscar = findViewById(R.id.etIdentificacionBuscar);
        btnBuscarJugador = findViewById(R.id.btnBuscarJugador);
        btnDescargarDocumento = findViewById(R.id.btnDescargarDocumento);
        ivFotoJugador = findViewById(R.id.ivFotoJugador);
        tvNombre = findViewById(R.id.tvNombre);
        tvEdad = findViewById(R.id.tvEdad);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvEPS = findViewById(R.id.tvEPS);
        tvContactoEmergencia = findViewById(R.id.tvContactoEmergencia);
        tvTelefonoEmergencia = findViewById(R.id.tvTelefonoEmergencia);
        contenedorDatos = findViewById(R.id.contenedorDatos);

        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");

        btnBuscarJugador.setOnClickListener(v -> buscarJugador());

        btnDescargarDocumento.setOnClickListener(v -> {
            if (documentoJugadorUrl != null && !documentoJugadorUrl.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(documentoJugadorUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "❌ No se pudo abrir el documento", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "⚠️ No hay documento disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarJugador() {
        String id = etIdentificacionBuscar.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "⚠️ Ingresa una identificación", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean encontrado = false;
                for (DataSnapshot jugadorSnapshot : snapshot.getChildren()) {
                    String identificacion = jugadorSnapshot.child("identificacion").getValue(String.class);
                    if (identificacion != null && identificacion.equals(id)) {
                        jugadorKeyActual = jugadorSnapshot.getKey();
                        mostrarDatos(jugadorSnapshot);
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado) {
                    contenedorDatos.setVisibility(View.GONE);
                    Toast.makeText(GestionPerfilJugadorActivity.this, "❌ Jugador no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                contenedorDatos.setVisibility(View.GONE);
                Toast.makeText(GestionPerfilJugadorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDatos(DataSnapshot snapshot) {
        String nombre = snapshot.child("nombre").getValue(String.class);
        String edad = snapshot.child("edad").getValue(String.class);
        String categoria = snapshot.child("categoria").getValue(String.class);
        String eps = snapshot.child("eps").getValue(String.class);
        String contacto = snapshot.child("contactoEmergencia").getValue(String.class);
        String telefono = snapshot.child("telefonoEmergencia").getValue(String.class);
        String urlFoto = snapshot.child("urlFoto").getValue(String.class);
        String urlDocumento = snapshot.child("urlDocumento").getValue(String.class);

        tvNombre.setText(nombre);
        tvEdad.setText(edad);
        tvCategoria.setText(categoria);
        tvEPS.setText(eps);
        tvContactoEmergencia.setText(contacto);
        tvTelefonoEmergencia.setText(telefono);

        if (urlFoto != null && !urlFoto.isEmpty()) {
            Glide.with(this).load(urlFoto).into(ivFotoJugador);
        } else {
            ivFotoJugador.setImageResource(R.drawable.ic_person);
        }

        documentoJugadorUrl = urlDocumento;

        btnDescargarDocumento.setVisibility(
                (urlDocumento != null && !urlDocumento.isEmpty()) ? View.VISIBLE : View.GONE
        );

        contenedorDatos.setVisibility(View.VISIBLE);
    }
}
