package com.example.thue_tro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class PostedPostsActivity extends AppCompatActivity {

    private LinearLayout postListContainer;
    private DatabaseReference databaseReference;
    private DatabaseReference usersReference;
    private DatabaseReference adminsReference;
    private TextView tvQuayLai;
    private String currentUsername, userRole, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posted_posts);

        postListContainer = findViewById(R.id.postListContainer);
        tvQuayLai = findViewById(R.id.tvQuayLai);
        databaseReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("posts");
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        currentUsername = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("role");
        Log.d("PostedPostsActivity", "User role: " + userRole); // Log để kiểm tra role

        if (currentUsername == null) {
            finish();
            return;
        }

        tvQuayLai.setOnClickListener(v -> finish());

        loadUserPhoneAndPosts();
    }

    private void loadUserPhoneAndPosts() {
        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

        targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class);
                                if (user != null && user.soDienThoai != null) {
                                    userPhone = user.soDienThoai;
                                    loadPosts();
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PostedPosts", "Error fetching user: " + databaseError.getMessage());
                    }
                });
    }

    private void loadPosts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postListContainer.removeAllViews();

                if (!dataSnapshot.exists()) {
                    TextView noPostsText = new TextView(PostedPostsActivity.this);
                    noPostsText.setText("Chưa có bài đăng nào.");
                    noPostsText.setTextSize(16);
                    noPostsText.setPadding(16, 16, 16, 16);
                    postListContainer.addView(noPostsText);
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String postId = snapshot.getKey();
                    PostFragment.Post post = snapshot.getValue(PostFragment.Post.class);
                    if (post != null) {
                        // Admin thấy tất cả bài đăng, User chỉ thấy bài của mình dựa trên số điện thoại
                      if ("admin".equals(userRole) || (post.username != null && post.username.equals(currentUsername))) {
                            addPostToList(post, postId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("PostedPosts", "Error loading posts: " + databaseError.getMessage());
            }
        });
    }

    private void addPostToList(PostFragment.Post post, String postId) {
        View postView = LayoutInflater.from(this).inflate(R.layout.item_post, postListContainer, false);

        ImageView imgPhoto1 = postView.findViewById(R.id.imgPhoto1);
        TextView tvAddress = postView.findViewById(R.id.tvAddress);
        TextView tvContact = postView.findViewById(R.id.tvContact);
        TextView tvPrice = postView.findViewById(R.id.tvPrice);

        tvAddress.setText("Địa chỉ: " + (post.address != null ? post.address : "Không có"));
        tvContact.setText("Liên hệ: " + (post.phone != null ? post.phone : "Không có"));
        tvPrice.setText("Giá thuê: " + (post.price != null ? formatNumber(post.price) : "Không có") + "/tháng");

        if (post.photo1Uri != null && !post.photo1Uri.isEmpty()) {
            Bitmap bitmap = decodeBase64ToBitmap(post.photo1Uri);
            if (bitmap != null) {
                imgPhoto1.setImageBitmap(bitmap);
            } else {
                imgPhoto1.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgPhoto1.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        postView.setOnClickListener(v -> {
            Intent intent = new Intent(PostedPostsActivity.this, PostedPostDetailActivity.class);
            intent.putExtra("post", post);
            intent.putExtra("postId", postId);
            intent.putExtra("username", currentUsername);
            intent.putExtra("role", userRole);
            startActivity(intent);
        });

        postListContainer.addView(postView);
    }

    private void deletePost(String postId) {
        databaseReference.child(postId).removeValue()
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});
    }

    private String formatNumber(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            long number = Long.parseLong(value.replaceAll("[^0-9]", ""));
            return new DecimalFormat("#,###").format(number);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("PostedPosts", "Error decoding Base64: " + e.getMessage());
            return null;
        }
    }
}