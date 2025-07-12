package com.lamasia.applamasia;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ResumenEventosActivity extends AppCompatActivity {

    private RecyclerView rvEventos;
    private LinearLayout contenedorAvisos;
    private EventoAdapter eventoAdapter;
    private final List<Evento> listaEventos = new ArrayList<>();
    private String categoriaUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("📅 Resumen de Eventos y Avisos");
        setContentView(R.layout.activity_resumen_eventos);

        rvEventos = findViewById(R.id.rvEventos);
        contenedorAvisos = findViewById(R.id.contenedorAvisos);

        rvEventos.setLayoutManager(new LinearLayoutManager(this));
        eventoAdapter = new EventoAdapter(listaEventos);
        rvEventos.setAdapter(eventoAdapter);

        categoriaUsuario = getIntent().getStringExtra("categoria");
        if (categoriaUsuario == null || categoriaUsuario.isEmpty()) {
            cargarCategoriaDesdeFirebase();
        } else {
            cargarEventosYAvisos();
        }
    }

    private void cargarCategoriaDesdeFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
        ref.child("categoria").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriaUsuario = snapshot.getValue(String.class);
                cargarEventosYAvisos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // No hacer nada
            }
        });
    }

    private void cargarEventosYAvisos() {
        cargarEventos();
        cargarAvisos();
    }

    private void cargarEventos() {
        DatabaseReference refEventos = FirebaseDatabase.getInstance().getReference("eventos");
        refEventos.orderByChild("categoria").equalTo(categoriaUsuario)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaEventos.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String tipo = snap.child("tipo").getValue(String.class);
                            if (tipo != null && tipo.startsWith("[Aviso]")) continue;

                            String fecha = snap.child("fecha").getValue(String.class);
                            String descripcion = snap.child("descripcion").getValue(String.class);
                            String hora = snap.child("hora").getValue(String.class);
                            String expiracion = snap.child("expiracion").getValue(String.class);
                            String categoria = snap.child("categoria").getValue(String.class);

                            if (tipo != null && fecha != null && descripcion != null && hora != null && expiracion != null && categoria != null) {
                                listaEventos.add(new Evento(tipo, fecha, descripcion, hora, expiracion, categoria));
                            }
                        }
                        eventoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // No hacer nada
                    }
                });
    }

    private void cargarAvisos() {
        DatabaseReference refAvisos = FirebaseDatabase.getInstance().getReference("avisos");
        refAvisos.orderByChild("categoria").equalTo(categoriaUsuario)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contenedorAvisos.removeAllViews();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String titulo = snap.child("titulo").getValue(String.class);
                            String contenido = snap.child("contenido").getValue(String.class);
                            String fecha = snap.child("fecha").getValue(String.class);

                            if (titulo != null && contenido != null && fecha != null) {
                                View avisoView = crearVistaAviso(titulo, contenido, fecha);
                                contenedorAvisos.addView(avisoView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // No hacer nada
                    }
                });
    }

    private View crearVistaAviso(String titulo, String contenido, String fecha) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(getResources().getColor(R.color.blue));
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setElevation(6f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        layout.setLayoutParams(params);

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("📢 " + titulo);
        tvTitulo.setTextColor(getResources().getColor(android.R.color.white));
        tvTitulo.setTextSize(16);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvTitulo);

        TextView tvContenido = new TextView(this);
        tvContenido.setText("📝 " + contenido);
        tvContenido.setTextColor(getResources().getColor(android.R.color.white));
        layout.addView(tvContenido);

        TextView tvFecha = new TextView(this);
        tvFecha.setText("📅 " + fecha);
        tvFecha.setTextColor(getResources().getColor(android.R.color.white));
        layout.addView(tvFecha);

        return layout;
    }
}
