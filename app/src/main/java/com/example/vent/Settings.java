package com.example.vent;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {

    BottomNavigationView nav;
    LinearLayout changeUsernameBtn;
    LinearLayout signOutBtn;

    LinearLayout aboutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        changeUsernameBtn = findViewById(R.id.changeUsername);
        signOutBtn = findViewById(R.id.signOut);
        aboutBtn = findViewById(R.id.aboutVent);

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("About Vent")
                        .setMessage("Vent is a safe and anonymous platform for sharing thoughts.\n\nDeveloped by:\nRyan Christopher Estoque\nAltair Lawrence Dela Cruz\nJames David Asoy\nJhon Dominic Albacite\n\nBSIT - 2C")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        changeUsernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this, EditUsername.class));
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this).setTitle("Sign Out")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton("Sign Out", (dialog, which) -> {

                            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                                    Settings.this,
                                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestIdToken(getString(R.string.default_web_client_id))
                                            .requestEmail()
                                            .build()
                            );

                            googleSignInClient.signOut().addOnCompleteListener(task -> {
                                FirebaseAuth.getInstance().signOut(); // 👈 Sign out from Firebase

                                Intent intent = new Intent(Settings.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear back stack
                                startActivity(intent);
                                finish();
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_settings);

        nav.setOnItemSelectedListener(item ->

        {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, Home.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_my_vents) {
                startActivity(new Intent(this, MyVents.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }

            return false;
        });
    }
}