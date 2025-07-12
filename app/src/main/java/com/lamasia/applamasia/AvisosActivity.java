// AvisosActivity.java
package com.lamasia.applamasia;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.*;

public class AvisosActivity extends AppCompatActivity {

    private LinearLayout layoutAvisos;
    private ImageButton btnAgregarAviso;
    private TextView tvSinAvisos;
    private String categoriaUsuario = "";
    private String rolUsuario = "";
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("📣 Avisos y Comunicados");
        setContentView(R.layout.activity_avisos);

        layoutAvisos = findViewById(R.id.layoutAvisos);
        btnAgregarAviso = findViewById(R.id.btnAgregarAviso);
        tvSinAvisos = findViewById(R.id.tvSinAvisos);

        user = FirebaseAuth.getInstance().getCurrentUser();
        categoriaUsuario = getIntent().getStringExtra("categoria");

        if (user == null) {
            Toast.makeText(this, "❌ Error: sesión no iniciada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (categoriaUsuario == null || categoriaUsuario.isEmpty()) {
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid());
            refUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    categoriaUsuario = snapshot.child("categoria").getValue(String.class);
                    if (categoriaUsuario == null || categoriaUsuario.isEmpty()) {
                        Toast.makeText(AvisosActivity.this, "⚠️ Categoría no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    FirebaseMessaging.getInstance().subscribeToTopic("eventos_" + categoriaUsuario);
                    iniciarActividad();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AvisosActivity.this, "⚠️ Error al cargar categoría", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic("eventos_" + categoriaUsuario);
            iniciarActividad();
        }
    }

    private void iniciarActividad() {
        DatabaseReference refUsuario = FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid());
        refUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rolUsuario = snapshot.child("rol").getValue(String.class);

                if ("admin".equals(rolUsuario)) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("eventos_" + categoriaUsuario);
                } else {
                    btnAgregarAviso.setVisibility(View.GONE);
                }

                cargarAvisos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AvisosActivity.this, "⚠️ Error al obtener rol", Toast.LENGTH_SHORT).show();
            }
        });

        btnAgregarAviso.setOnClickListener(v -> mostrarDialogoAgregarAviso());
    }

    private void cargarAvisos() {
        layoutAvisos.removeAllViews();
        tvSinAvisos.setVisibility(View.GONE);

        DatabaseReference refAvisos = FirebaseDatabase.getInstance().getReference("avisos");
        refAvisos.orderByChild("categoria").equalTo(categoriaUsuario)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        layoutAvisos.removeAllViews();
                        boolean hayAvisos = false;

                        for (DataSnapshot avisoSnap : snapshot.getChildren()) {
                            String key = avisoSnap.getKey();
                            String titulo = avisoSnap.child("titulo").getValue(String.class);
                            String contenido = avisoSnap.child("contenido").getValue(String.class);
                            String fecha = avisoSnap.child("fecha").getValue(String.class);
                            String expiracion = avisoSnap.child("expiracion").getValue(String.class);
                            String hora = avisoSnap.child("hora").getValue(String.class);

                            if (!avisoExpirado(fecha, hora, expiracion)) {
                                hayAvisos = true;
                                agregarAVista(key, titulo, contenido, fecha);
                            } else {
                                avisoSnap.getRef().removeValue();

                                FirebaseDatabase.getInstance().getReference("eventos")
                                        .orderByChild("tipo").equalTo("[Aviso] " + titulo)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot snap : snapshot.getChildren()) {
                                                    snap.getRef().removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                        }

                        if (!hayAvisos) {
                            tvSinAvisos.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AvisosActivity.this, "Error al cargar avisos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean avisoExpirado(String fecha, String hora, String expiracionHoras) {
        try {
            int horas = Integer.parseInt(expiracionHoras);
            if (horas <= 0) return false;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date fechaEvento = sdf.parse(fecha + " " + hora);
            Calendar expiracion = Calendar.getInstance();
            expiracion.setTime(fechaEvento);
            expiracion.add(Calendar.HOUR_OF_DAY, horas);

            return new Date().after(expiracion.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    private void agregarAVista(String key, String titulo, String contenido, String fecha) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setBackgroundColor(getResources().getColor(R.color.blue));
        contenedor.setPadding(24, 24, 24, 24);
        contenedor.setElevation(6f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        contenedor.setLayoutParams(params);

        contenedor.addView(crearTexto("📢 " + titulo, 18, true));
        contenedor.addView(crearTexto("📝 " + contenido, 16, false));
        contenedor.addView(crearTexto("📅 " + fecha, 14, false));

        if ("admin".equals(rolUsuario)) {
            Button btnEliminar = new Button(this);
            btnEliminar.setText("❌ Eliminar aviso");
            btnEliminar.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("¿Eliminar aviso?")
                    .setMessage("¿Estás seguro de eliminar este aviso?")
                    .setPositiveButton("Sí", (d, w) ->
                            FirebaseDatabase.getInstance().getReference("avisos").child(key).removeValue())
                    .setNegativeButton("No", null)
                    .show());
            contenedor.addView(btnEliminar);
        }

        layoutAvisos.addView(contenedor);
    }

    private TextView crearTexto(String texto, float tamaño, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(tamaño);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        tv.setGravity(Gravity.START);
        if (negrita) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void mostrarDialogoAgregarAviso() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        EditText etTitulo = new EditText(this);
        etTitulo.setHint("Título del aviso");
        layout.addView(etTitulo);

        EditText etContenido = new EditText(this);
        etContenido.setHint("Contenido del aviso");
        etContenido.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        layout.addView(etContenido);

        Spinner spinnerExpira = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"No borrar", "24 horas", "48 horas", "72 horas"});
        spinnerExpira.setAdapter(adapter);
        layout.addView(spinnerExpira);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Aviso")
                .setView(layout)
                .setPositiveButton("Publicar", (dialog, which) -> {
                    String titulo = etTitulo.getText().toString().trim();
                    String contenido = etContenido.getText().toString().trim();
                    String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    String expiracion = switch (spinnerExpira.getSelectedItemPosition()) {
                        case 1 -> "24";
                        case 2 -> "48";
                        case 3 -> "72";
                        default -> "0";
                    };

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("avisos").push();
                    ref.child("titulo").setValue(titulo);
                    ref.child("contenido").setValue(contenido);
                    ref.child("fecha").setValue(fecha);
                    ref.child("hora").setValue(hora);
                    ref.child("expiracion").setValue(expiracion);
                    ref.child("categoria").setValue(categoriaUsuario);

                    DatabaseReference eventoRef = FirebaseDatabase.getInstance().getReference("eventos").push();
                    eventoRef.child("tipo").setValue("[Aviso] " + titulo);
                    eventoRef.child("fecha").setValue(fecha);
                    eventoRef.child("hora").setValue(hora);
                    eventoRef.child("descripcion").setValue(contenido);
                    eventoRef.child("expiracion").setValue(expiracion);
                    eventoRef.child("categoria").setValue(categoriaUsuario);

                    // 🔔 Solo notifica a usuarios (no a admin)
                    if (!"admin".equals(rolUsuario)) {
                        String topic = "eventos_" + categoriaUsuario;
                        FCMHelperV1.enviarNotificacionATopic(
                                AvisosActivity.this,
                                topic,
                                "📢 Nuevo aviso",
                                titulo + ": " + contenido
                        );
                    }

                    Toast.makeText(this, "✅ Aviso publicado con éxito", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
