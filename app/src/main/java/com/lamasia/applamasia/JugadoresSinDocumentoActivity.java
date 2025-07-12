package com.lamasia.applamasia;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class JugadoresSinDocumentoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewJugadores;
    private TextView txtMensaje;
    private String categoriaAsignada = "";
    private DatabaseReference dbRef;
    private JugadorAdapter adapter;
    private ArrayList<Jugador> listaJugadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jugadores_sin_documento);

        recyclerViewJugadores = findViewById(R.id.recyclerViewJugadoresSinDocumento);
        txtMensaje = findViewById(R.id.txtMensajeSinDocumento);

        recyclerViewJugadores.setLayoutManager(new LinearLayoutManager(this));

        listaJugadores = new ArrayList<>();
        adapter = new JugadorAdapter(this, listaJugadores);
        recyclerViewJugadores.setAdapter(adapter);

        categoriaAsignada = getIntent().getStringExtra("categoria");
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarJugadoresSinDocumento();
    }

    private void cargarJugadoresSinDocumento() {
        dbRef.orderByChild("categoria").equalTo(categoriaAsignada)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaJugadores.clear();
                        for (DataSnapshot jugadorSnap : snapshot.getChildren()) {
                            String documentoUrl = jugadorSnap.child("urlDocumento").getValue(String.class);
                            if (documentoUrl == null || documentoUrl.isEmpty()) {
                                Jugador jugador = jugadorSnap.getValue(Jugador.class);
                                listaJugadores.add(jugador);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (listaJugadores.isEmpty()) {
                            txtMensaje.setVisibility(View.VISIBLE);
                            recyclerViewJugadores.setVisibility(View.GONE);
                        } else {
                            txtMensaje.setVisibility(View.GONE);
                            recyclerViewJugadores.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(JugadoresSinDocumentoActivity.this, "Error al cargar jugadores", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

