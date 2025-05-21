package com.example.vent;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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


public class MyVents extends AppCompatActivity {
    BottomNavigationView nav;
    FirebaseFirestore db;
    FirebaseUser user;
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_vents);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myVents), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(null);
        List<Post> myPosts = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        MyVentsAdapter adapter = new MyVentsAdapter(this, myPosts, new MyVentsAdapter.MyPostListener() {
            @Override
            public void onEdit(Post post, String postId) {
                Intent intent = new Intent(MyVents.this, EditVent.class);
                intent.putExtra("postId", postId);
                intent.putExtra("content", post.getContent());
                startActivity(intent);
                finish();
            }

            @Override
            public void onDelete(String postId) {
                new AlertDialog.Builder(MyVents.this) // 'context' must be passed to your adapter
                        .setTitle("Delete Vent")
                        .setMessage("Are you sure you want to delete this vent?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                db.collection("vents").document(postId)
                        .delete()
                        .addOnSuccessListener(unused -> {
                                Toast.makeText(MyVents.this, "Vent deleted!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MyVents.this, MyVents.class));
                                overridePendingTransition(0, 0);
                                finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(MyVents.this, "Failed to delete.", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            emptyView = findViewById(R.id.emptyMyView);

            db.collection("vents")
                    .whereEqualTo("uid", user.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null || querySnapshot == null) return;

                        myPosts.clear(); // Clear old data
                        for (DocumentSnapshot doc : querySnapshot) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setId(doc.getId()); // Set Firestore document ID manually
                                myPosts.add(post);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        emptyView.setVisibility(myPosts.isEmpty() ? View.VISIBLE : View.GONE);
                    });


        nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_my_vents);

        nav.setOnItemSelectedListener(item ->

        {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, Home.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_my_vents) {
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