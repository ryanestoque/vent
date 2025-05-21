package com.example.vent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MyVentsAdapter extends RecyclerView.Adapter<MyVentsAdapter.MyVentsViewHolder> {

    public interface MyPostListener {
        void onEdit(Post post, String postId);
        void onDelete(String postId);
    }
    private List<Post> postList;
    private MyPostListener listener;

    private Context context;

    public MyVentsAdapter(Context context, List<Post> postList, MyPostListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
    }

    public static class MyVentsViewHolder extends RecyclerView.ViewHolder {
        TextView anonUsername, postContent, feelCount, postDate;
        ImageView menuButton;
        ImageView feelBtn;

        public MyVentsViewHolder(View itemView) {
            super(itemView);
            anonUsername = itemView.findViewById(R.id.anonUsername);
            postContent = itemView.findViewById(R.id.postContent);
            feelCount = itemView.findViewById(R.id.feelCount);
            postDate = itemView.findViewById(R.id.postDate);
            menuButton = itemView.findViewById(R.id.postMenu);
            feelBtn = itemView.findViewById(R.id.feelBtn);
        }
    }

    @NonNull
    @Override
    public MyVentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new MyVentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVentsViewHolder holder, int position) {
        holder.menuButton.setVisibility(View.VISIBLE);

        holder.menuButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.post_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit_post) {
                    listener.onEdit(postList.get(position), postList.get(position).getId());
                    return true;
                } else if (item.getItemId() == R.id.delete_post) {
                    listener.onDelete(postList.get(position).getId());
                    return true;
                }
                return false;
            });
            popup.show();
        });

        Post post = postList.get(position);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.anonUsername.setText(post.getAnonUsername());
        holder.postContent.setText(post.getContent());
        if(post.getFeelCount() == 1) {
            holder.feelCount.setText("" + post.getFeelCount() + " feel");
        } else {
            holder.feelCount.setText("" + post.getFeelCount() + " feels");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        String date = sdf.format(new Date(post.getTimestamp()));
        holder.postDate.setText(date);

        // Set icon based on whether user has "felt"
        boolean isFelt = post.getFeelUsers() != null && post.getFeelUsers().contains(currentUser.getUid());
        holder.feelBtn.setImageResource(isFelt ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_border_24);

        // Click to toggle feel
        holder.feelBtn.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference postRef = db.collection("vents").document(post.getId());

            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(postRef);
                List<String> feelUsers = (List<String>) snapshot.get("feelUsers");
                Long feelCount = snapshot.getLong("feelCount");

                if (feelUsers == null) feelUsers = new ArrayList<>();
                if (feelCount == null) feelCount = 0L;

                if (feelUsers.contains(currentUser.getUid())) {
                    // Unlike
                    feelUsers.remove(currentUser.getUid());
                    feelCount--;
                } else {
                    // Like
                    feelUsers.add(currentUser.getUid());
                    feelCount++;
                }

                transaction.update(postRef, "feelUsers", feelUsers);
                transaction.update(postRef, "feelCount", feelCount);
                return null;
            }).addOnSuccessListener(unused -> {
                // Optional: Update locally for immediate feedback
                if (post.getFeelUsers() == null) post.setFeelUsers(new ArrayList<>());
                if (post.getFeelUsers().contains(currentUser.getUid())) {
                    post.getFeelUsers().remove(currentUser.getUid());
                    post.setFeelCount(post.getFeelCount() - 1);
                } else {
                    post.getFeelUsers().add(currentUser.getUid());
                    post.setFeelCount(post.getFeelCount() + 1);
                }

                notifyItemChanged(position);
            });
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}

