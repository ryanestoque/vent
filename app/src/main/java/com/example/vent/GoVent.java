package com.example.vent;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class GoVent extends AppCompatActivity {

    ImageView closeBtn;
    EditText inputVent;
    LinearLayout ventBtn;
    FirebaseFirestore db;
    FirebaseUser user;

    RelativeLayout progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_go_vent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goVent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputVent = findViewById(R.id.inputVent);
        ventBtn = findViewById(R.id.ventBtn);
        progressOverlay = findViewById(R.id.progressIndicator);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        inputVent.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        ventBtn.setOnClickListener(v -> {
            String postMessage = inputVent.getText().toString().trim();

            progressOverlay.setVisibility(View.VISIBLE); // Show spinner

            if(postMessage.isEmpty()) {
                Toast.makeText(this, "Let it out, express something.", Toast.LENGTH_LONG).show();
                progressOverlay.setVisibility(View.GONE); // Show spinner
                return;
            }

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        String username = doc.contains("username") ? doc.getString("username") : "Anonymous";

                        Post newPost = new Post(username, postMessage, 0, System.currentTimeMillis(), user.getUid().toString());

                        db.collection("vents")
                                .add(newPost)
                                .addOnSuccessListener(documentReference -> {
                                    progressOverlay.setVisibility(View.GONE); // Show spinner
                                    Toast.makeText(this, "Vented!", Toast.LENGTH_SHORT).show();
                                    inputVent.setText(""); // Clear input

                                    View view = this.getCurrentFocus();
                                    if (view != null) {
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressOverlay.setVisibility(View.GONE); // Show spinner
                                    Toast.makeText(this, "Failed to vent.", Toast.LENGTH_SHORT).show();
                                });
                    });
        });

        findViewById(R.id.goVent).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });

        closeBtn = findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}