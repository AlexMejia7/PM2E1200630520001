package com.example.pm2e1200630520001;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int REQUEST_PERMISSIONS = 200;

    private EditText edtNombre, edtTelefono, edtNota, edtLat, edtLng;
    private Button btnGuardar, btnLista, btnFoto, btnAudio;
    private ImageView imgVistaPrevia;
    private FirebaseHelper firebaseHelper;

    private String fotoBase64 = "";
    private String audioBase64 = "";

    private MediaRecorder recorder;
    private String audioPath;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        edtNombre = findViewById(R.id.edtNombre);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtNota = findViewById(R.id.edtNota);
        edtLat = findViewById(R.id.edtLatitud);
        edtLng = findViewById(R.id.edtLongitud);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnLista = findViewById(R.id.btnVerLista);
        btnFoto = findViewById(R.id.btnFoto);
        btnAudio = findViewById(R.id.btnAudio);
        imgVistaPrevia = findViewById(R.id.imgVistaPrevia);

        firebaseHelper = new FirebaseHelper();

        // Botones
        btnFoto.setOnClickListener(v -> seleccionarImagen());
        btnAudio.setOnClickListener(v -> toggleGrabarAudio());

        btnGuardar.setOnClickListener(v -> guardarContacto());
        btnLista.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaContactosActivity.class)));

        // Solicitar permisos si es Android >= 6
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!tienePermisos()) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        }
    }

    private boolean tienePermisos() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Selección de imagen
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imgVistaPrevia.setImageBitmap(bitmap);
                    fotoBase64 = convertirBase64(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String convertirBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    // Grabar audio
    private void toggleGrabarAudio() {
        if (!isRecording) {
            iniciarGrabacion();
        } else {
            detenerGrabacion();
        }
    }

    private void iniciarGrabacion() {
        try {
            File audioFile = File.createTempFile("audio_", ".3gp", getCacheDir());
            audioPath = audioFile.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioPath);
            recorder.prepare();
            recorder.start();

            isRecording = true;
            btnAudio.setText("Detener Grabación");
            Toast.makeText(this, "Grabando audio...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerGrabacion() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
            btnAudio.setText("Grabar Audio");

            // Convertir a Base64
            audioBase64 = convertirArchivoBase64(audioPath);

            Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertirArchivoBase64(String path) {
        try {
            File file = new File(path);
            byte[] bytes = new byte[(int) file.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            fis.read(bytes);
            fis.close();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Guardar contacto
    private void guardarContacto() {
        String nombre = edtNombre.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String nota = edtNota.getText().toString().trim();
        double lat = edtLat.getText().toString().isEmpty() ? 0 : Double.parseDouble(edtLat.getText().toString());
        double lng = edtLng.getText().toString().isEmpty() ? 0 : Double.parseDouble(edtLng.getText().toString());

        Contacto contacto = new Contacto(null, nombre, telefono, nota, lat, lng, fotoBase64, audioBase64);
        firebaseHelper.guardarContacto(contacto);
        Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();

        limpiarCampos();
    }

    private void limpiarCampos() {
        edtNombre.setText("");
        edtTelefono.setText("");
        edtNota.setText("");
        edtLat.setText("");
        edtLng.setText("");
        imgVistaPrevia.setImageResource(R.mipmap.ic_launcher);
        fotoBase64 = "";
        audioBase64 = "";
    }
}
