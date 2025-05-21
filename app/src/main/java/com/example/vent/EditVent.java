package com.example.vent;

import android.content.Context;
import android.content.Intent;
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

public class EditVent extends AppCompatActivity {

    ImageView closeBtn;
    EditText inputVent;
    LinearLayout saveBtn;
    FirebaseFirestore db;
    FirebaseUser user;

    RelativeLayout progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_vent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editVent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputVent = findViewById(R.id.inputVent);
        saveBtn = findViewById(R.id.saveBtn);
        progressOverlay = findViewById(R.id.progressIndicator);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        String postId = getIntent().getStringExtra("postId");
        String content = getIntent().getStringExtra("content");
        inputVent.setText(content);

        inputVent.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        saveBtn.setOnClickListener(v -> {
            String updatedMessage = inputVent.getText().toString().trim();

            progressOverlay.setVisibility(View.VISIBLE); // Show spinner

            if(updatedMessage.isEmpty()) {
                Toast.makeText(this, "Let it out, express something.", Toast.LENGTH_LONG).show();
                progressOverlay.setVisibility(View.GONE); // Show spinner
                return;
            }

            db.collection("vents").document(postId)
                    .update("content", updatedMessage, "timestamp", System.currentTimeMillis())
                    .addOnSuccessListener(unused -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Vent updated!", Toast.LENGTH_SHORT).show();


                        View view = this.getCurrentFocus();
                        if (view != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        startActivity(new Intent(EditVent.this, MyVents.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update.", Toast.LENGTH_SHORT).show();
            });
        });

        findViewById(R.id.editVent).setOnTouchListener(new View.OnTouchListener() {
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
                startActivity(new Intent(EditVent.this, MyVents.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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