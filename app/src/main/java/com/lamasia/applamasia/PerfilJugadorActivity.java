package com.lamasia.applamasia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PerfilJugadorActivity extends AppCompatActivity {

    private ImageView ivFotoJugador;
    private EditText etIdentificacion, etNombre, etEdad, etCategoria, etEPS, etContactoEmergencia, etTelefonoEmergencia;
    private Button btnSeleccionarFoto, btnSeleccionarDocumento, btnEliminarDocumento, btnGuardarPerfil;

    private Uri uriFoto, uriDocumento;
    private DatabaseReference dbRef;
    private String uid;
    private String documentoUrlActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_nino);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = user.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("jugadores").child(uid);

        ivFotoJugador = findViewById(R.id.ivFotoJugador);
        etIdentificacion = findViewById(R.id.etIdentificacion);
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etCategoria = findViewById(R.id.etCategoria);
        etEPS = findViewById(R.id.etEPS);
        etContactoEmergencia = findViewById(R.id.etContactoEmergencia);
        etTelefonoEmergencia = findViewById(R.id.etTelefonoEmergencia);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnSeleccionarDocumento = findViewById(R.id.btnSeleccionarDocumento);
        btnEliminarDocumento = findViewById(R.id.btnEliminarDocumento);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);

        cargarDatosJugador();

        btnSeleccionarFoto.setOnClickListener(v -> seleccionarArchivo(1));
        btnSeleccionarDocumento.setOnClickListener(v -> seleccionarArchivo(2));
        btnEliminarDocumento.setOnClickListener(v -> eliminarDocumento());
        btnGuardarPerfil.setOnClickListener(v -> guardarDatosJugador());
    }

    private void seleccionarArchivo(int tipo) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(tipo == 1 ? "image/*" : "application/pdf");
        startActivityForResult(intent, tipo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == 1) {
                uriFoto = uri;
                ivFotoJugador.setImageURI(uri);
                subirArchivo(uriFoto, "fotos", "urlFoto");
            } else if (requestCode == 2) {
                uriDocumento = uri;
                Toast.makeText(this, "📎 Documento seleccionado", Toast.LENGTH_SHORT).show();
                subirArchivo(uriDocumento, "documentos", "urlDocumento");
            }
        }
    }

    private void subirArchivo(Uri archivoUri, String carpeta, String campoBD) {
        if (archivoUri == null) return;

        String nombreArchivo = uid + "_" + System.currentTimeMillis();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(carpeta).child(nombreArchivo);

        storageRef.putFile(archivoUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            dbRef.child(campoBD).setValue(uri.toString());
                            if (campoBD.equals("urlFoto")) {
                                Glide.with(this).load(uri.toString()).into(ivFotoJugador);
                            }
                            if (campoBD.equals("urlDocumento")) {
                                documentoUrlActual = uri.toString();
                                btnEliminarDocumento.setVisibility(View.VISIBLE);
                            }
                            Toast.makeText(this, "✅ Archivo subido correctamente", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error al subir archivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cargarDatosJugador() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(PerfilJugadorActivity.this, "📝 Completa el formulario y guarda los datos.", Toast.LENGTH_LONG).show();
                    return;
                }

                etIdentificacion.setText(snapshot.child("identificacion").getValue(String.class));
                etNombre.setText(snapshot.child("nombre").getValue(String.class));
                etEdad.setText(snapshot.child("edad").getValue(String.class));
                etCategoria.setText(snapshot.child("categoria").getValue(String.class));
                etEPS.setText(snapshot.child("eps").getValue(String.class));
                etContactoEmergencia.setText(snapshot.child("contactoEmergencia").getValue(String.class));
                etTelefonoEmergencia.setText(snapshot.child("telefonoEmergencia").getValue(String.class));

                String urlFoto = snapshot.child("urlFoto").getValue(String.class);
                documentoUrlActual = snapshot.child("urlDocumento").getValue(String.class);

                if (urlFoto != null && !urlFoto.isEmpty()) {
                    Glide.with(PerfilJugadorActivity.this).load(urlFoto).into(ivFotoJugador);
                }

                if (documentoUrlActual != null && !documentoUrlActual.isEmpty()) {
                    btnEliminarDocumento.setVisibility(View.VISIBLE);
                } else {
                    btnEliminarDocumento.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PerfilJugadorActivity.this, "Error al cargar perfil: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarDocumento() {
        if (documentoUrlActual != null && !documentoUrlActual.isEmpty()) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(documentoUrlActual);
            storageRef.delete()
                    .addOnSuccessListener(unused -> {
                        dbRef.child("urlDocumento").removeValue()
                                .addOnSuccessListener(unused2 -> {
                                    documentoUrlActual = "";
                                    btnEliminarDocumento.setVisibility(View.GONE);
                                    Toast.makeText(this, "✅ Documento eliminado", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "❌ No se pudo actualizar en la base de datos", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ Error al eliminar del almacenamiento", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "⚠️ No hay documento que eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarDatosJugador() {
        String identificacion = etIdentificacion.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String eps = etEPS.getText().toString().trim();
        String contacto = etContactoEmergencia.getText().toString().trim();
        String telefono = etTelefonoEmergencia.getText().toString().trim();

        if (identificacion.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "Por favor, completa al menos identificación y nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child("identificacion").setValue(identificacion);
        dbRef.child("nombre").setValue(nombre);
        dbRef.child("edad").setValue(edad);
        dbRef.child("categoria").setValue(categoria);
        dbRef.child("eps").setValue(eps);
        dbRef.child("contactoEmergencia").setValue(contacto);
        dbRef.child("telefonoEmergencia").setValue(telefono);

        Toast.makeText(this, "✅ Perfil actualizado correctamente", Toast.LENGTH_LONG).show();
    }
}

