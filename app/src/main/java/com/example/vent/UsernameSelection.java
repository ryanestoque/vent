package com.example.vent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UsernameSelection extends AppCompatActivity {

    LinearLayout proceedBtn;

    private FirebaseFirestore db;
    private FirebaseUser user;

    RelativeLayout progressOverlay;

    EditText anonUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_username_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        anonUser = findViewById(R.id.anonUser);
        progressOverlay = findViewById(R.id.progressIndicator);

        if (user != null) {
            Toast.makeText(this, "Signed in as " + user.getEmail(), Toast.LENGTH_LONG).show();
        }

        proceedBtn = findViewById(R.id.proceedBtn);

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressOverlay.setVisibility(View.VISIBLE); // Show spinner

                String enteredUsername = anonUser.getText().toString().trim();

                if (enteredUsername.isEmpty()) {
                    progressOverlay.setVisibility(View.GONE); // Show spinner
                    Toast.makeText(UsernameSelection.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users")
                        .whereEqualTo("username", enteredUsername)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Username already taken
                                progressOverlay.setVisibility(View.GONE); // Show spinner
                                Toast.makeText(UsernameSelection.this, "Username is already taken.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Username is available — proceed to save
                                Map<String, Object> username = new HashMap<>();
                                username.put("username", enteredUsername);

                                db.collection("users").document(user.getUid())
                                        .set(username, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            progressOverlay.setVisibility(View.GONE); // Show spinner
                                            startActivity(new Intent(UsernameSelection.this, Home.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UsernameSelection.this, "Error saving username.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(UsernameSelection.this, "Failed to check username.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}