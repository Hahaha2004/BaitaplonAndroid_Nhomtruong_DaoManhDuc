package com.example.thue_tro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private EditText edtSearch;
    private ImageView imgBanner1, imgBanner2, imgBanner3;
    private Button btnChangeBanner1, btnChangeBanner2, btnChangeBanner3;
    private LinearLayout adminBannerControls;
    private DatabaseReference bannerRef;
    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;
    private static final int PICK_IMAGE_REQUEST_3 = 3;
    private String userRole;
    private int currentImageSelection;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ các view
        edtSearch = view.findViewById(R.id.edtSearch);
        imgBanner1 = view.findViewById(R.id.imgBanner1);
        imgBanner2 = view.findViewById(R.id.imgBanner2);
        imgBanner3 = view.findViewById(R.id.imgBanner3);
        btnChangeBanner1 = view.findViewById(R.id.btnChangeBanner1);
        btnChangeBanner2 = view.findViewById(R.id.btnChangeBanner2);
        btnChangeBanner3 = view.findViewById(R.id.btnChangeBanner3);
        adminBannerControls = view.findViewById(R.id.adminBannerControls);

        bannerRef = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("banners");

        // Lấy role từ Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            userRole = bundle.getString("role");
            Log.d("HomeFragment", "User role: " + userRole); // Log để kiểm tra role
        }

        // Kiểm tra vai trò người dùng
        if ("admin".equals(userRole)) {
            adminBannerControls.setVisibility(View.VISIBLE);
        } else {
            adminBannerControls.setVisibility(View.GONE);
        }

        // Tạo node banners nếu chưa tồn tại
        initializeBanners();

        // Xử lý sự kiện nhấn vào thanh tìm kiếm
        edtSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                SearchFragment searchFragment = new SearchFragment();
                Bundle searchBundle = new Bundle();
                searchBundle.putString("username", bundle != null ? bundle.getString("username") : null);
                searchBundle.putString("role", userRole);
                searchFragment.setArguments(searchBundle);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, searchFragment)
                        .addToBackStack(null)
                        .commit();
                getActivity().findViewById(R.id.navigation_search).performClick();
            }
        });

        // Xử lý thay đổi banner (chỉ admin)
        btnChangeBanner1.setOnClickListener(v -> openImagePicker(PICK_IMAGE_REQUEST_1));
        btnChangeBanner2.setOnClickListener(v -> openImagePicker(PICK_IMAGE_REQUEST_2));
        btnChangeBanner3.setOnClickListener(v -> openImagePicker(PICK_IMAGE_REQUEST_3));

        // Tải banner từ Firebase
        loadBanners();

        return view;
    }

    private void initializeBanners() {
        // Kiểm tra xem node banners đã tồn tại chưa
        bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Nếu node banners chưa tồn tại, tạo node với các banner mặc định
                    Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);
                    String defaultBase64 = bitmapToBase64(defaultBitmap);

                    bannerRef.child("banner1").setValue(defaultBase64);
                    bannerRef.child("banner2").setValue(defaultBase64);
                    bannerRef.child("banner3").setValue(defaultBase64);
                    Log.d("HomeFragment", "Created banners node with default images");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeFragment", "Error initializing banners: " + databaseError.getMessage());
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    //lấy ảnh từ thư viện máy ảo
    private void openImagePicker(int requestCode) {
        currentImageSelection = requestCode;
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            android.net.Uri imageUri = data.getData();
            if (imageUri != null) {
                String bannerKey = "banner" + (requestCode - PICK_IMAGE_REQUEST_1 + 1);
                try {
                    Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    // Hiển thị ảnh lên ImageView ngay lập tức
                    switch (requestCode) {
                        case PICK_IMAGE_REQUEST_1:
                            imgBanner1.setImageBitmap(bitmap);
                            break;
                        case PICK_IMAGE_REQUEST_2:
                            imgBanner2.setImageBitmap(bitmap);
                            break;
                        case PICK_IMAGE_REQUEST_3:
                            imgBanner3.setImageBitmap(bitmap);
                            break;
                    }
                    // Chuyển ảnh thành Base64 và lưu vào Realtime Database
                    uploadImageToFirebase(imageUri, bannerKey);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Lưu ảnh lên firebase
    private void uploadImageToFirebase(android.net.Uri imageUri, String bannerKey) {
        try {
            // Chuyển đổi hình ảnh từ Uri thành Bitmap
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Lưu chuỗi Base64 vào Realtime Database
            bannerRef.child(bannerKey).setValue(base64String)
                    .addOnSuccessListener(aVoid -> {
                        // Thay Toast bằng Log để debug
                        Log.d("HomeFragment", "Cập nhật banner thành công: " + bannerKey);
                    })
                    .addOnFailureListener(e -> {
                        // Thay Toast bằng Log để debug
                        Log.e("HomeFragment", "Lỗi cập nhật banner: " + e.getMessage());
                    });
        } catch (IOException e) {
            // Thay Toast bằng Log để debug
            Log.e("HomeFragment", "Lỗi chuyển đổi ảnh: " + e.getMessage());
        }
    }

    private void loadBanners() {
        bannerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String bannerKey = snapshot.getKey();
                    String base64String = snapshot.getValue(String.class);
                    if (base64String != null && !base64String.isEmpty()) {
                        Bitmap bitmap = decodeBase64ToBitmap(base64String);
                        if (bitmap != null) {
                            switch (bannerKey) {
                                case "banner1":
                                    imgBanner1.setImageBitmap(bitmap);
                                    break;
                                case "banner2":
                                    imgBanner2.setImageBitmap(bitmap);
                                    break;
                                case "banner3":
                                    imgBanner3.setImageBitmap(bitmap);
                                    break;
                            }
                        }
                    } else {
                        // Nếu không có ảnh, hiển thị ảnh mặc định
                        switch (bannerKey) {
                            case "banner1":
                                imgBanner1.setImageResource(android.R.drawable.ic_menu_gallery);
                                break;
                            case "banner2":
                                imgBanner2.setImageResource(android.R.drawable.ic_menu_gallery);
                                break;
                            case "banner3":
                                imgBanner3.setImageResource(android.R.drawable.ic_menu_gallery);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Lỗi tải banner: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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