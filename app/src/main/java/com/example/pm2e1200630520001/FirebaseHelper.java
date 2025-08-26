package com.example.pm2e1200630520001;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    private DatabaseReference contactosRef;

    public FirebaseHelper() {
        contactosRef = FirebaseDatabase.getInstance().getReference("contactos");
    }

    public void guardarContacto(Contacto contacto) {
        if (contacto.getId() == null) {
            String id = contactosRef.push().getKey();
            contacto.setId(id);
        }
        contactosRef.child(contacto.getId()).setValue(contacto);
    }

    public void eliminarContacto(Contacto contacto) {
        contactosRef.child(contacto.getId()).removeValue();
    }

    public DatabaseReference getContactosRef() {
        return contactosRef;
    }
}
