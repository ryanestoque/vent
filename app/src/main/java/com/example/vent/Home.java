package com.example.vent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    BottomNavigationView nav;
    ImageView goVentBtn;
    TextView emptyView;
    FirebaseFirestore db;
    FirebaseUser user;

    static boolean toasted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && !toasted) {
            Toast.makeText(this, "Signed in as " + user.getEmail(), Toast.LENGTH_LONG).show();
            toasted = true;
        }


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<Post> postList = new ArrayList<>();
        long now = System.currentTimeMillis();

        db = FirebaseFirestore.getInstance();
        PostAdapter adapter = new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        emptyView = findViewById(R.id.emptyView);

        db.collection("vents")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;

                    postList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId()); // Set Firestore document ID manually
                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    emptyView.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
                });

        goVentBtn = findViewById(R.id.goVent);

        goVentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, GoVent.class));
            }
        });

        nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already in Home, do nothing
                return true;
            } else if (itemId == R.id.nav_my_vents) {
                startActivity(new Intent(this, MyVents.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });


    }
}