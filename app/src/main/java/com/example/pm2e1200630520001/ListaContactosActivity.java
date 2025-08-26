package com.example.pm2e1200630520001;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaContactosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactoAdapter adapter;
    private List<Contacto> listaContactos;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        recyclerView = findViewById(R.id.recyclerContactos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaContactos = new ArrayList<>();
        firebaseHelper = new FirebaseHelper();

        adapter = new ContactoAdapter(listaContactos, this, new ContactoAdapter.OnItemClickListener() {
            @Override
            public void onLlamarClick(Contacto contacto) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contacto.getTelefono()));
                startActivity(intent);
            }

            @Override
            public void onUbicacionClick(Contacto contacto) {
                String uri = "geo:" + contacto.getLatitud() + "," + contacto.getLongitud() +
                        "?q=" + contacto.getLatitud() + "," + contacto.getLongitud() + "(" + contacto.getNombre() + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }

            @Override
            public void onEditarClick(Contacto contacto) {
                Toast.makeText(ListaContactosActivity.this, "Función Editar aún no implementada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEliminarClick(Contacto contacto) {
                firebaseHelper.eliminarContacto(contacto);
                Toast.makeText(ListaContactosActivity.this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Escucha cambios en Firebase
        firebaseHelper.getContactosRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaContactos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Contacto contacto = ds.getValue(Contacto.class);
                    if (contacto != null) {
                        listaContactos.add(contacto);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaContactosActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
