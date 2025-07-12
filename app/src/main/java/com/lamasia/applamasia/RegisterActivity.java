package com.lamasia.applamasia;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFechaNacimiento, etCorreoAcudiente, etCedulaAcudiente, etNombreAcudiente, etTelefonoAcudiente, etDireccion, etOcupacion;
    private EditText etNombreJugador, etColegio, etPosicion, etEPS, etAlergias;
    private EditText etCorreo, etPassword;
    private Spinner spinnerTipoDocumento, spinnerCategoria;
    private CheckBox checkAutorizacion;
    private Button btnRegistrar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Referencias
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etNombreAcudiente = findViewById(R.id.etNombreAcudiente);
        etCedulaAcudiente = findViewById(R.id.etCedulaAcudiente);
        etTelefonoAcudiente = findViewById(R.id.etTelefonoAcudiente);
        etCorreoAcudiente = findViewById(R.id.etCorreoAcudiente);
        etDireccion = findViewById(R.id.etDireccion);
        etOcupacion = findViewById(R.id.etOcupacion);
        etNombreJugador = findViewById(R.id.etNombreJugador);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etColegio = findViewById(R.id.etColegio);
        etPosicion = findViewById(R.id.etPosicion);
        etEPS = findViewById(R.id.etEPS);
        etAlergias = findViewById(R.id.etAlergias);
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        checkAutorizacion = findViewById(R.id.checkAutorizacion);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Fecha nacimiento
        etFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(RegisterActivity.this, (view, year, month, day) -> {
                etFechaNacimiento.setText(day + "/" + (month + 1) + "/" + year);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        // Spinners
        String[] tiposDocumento = {"Cédula de ciudadanía", "Cédula extranjera", "Pasaporte", "Tarjeta de identidad"};
        ArrayAdapter<String> adapterDocumento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDocumento);
        adapterDocumento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(adapterDocumento);

        String[] categorias = {"Categoria 2020", "Categoria 2019", "Categoria 2018", "Categoria 2017", "Categoria 2016", "Categoria 2015"};
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Botón registrar
        btnRegistrar.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(correo)) {
                etCorreo.setError("Correo requerido");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Contraseña requerida");
                return;
            }
            if (!checkAutorizacion.isChecked()) {
                Toast.makeText(this, "Debe aceptar la autorización", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(correo, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
