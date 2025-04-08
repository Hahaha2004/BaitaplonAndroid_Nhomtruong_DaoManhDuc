package com.example.thue_tro;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PostedPostDetailActivity extends AppCompatActivity {

    private TextView tvQuayLai, tvRoomType, tvPrice, tvAddress, tvContact, tvFreedom;
    private TextView tvElectricity, tvWater, tvService, tvWifi, tvUserName, tvEmail;
    private LinearLayout contactContainer;
    private ViewPager2 viewPagerPhotos;
    private Button btnDelete;
    private DatabaseReference databaseReference;
    private DatabaseReference usersReference;
    private DatabaseReference adminsReference;
    private String postId, currentUsername, userRole, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posted_post_detail);

        // Ánh xạ
        tvQuayLai = findViewById(R.id.tvQuayLai);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvPrice = findViewById(R.id.tvPrice);
        tvAddress = findViewById(R.id.tvAddress);
        tvContact = findViewById(R.id.tvContact);
        tvFreedom = findViewById(R.id.tvFreedom);
        tvElectricity = findViewById(R.id.tvElectricity);
        tvWater = findViewById(R.id.tvWater);
        tvService = findViewById(R.id.tvService);
        tvWifi = findViewById(R.id.tvWifi);
        viewPagerPhotos = findViewById(R.id.viewPagerPhotos);
        btnDelete = findViewById(R.id.btnDelete);
        contactContainer = findViewById(R.id.contactContainer);

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("posts");
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        PostFragment.Post post = (PostFragment.Post) intent.getSerializableExtra("post");
        postId = intent.getStringExtra("postId");
        currentUsername = intent.getStringExtra("username");
        userRole = intent.getStringExtra("role");
        Log.d("PostedPostDetailActivity", "User role: " + userRole);

        // Lấy số điện thoại người dùng hiện tại và hiển thị chi tiết bài đăng
        loadUserPhone(post);

        // Xử lý nút "Quay lại"
        tvQuayLai.setOnClickListener(v -> finish());
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage(message)
                .setIcon(R.drawable.ic_check_green)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có muốn xóa bài đăng này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Thực hiện xóa khi người dùng chọn "Có"
                    deletePost();
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    // Đóng dialog khi người dùng chọn "Không"
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void loadUserPhone(PostFragment.Post post) {
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
                                    displayPostDetails(post);
                                    return;
                                }
                            }
                            showErrorDialog("Không tìm thấy số điện thoại người dùng");
                        } else {
                            showErrorDialog("Không tìm thấy thông tin người dùng");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showErrorDialog("Lỗi: " + databaseError.getMessage());
                    }
                });
    }

    private void displayPostDetails(PostFragment.Post post) {
        if (post != null) {
            tvRoomType.setText(post.roomType != null ? post.roomType : "Không có");
            tvPrice.setText("Giá liên hệ: " + (post.price != null ? formatNumber(post.price) : "Không có") + "/tháng");
            tvAddress.setText((post.city != null ? post.city : "") + ", " +
                    (post.district != null ? post.district : "") + ", " +
                    (post.address != null ? post.address : "Không có"));
            tvFreedom.setText("Tự do");
            tvElectricity.setText(post.electricity != null && !post.electricity.isEmpty() ? formatNumber(post.electricity) + "/kWh" : "Không có");
            tvWater.setText(post.water != null && !post.water.isEmpty() ? formatNumber(post.water) + "/m³" : "Không có");
            tvService.setText(post.service != null && !post.service.isEmpty() ? formatNumber(post.service) + "/tháng" : "Không có");
            tvWifi.setText(post.wifi != null && !post.wifi.isEmpty() ? formatNumber(post.wifi) + "/tháng" : "Không có");

            // Load thông tin người đăng
            loadUserInfo(post);

            // Sự kiện gọi điện
            contactContainer.setOnClickListener(v -> {
                String phoneNumber = post.phone != null ? post.phone : "";
                if (!phoneNumber.isEmpty()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(callIntent);
                } else {
                    showErrorDialog("Số điện thoại không khả dụng");
                }
            });

            // Hiển thị ảnh
            List<Bitmap> photoList = new ArrayList<>();
            if (post.photo1Uri != null && !post.photo1Uri.isEmpty()) {
                Bitmap bitmap1 = decodeBase64ToBitmap(post.photo1Uri);
                if (bitmap1 != null) photoList.add(bitmap1);
            }
            if (post.photo2Uri != null && !post.photo2Uri.isEmpty()) {
                Bitmap bitmap2 = decodeBase64ToBitmap(post.photo2Uri);
                if (bitmap2 != null) photoList.add(bitmap2);
            }
            if (post.photo3Uri != null && !post.photo3Uri.isEmpty()) {
                Bitmap bitmap3 = decodeBase64ToBitmap(post.photo3Uri);
                if (bitmap3 != null) photoList.add(bitmap3);
            }

            if (photoList.isEmpty()) {
                Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);
                photoList.add(defaultBitmap);
            }

            PhotoAdapter photoAdapter = new PhotoAdapter(this, photoList);
            viewPagerPhotos.setAdapter(photoAdapter);

            // Kiểm tra quyền xóa
            if ("admin".equals(userRole) || (post.phone != null && post.phone.equals(userPhone))) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> showConfirmDeleteDialog()); // Hiển thị dialog xác nhận khi nhấn nút xóa
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        } else {
            showErrorDialog("Không thể tải thông tin bài đăng");
            finish();
        }
    }

    private void loadUserInfo(PostFragment.Post post) {
        String phone = post.phone != null ? post.phone : "";
        if (phone.isEmpty()) {
            tvContact.setText("Liên hệ: " + phone + " - Không có thông tin");
            if (tvUserName != null) tvUserName.setText("Người đăng: Không có thông tin");
            if (tvEmail != null) tvEmail.setText("Email: Không có thông tin");
            return;
        }

        // Tìm trong node users trước
        usersReference.orderByChild("soDienThoai").equalTo(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class);
                                if (user != null) {
                                    String fullName = user.nguoiDung != null ? user.nguoiDung : "Không có tên";
                                    tvContact.setText("Liên hệ: " + phone + " - " + fullName);
                                    if (tvUserName != null) tvUserName.setText("Người đăng: " + fullName);
                                    if (tvEmail != null) tvEmail.setText("Email: " + (user.email != null ? user.email : "Không có email"));
                                    return;
                                }
                            }
                        } else {
                            // Nếu không tìm thấy trong users, tìm trong admins
                            adminsReference.orderByChild("soDienThoai").equalTo(phone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    DangKi.User user = snapshot.getValue(DangKi.User.class);
                                                    if (user != null) {
                                                        String fullName = user.nguoiDung != null ? user.nguoiDung : "Không có tên";
                                                        tvContact.setText("Liên hệ: " + phone + " - " + fullName);
                                                        if (tvUserName != null) tvUserName.setText("Người đăng: " + fullName);
                                                        if (tvEmail != null) tvEmail.setText("Email: " + (user.email != null ? user.email : "Không có email"));
                                                        return;
                                                    }
                                                }
                                            } else {
                                                tvContact.setText("Liên hệ: " + phone + " - Không có thông tin");
                                                if (tvUserName != null) tvUserName.setText("Người đăng: Không có thông tin");
                                                if (tvEmail != null) tvEmail.setText("Email: Không có thông tin");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("PostedPostDetail", "Error fetching admin info: " + databaseError.getMessage());
                                            tvContact.setText("Liên hệ: " + phone + " - Lỗi tải thông tin");
                                            if (tvUserName != null) tvUserName.setText("Người đăng: Lỗi tải thông tin");
                                            if (tvEmail != null) tvEmail.setText("Email: Lỗi tải thông tin");
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PostedPostDetail", "Error fetching user info: " + databaseError.getMessage());
                        tvContact.setText("Liên hệ: " + phone + " - Lỗi tải thông tin");
                        if (tvUserName != null) tvUserName.setText("Người đăng: Lỗi tải thông tin");
                        if (tvEmail != null) tvEmail.setText("Email: Lỗi tải thông tin");
                    }
                });
    }

    private void deletePost() {
        if (postId != null) {
            databaseReference.child(postId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        showSuccessDialog("Xóa bài đăng thành công");
                        finish();
                    })
                    .addOnFailureListener(e -> showErrorDialog("Lỗi khi xóa bài đăng: " + e.getMessage()));
        } else {
            showErrorDialog("Không tìm thấy ID bài đăng");
        }
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
            e.printStackTrace();
            return null;
        }
    }
}