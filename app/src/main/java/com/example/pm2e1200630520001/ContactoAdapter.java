package com.example.pm2e1200630520001;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder> {

    private List<Contacto> listaContactos;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onLlamarClick(Contacto contacto);
        void onUbicacionClick(Contacto contacto);
        void onEditarClick(Contacto contacto);
        void onEliminarClick(Contacto contacto);
    }

    public ContactoAdapter(List<Contacto> listaContactos, Context context, OnItemClickListener listener) {
        this.listaContactos = listaContactos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        return new ContactoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Contacto contacto = listaContactos.get(position);

        holder.txtNombre.setText(contacto.getNombre());
        holder.txtTelefono.setText(contacto.getTelefono());
        holder.txtNota.setText(contacto.getNota());

        // Mostrar imagen si existe
        if (contacto.getFotoBase64() != null && !contacto.getFotoBase64().isEmpty()) {
            byte[] decodedBytes = Base64.decode(contacto.getFotoBase64(), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imgFoto.setImageBitmap(bmp);
        } else {
            holder.imgFoto.setImageResource(R.mipmap.ic_launcher); // imagen por defecto
        }

        // Botones
        holder.btnLlamar.setOnClickListener(v -> listener.onLlamarClick(contacto));
        holder.btnUbicacion.setOnClickListener(v -> listener.onUbicacionClick(contacto));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onEliminarClick(contacto);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    static class ContactoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtTelefono, txtNota;
        ImageView imgFoto;
        ImageButton btnLlamar, btnUbicacion;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreContacto);
            txtTelefono = itemView.findViewById(R.id.txtTelefonoContacto);
            txtNota = itemView.findViewById(R.id.txtNotaContacto);
            imgFoto = itemView.findViewById(R.id.imgFotoContacto);
            btnLlamar = itemView.findViewById(R.id.btnLlamar);
            btnUbicacion = itemView.findViewById(R.id.btnUbicacion);
        }
    }
}
