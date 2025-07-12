package com.lamasia.applamasia;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class JugadoresPorCategoriaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JugadorAdapter adapter;
    private List<Jugador> listaJugadores;
    private String categoriaDelAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_jugadores); // Asegúrate que este layout contiene recyclerViewJugadores

        recyclerView = findViewById(R.id.recyclerViewJugadores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaJugadores = new ArrayList<>();
        adapter = new JugadorAdapter(this, listaJugadores); // ← CORREGIDO
        recyclerView.setAdapter(adapter);

        obtenerCategoriaYListar();
    }

    private void obtenerCategoriaYListar() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        refUsuarios.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriaDelAdmin = snapshot.child("categoria").getValue(String.class);
                if (categoriaDelAdmin != null) {
                    listarJugadoresPorCategoria();
                } else {
                    Toast.makeText(JugadoresPorCategoriaActivity.this, "No se encontró la categoría del administrador", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JugadoresPorCategoriaActivity.this, "Error consultando usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listarJugadoresPorCategoria() {
        DatabaseReference refJugadores = FirebaseDatabase.getInstance().getReference("jugadores");

        refJugadores.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaJugadores.clear();
                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {
                    for (DataSnapshot jugadorSnap : usuarioSnap.getChildren()) {
                        String categoriaJugador = jugadorSnap.child("categoria").getValue(String.class);
                        if (categoriaDelAdmin.equals(categoriaJugador)) {
                            Jugador jugador = jugadorSnap.getValue(Jugador.class);
                            listaJugadores.add(jugador);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JugadoresPorCategoriaActivity.this, "Error cargando jugadores", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
