package com.lamasia.applamasia;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarioEventosActivity extends AppCompatActivity {

    private LinearLayout layoutEventos, layoutDiasSemana;
    private ImageButton btnAgregarEvento;
    private String categoriaUsuario = "";
    private boolean esAdmin = false;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario_eventos);

        layoutEventos = findViewById(R.id.layoutEventos);
        layoutDiasSemana = findViewById(R.id.layoutDiasSemana);
        btnAgregarEvento = findViewById(R.id.btnAgregarEvento);

        categoriaUsuario = getIntent().getStringExtra("categoria");
        if (categoriaUsuario == null || categoriaUsuario.isEmpty()) {
            Toast.makeText(this, "Categoría no definida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no iniciada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        verificarRolYContinuar();
    }

    private void verificarRolYContinuar() {
        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String rol = snapshot.child("rol").getValue(String.class);
                esAdmin = "admin".equalsIgnoreCase(rol);

                if (esAdmin) {
                    btnAgregarEvento.setVisibility(View.VISIBLE);
                    btnAgregarEvento.setOnClickListener(v -> mostrarDialogoEvento(null, "", "", "", "", "", "24"));
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("eventos_" + categoriaUsuario);
                } else {
                    btnAgregarEvento.setVisibility(View.GONE);
                    FirebaseMessaging.getInstance().subscribeToTopic("eventos_" + categoriaUsuario);
                }

                cargarDiasSemana();
                cargarEventosDesdeFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarioEventosActivity.this, "Error verificando rol", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDiasSemana() {
        layoutDiasSemana.removeAllViews();
        String[] dias = {"DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB"};
        Calendar hoy = Calendar.getInstance();
        int diaActual = hoy.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < dias.length; i++) {
            LinearLayout diaLayout = new LinearLayout(this);
            diaLayout.setOrientation(LinearLayout.VERTICAL);
            diaLayout.setPadding(16, 8, 16, 8);
            diaLayout.setGravity(Gravity.CENTER);

            TextView tvDia = new TextView(this);
            tvDia.setText(dias[i]);
            tvDia.setTextColor(Color.WHITE);
            tvDia.setGravity(Gravity.CENTER);

            TextView tvNumero = new TextView(this);
            hoy.set(Calendar.DAY_OF_WEEK, i + 1);
            int diaNum = hoy.get(Calendar.DAY_OF_MONTH);
            tvNumero.setText(String.valueOf(diaNum));
            tvNumero.setTextColor(Color.YELLOW);
            tvNumero.setGravity(Gravity.CENTER);

            if (i == diaActual) {
                tvDia.setTextColor(Color.BLACK);
                tvNumero.setTextColor(Color.BLACK);
                diaLayout.setBackgroundColor(Color.parseColor("#FFD700"));
            }

            diaLayout.addView(tvDia);
            diaLayout.addView(tvNumero);
            layoutDiasSemana.addView(diaLayout);
        }
    }

    private void cargarEventosDesdeFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("eventos");
        ref.orderByChild("categoria").equalTo(categoriaUsuario)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        layoutEventos.removeAllViews();

                        for (DataSnapshot eventoSnap : snapshot.getChildren()) {
                            String key = eventoSnap.getKey();
                            String tipo = eventoSnap.child("tipo").getValue(String.class);
                            String fecha = eventoSnap.child("fecha").getValue(String.class);
                            String descripcion = eventoSnap.child("descripcion").getValue(String.class);
                            String hora = eventoSnap.child("hora").getValue(String.class);
                            String expiracion = eventoSnap.child("expiracion").getValue(String.class);
                            String lugar = eventoSnap.child("lugar").getValue(String.class); // nuevo campo

                            if (!eventoExpirado(fecha, hora, expiracion)) {
                                agregarEvento(key, tipo, fecha, descripcion, hora, lugar, expiracion);
                            } else {
                                eventoSnap.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CalendarioEventosActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean eventoExpirado(String fecha, String hora, String expiracionHoras) {
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

    private void agregarEvento(String key, String tipo, String fecha, String descripcion, String hora, String lugar, String expiracion) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setBackgroundColor(Color.parseColor("#061C3B"));
        contenedor.setPadding(24, 24, 24, 24);
        contenedor.setElevation(8f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        contenedor.setLayoutParams(params);

        contenedor.addView(crearTexto("📌 Evento: " + tipo, Color.WHITE, 16, true));
        contenedor.addView(crearTexto("📅 Fecha: " + fecha, Color.LTGRAY, 14, false));
        contenedor.addView(crearTexto("📍 Lugar: " + lugar, Color.LTGRAY, 14, false));
        contenedor.addView(crearTexto("📝 Descripción: " + descripcion, Color.WHITE, 18, true));
        contenedor.addView(crearTexto("⏰ Hora: " + hora, Color.LTGRAY, 14, false));

        if (esAdmin) {
            Button btnAsistencia = new Button(this);
            btnAsistencia.setText("📋 Ver asistencia");
            btnAsistencia.setOnClickListener(v -> mostrarAsistenciaEvento(key));
            contenedor.addView(btnAsistencia);

            Button btnEliminar = new Button(this);
            btnEliminar.setText("❌ Eliminar evento");
            btnEliminar.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("¿Eliminar evento?")
                        .setMessage("¿Seguro que deseas eliminar este evento?")
                        .setPositiveButton("Sí", (dialog, which) ->
                                FirebaseDatabase.getInstance().getReference("eventos").child(key).removeValue())
                        .setNegativeButton("No", null)
                        .show();
            });

            contenedor.addView(btnEliminar);
            contenedor.setOnClickListener(v -> mostrarDialogoEvento(key, tipo, fecha, descripcion, hora, lugar, expiracion));
        } else {
            LinearLayout opciones = new LinearLayout(this);
            opciones.setOrientation(LinearLayout.HORIZONTAL);
            opciones.setGravity(Gravity.CENTER);
            opciones.setPadding(0, 16, 0, 0);

            Button btnSi = new Button(this);
            btnSi.setText("✅ Asistiré");
            btnSi.setOnClickListener(v -> confirmarAsistencia(key, "si"));

            Button btnNo = new Button(this);
            btnNo.setText("❌ No podré asistir");
            btnNo.setOnClickListener(v -> confirmarAsistencia(key, "no"));

            opciones.addView(btnSi);
            opciones.addView(btnNo);
            contenedor.addView(opciones);
        }

        layoutEventos.addView(contenedor);
    }

    private void mostrarDialogoEvento(String key, String tipoInit, String fechaInit, String descripcionInit, String horaInit, String lugarInit, String expiracionInit) {
        if (!esAdmin) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        EditText etTipo = new EditText(this);
        etTipo.setHint("📌 Tipo de evento");
        etTipo.setText(tipoInit);
        layout.addView(etTipo);

        EditText etFecha = new EditText(this);
        etFecha.setHint("📅 Fecha");
        etFecha.setFocusable(false);
        etFecha.setText(fechaInit);
        layout.addView(etFecha);

        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String fecha = String.format("%02d/%02d/%d", day, month + 1, year);
                etFecha.setText(fecha);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        EditText etDescripcion = new EditText(this);
        etDescripcion.setHint("📝 Descripción");
        etDescripcion.setText(descripcionInit);
        layout.addView(etDescripcion);

        EditText etHora = new EditText(this);
        etHora.setHint("⏰ Hora");
        etHora.setFocusable(false);
        etHora.setText(horaInit);
        layout.addView(etHora);

        etHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                String hora = String.format("%02d:%02d", hour, minute);
                etHora.setText(hora);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        EditText etLugar = new EditText(this);
        etLugar.setHint("📍 Lugar");
        etLugar.setText(lugarInit);
        layout.addView(etLugar);

        Spinner spinnerExpira = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"No borrar", "24 horas", "48 horas", "72 horas"});
        spinnerExpira.setAdapter(adapter);

        if (expiracionInit != null) {
            int index = switch (expiracionInit) {
                case "24" -> 1;
                case "48" -> 2;
                case "72" -> 3;
                default -> 0;
            };
            spinnerExpira.setSelection(index);
        }

        layout.addView(spinnerExpira);

        new AlertDialog.Builder(this)
                .setTitle(key == null ? "Nuevo Evento" : "Editar Evento")
                .setView(layout)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String tipo = etTipo.getText().toString();
                    String fecha = etFecha.getText().toString();
                    String descripcion = etDescripcion.getText().toString();
                    String hora = etHora.getText().toString();
                    String lugar = etLugar.getText().toString();
                    String expiracion = switch (spinnerExpira.getSelectedItemPosition()) {
                        case 1 -> "24";
                        case 2 -> "48";
                        case 3 -> "72";
                        default -> "0";
                    };

                    DatabaseReference ref = (key == null) ?
                            FirebaseDatabase.getInstance().getReference("eventos").push() :
                            FirebaseDatabase.getInstance().getReference("eventos").child(key);

                    ref.child("tipo").setValue(tipo);
                    ref.child("fecha").setValue(fecha);
                    ref.child("descripcion").setValue(descripcion);
                    ref.child("hora").setValue(hora);
                    ref.child("lugar").setValue(lugar);
                    ref.child("expiracion").setValue(expiracion);
                    ref.child("categoria").setValue(categoriaUsuario);

                    if (key == null) {
                        String titulo = "📅 Nuevo evento: " + tipo;
                        String cuerpo = descripcion + " - " + fecha + " " + hora;
                        String topic = "eventos_" + categoriaUsuario;
                        FCMHelperV1.enviarNotificacionATopic(this, topic, titulo, cuerpo);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarAsistencia(String eventoId, String respuesta) {
        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("asistencias")
                .child(eventoId)
                .child(uid);

        ref.setValue(respuesta).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "✅ Respuesta guardada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Error al guardar respuesta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarAsistenciaEvento(String eventoId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("asistencias")
                .child(eventoId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> uidsSi = new ArrayList<>();
                List<String> uidsNo = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String uid = snap.getKey();
                    String respuesta = snap.getValue(String.class);
                    if ("si".equals(respuesta)) {
                        uidsSi.add(uid);
                    } else {
                        uidsNo.add(uid);
                    }
                }

                DatabaseReference jugadoresRef = FirebaseDatabase.getInstance().getReference("jugadores");

                jugadoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        StringBuilder si = new StringBuilder("✅ Asistirán:\n");
                        StringBuilder no = new StringBuilder("❌ No asistirán:\n");

                        for (String uid : uidsSi) {
                            String nombre = dataSnapshot.child(uid).child("nombre").getValue(String.class);
                            si.append("• ").append(nombre != null ? nombre : uid).append("\n");
                        }
                        for (String uid : uidsNo) {
                            String nombre = dataSnapshot.child(uid).child("nombre").getValue(String.class);
                            no.append("• ").append(nombre != null ? nombre : uid).append("\n");
                        }

                        new AlertDialog.Builder(CalendarioEventosActivity.this)
                                .setTitle("📋 Asistencia")
                                .setMessage(si.toString() + "\n" + no.toString())
                                .setPositiveButton("Cerrar", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CalendarioEventosActivity.this, "Error al cargar nombres", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarioEventosActivity.this, "Error al cargar asistencia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TextView crearTexto(String texto, int color, float tamaño, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(color);
        tv.setTextSize(tamaño);
        tv.setGravity(Gravity.START);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }
}
