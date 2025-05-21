package com.example.vent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class EditUsername extends AppCompatActivity {

    LinearLayout saveBtn;
    private FirebaseFirestore db;
    private FirebaseUser user;
    EditText anonUser;
    RelativeLayout progressOverlay;

    ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_username);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        anonUser = findViewById(R.id.anonUser);
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);
        progressOverlay = findViewById(R.id.progressIndicator);

        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String existingUsername = documentSnapshot.getString("username");
                            if (existingUsername != null) {
                               anonUser.setText(existingUsername);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
                    });
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressOverlay.setVisibility(View.VISIBLE); // Show spinner
                String newUsername = anonUser.getText().toString().trim();

                if (newUsername.isEmpty()) {
                    progressOverlay.setVisibility(View.GONE); // Show spinner
                    Toast.makeText(EditUsername.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentReference userRef = db.collection("users").document(user.getUid());
                userRef.update("username", newUsername)
                        .addOnSuccessListener(aVoid -> {
                            progressOverlay.setVisibility(View.GONE); // Show spinner
                            Toast.makeText(EditUsername.this, "Username updated!", Toast.LENGTH_SHORT).show();

                            db.collection("vents")
                                    .whereEqualTo("uid", user.getUid())
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        WriteBatch batch = db.batch();

                                        for (DocumentSnapshot doc : querySnapshot) {
                                            batch.update(doc.getReference(), "anonUsername", newUsername);
                                        }

                                        batch.commit()
                                                .addOnSuccessListener(unused -> {
                                                    // Toast.makeText(this, "All posts updated!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Toast.makeText(this, "Failed to update posts", Toast.LENGTH_SHORT).show();
                                                });
                                    });

                            startActivity(new Intent(EditUsername.this, Home.class));
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressOverlay.setVisibility(View.GONE); // Show spinner
                            Toast.makeText(EditUsername.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditUsername.this, Home.class));
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                        });
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}