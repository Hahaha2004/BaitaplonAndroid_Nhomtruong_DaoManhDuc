package com.example.thue_tro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";
    private TextView tvQuayLai, tvRoomType, tvPrice, tvAddress, tvFreedom, tvContact;
    private TextView tvElectricity, tvWater, tvService, tvWifi, tvPostDate;
    private LinearLayout contactContainer;
    private ViewPager2 viewPagerPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Ánh xạ
        tvQuayLai = findViewById(R.id.tvQuayLai);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvPrice = findViewById(R.id.tvPrice);
        tvAddress = findViewById(R.id.tvAddress);
        tvFreedom = findViewById(R.id.tvFreedom);
        tvContact = findViewById(R.id.tvContact);
        tvElectricity = findViewById(R.id.tvElectricity);
        tvWater = findViewById(R.id.tvWater);
        tvService = findViewById(R.id.tvService);
        tvWifi = findViewById(R.id.tvWifi);
        tvPostDate = findViewById(R.id.tvPostDate);
        viewPagerPhotos = findViewById(R.id.viewPagerPhotos);
        contactContainer = findViewById(R.id.contactContainer);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        PostFragment.Post post = (PostFragment.Post) intent.getSerializableExtra("post");

        if (post != null) {
            Log.d(TAG, "Received post: " + post.toString());

            // Hiển thị thông tin
            tvRoomType.setText(post.roomType != null ? post.roomType : "Không có");
            tvPrice.setText("Giá liên hệ: " + (post.price != null ? formatNumber(post.price) : "Không có") + "/tháng");
            tvAddress.setText((post.city != null ? post.city : "") + ", " +
                    (post.district != null ? post.district : "") + ", " +
                    (post.address != null ? post.address : "Không có"));
            tvFreedom.setText("Tự do");
            tvContact.setText("Liên hệ: " + (post.phone != null ? post.phone : "Không có") + " - " +
                    (post.fullName != null ? post.fullName : "Không có"));
            tvElectricity.setText(post.electricity != null && !post.electricity.isEmpty() ? formatNumber(post.electricity) + "/kWh" : "Không có");
            tvWater.setText(post.water != null && !post.water.isEmpty() ? formatNumber(post.water) + "/m³" : "Không có");
            tvService.setText(post.service != null && !post.service.isEmpty() ? formatNumber(post.service) + "/tháng" : "Không có");
            tvWifi.setText(post.wifi != null && !post.wifi.isEmpty() ? formatNumber(post.wifi) + "/tháng" : "Không có");
            tvPostDate.setText("Ngày đăng: " + (post.postDate != null ? post.postDate : "Không có"));

            // Thêm sự kiện click cho contactContainer để gọi điện
            contactContainer.setOnClickListener(v -> {
                String phoneNumber = post.phone != null ? post.phone : "";
                if (!phoneNumber.isEmpty()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(callIntent);
                } else {
                    Toast.makeText(this, "Số điện thoại không khả dụng", Toast.LENGTH_SHORT).show();
                }
            });

            // Hiển thị ảnh trong ViewPager2
            List<Bitmap> photoList = new ArrayList<>();
            if (post.photo1Uri != null && !post.photo1Uri.isEmpty()) {
                Bitmap bitmap1 = decodeBase64ToBitmap(post.photo1Uri);
                if (bitmap1 != null) photoList.add(bitmap1);
                else Log.e(TAG, "Failed to decode photo1Uri");
            }
            if (post.photo2Uri != null && !post.photo2Uri.isEmpty()) {
                Bitmap bitmap2 = decodeBase64ToBitmap(post.photo2Uri);
                if (bitmap2 != null) photoList.add(bitmap2);
                else Log.e(TAG, "Failed to decode photo2Uri");
            }
            if (post.photo3Uri != null && !post.photo3Uri.isEmpty()) {
                Bitmap bitmap3 = decodeBase64ToBitmap(post.photo3Uri);
                if (bitmap3 != null) photoList.add(bitmap3);
                else Log.e(TAG, "Failed to decode photo3Uri");
            }

            if (photoList.isEmpty()) {
                Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);
                photoList.add(defaultBitmap);
            }

            PhotoAdapter photoAdapter = new PhotoAdapter(this, photoList);
            viewPagerPhotos.setAdapter(photoAdapter);
        } else {
            Log.e(TAG, "Post is null");
            Toast.makeText(this, "Không thể tải thông tin bài đăng", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý nút Quay lại
        tvQuayLai.setOnClickListener(v -> finish());
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
            Log.e(TAG, "Error decoding Base64: " + e.getMessage());
            return null;
        }
    }
}