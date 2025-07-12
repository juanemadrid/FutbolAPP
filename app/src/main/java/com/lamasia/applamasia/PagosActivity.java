package com.lamasia.applamasia;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PagosActivity extends AppCompatActivity {

    private RecyclerView recyclerPagos;
    private PagosAdapter pagosAdapter;
    private final List<Pago> listaPagos = new ArrayList<>();
    private boolean esAdmin = false;
    private TextView tvTitulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        tvTitulo = findViewById(R.id.tvTituloPagos);
        recyclerPagos = findViewById(R.id.recyclerPagos);
        recyclerPagos.setLayoutManager(new LinearLayoutManager(this));
        tvTitulo.setText("📘 Cartera de Pagos");

        verificarRolYMostrarPagos();
    }

    private void verificarRolYMostrarPagos() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String rol = snapshot.child("rol").getValue(String.class);

                if (rol != null && rol.equals("admin")) {
                    esAdmin = true;
                } else {
                    esAdmin = false;
                }

                Log.d("PagosActivity", "Rol detectado: " + rol + " | esAdmin: " + esAdmin);

                pagosAdapter = new PagosAdapter(listaPagos, esAdmin, PagosActivity.this);
                recyclerPagos.setAdapter(pagosAdapter);

                cargarPagosSimulados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PagosActivity.this, "Error al obtener el rol del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPagosSimulados() {
        listaPagos.clear();

        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        for (int i = 0; i < meses.length; i++) {
            String mes = meses[i];
            String estado;
            int monto;

            if (i < 6) {
                estado = "Pagado";
                monto = 60000;
            } else if (i == 6) {
                estado = "Pendiente";
                monto = 60000;
            } else {
                estado = "Futuro";
                monto = 0;
            }

            // Marcar como atrasado si ya pasó el 5 de julio
            if (i == 6) {
                Calendar hoy = Calendar.getInstance();
                if (hoy.get(Calendar.MONTH) > 6 || (hoy.get(Calendar.MONTH) == 6 && hoy.get(Calendar.DAY_OF_MONTH) > 5)) {
                    estado = "Atrasado";
                    monto = 70000;
                }
            }

            listaPagos.add(new Pago(mes, estado, monto));
        }

        pagosAdapter.notifyDataSetChanged();
    }
}
