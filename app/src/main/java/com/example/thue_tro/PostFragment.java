package com.example.thue_tro;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostFragment extends Fragment {

    private EditText edtFullName, edtPhone, edtPostDate, edtAddress, edtPrice, edtElectricity, edtWater, edtService, edtWifi;
    private TextView tvCity, tvFullNameError, tvPhoneError, tvPostDateError, tvDistrictError, tvAddressError, tvRoomTypeError, tvPriceError;
    private Spinner spinnerDistrict, spinnerRoomType;
    private ImageView imgPhoto1, imgPhoto2, imgPhoto3;
    private Button btnPost;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri1, imageUri2, imageUri3;
    private int currentImageSelection = 0;
    private String postId;
    private boolean isEditMode = false;
    private String currentUsername;
    private String selectedDistrict, selectedRoomType;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        // Ánh xạ các view
        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtPostDate = view.findViewById(R.id.edtPostDate);
        edtAddress = view.findViewById(R.id.edtAddress);
        edtPrice = view.findViewById(R.id.edtPrice);
        edtElectricity = view.findViewById(R.id.edtElectricity);
        edtWater = view.findViewById(R.id.edtWater);
        edtService = view.findViewById(R.id.edtService);
        edtWifi = view.findViewById(R.id.edtWifi);
        tvCity = view.findViewById(R.id.tvCity);
        spinnerDistrict = view.findViewById(R.id.spinnerDistrict);
        spinnerRoomType = view.findViewById(R.id.spinnerRoomType);
        imgPhoto1 = view.findViewById(R.id.imgPhoto1);
        imgPhoto2 = view.findViewById(R.id.imgPhoto2);
        imgPhoto3 = view.findViewById(R.id.imgPhoto3);
        btnPost = view.findViewById(R.id.btnPost);

        // Ánh xạ các TextView lỗi
        tvFullNameError = view.findViewById(R.id.tvFullNameError);
        tvPhoneError = view.findViewById(R.id.tvPhoneError);
        tvPostDateError = view.findViewById(R.id.tvPostDateError);
        tvDistrictError = view.findViewById(R.id.tvDistrictError);
        tvAddressError = view.findViewById(R.id.tvAddressError);
        tvRoomTypeError = view.findViewById(R.id.tvRoomTypeError);
        tvPriceError = view.findViewById(R.id.tvPriceError);

        // Lấy username từ Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentUsername = bundle.getString("username");
        }

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("posts");

        // Thiết lập spinner
        setupSpinners();

        // Thêm TextWatcher để định dạng số khi nhập
        addNumberFormatWatcher(edtPrice);
        addNumberFormatWatcher(edtElectricity);
        addNumberFormatWatcher(edtWater);
        addNumberFormatWatcher(edtService);
        addNumberFormatWatcher(edtWifi);

        // Điền ngày hiện tại khi đăng bài mới
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        edtPostDate.setText(currentDate);

        // Xử lý chọn ảnh
        imgPhoto1.setOnClickListener(v -> openImagePicker(1));
        imgPhoto2.setOnClickListener(v -> openImagePicker(2));
        imgPhoto3.setOnClickListener(v -> openImagePicker(3));

        // Kiểm tra chế độ chỉnh sửa
        if (bundle != null && bundle.getBoolean("isEditMode", false)) {
            isEditMode = true;
            postId = bundle.getString("postId");
            Post post = (Post) bundle.getSerializable("post");
            if (post != null) {
                btnPost.setText("Cập nhật");
                loadPostData(post);
            }
        }

        // Xử lý nút đăng bài hoặc cập nhật
        btnPost.setOnClickListener(v -> {
            if (isEditMode) {
                updatePost();
            } else {
                uploadPost();
            }
        });

        return view;
    }

    private void showErrorDialog(String message) {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showSuccessDialog(String message) {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle("Thành công")
                .setMessage(message)
                .setIcon(R.drawable.ic_check_green) // Đảm bảo sử dụng tích xanh
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToHomeFragment();
                })
                .setCancelable(false)
                .show();
    }

    private void setupSpinners() {
        // Thiết lập Spinner cho Quận
        String[] districts = {"Chọn quận", "Ba Đình", "Hoàn Kiếm", "Hai Bà Trưng", "Đống Đa", "Tây Hồ", "Cầu Giấy", "Thanh Xuân",
                "Hoàng Mai", "Long Biên", "Nam Từ Liêm", "Bắc Từ Liêm", "Hà Đông", "Sơn Tây", "Ba Vì",
                "Chương Mỹ", "Đan Phượng", "Đông Anh", "Gia Lâm", "Hoài Đức", "Mê Linh", "Mỹ Đức",
                "Phú Xuyên", "Phúc Thọ", "Quốc Oai", "Sóc Sơn", "Thạch Thất", "Thanh Oai", "Thanh Trì",
                "Thường Tín", "Ứng Hòa"};
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, districts) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Vô hiệu hóa mục hint
            }
        };
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);
        spinnerDistrict.setSelection(0); // Chọn hint mặc định

        // Thiết lập Spinner cho Loại phòng
        String[] roomTypes = {"Chọn loại phòng", "Phòng trọ", "Chung cư", "Nhà nguyên căn"};
        ArrayAdapter<String> roomTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roomTypes) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Vô hiệu hóa mục hint
            }
        };
        roomTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomType.setAdapter(roomTypeAdapter);
        spinnerRoomType.setSelection(0); // Chọn hint mặc định

        // Xử lý sự kiện chọn Quận
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDistrict = null;
                } else {
                    selectedDistrict = districts[position];
                    tvDistrictError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = null;
                tvDistrictError.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý sự kiện chọn Loại phòng
        spinnerRoomType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedRoomType = null;
                } else {
                    selectedRoomType = roomTypes[position];
                    tvRoomTypeError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoomType = null;
                tvRoomTypeError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addNumberFormatWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^0-9]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            long value = Long.parseLong(cleanString);
                            DecimalFormat formatter = new DecimalFormat("#,###");
                            String formatted = formatter.format(value);
                            current = formatted;
                            editText.setText(formatted);
                            editText.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            editText.setText(current);
                        }
                    } else {
                        current = "";
                    }

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    private void openImagePicker(int imageNumber) {
        currentImageSelection = imageNumber;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    switch (currentImageSelection) {
                        case 1:
                            imgPhoto1.setImageBitmap(bitmap);
                            imageUri1 = imageUri;
                            break;
                        case 2:
                            imgPhoto2.setImageBitmap(bitmap);
                            imageUri2 = imageUri;
                            break;
                        case 3:
                            imgPhoto3.setImageBitmap(bitmap);
                            imageUri3 = imageUri;
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog("Lỗi tải ảnh: " + e.getMessage());
                }
            }
        }
    }

    private void loadPostData(Post post) {
        if (post != null) {
            edtFullName.setText(post.fullName);
            edtPhone.setText(post.phone);
            edtPostDate.setText(post.postDate);
            edtAddress.setText(post.address);
            edtPrice.setText(formatNumber(post.price));
            edtElectricity.setText(formatNumber(post.electricity));
            edtWater.setText(formatNumber(post.water));
            edtService.setText(formatNumber(post.service));
            edtWifi.setText(formatNumber(post.wifi));

            setSpinnerValue(spinnerDistrict, post.district);
            setSpinnerValue(spinnerRoomType, post.roomType);

            if (post.photo1Uri != null && !post.photo1Uri.isEmpty()) {
                Bitmap bitmap1 = decodeBase64ToBitmap(post.photo1Uri);
                if (bitmap1 != null) imgPhoto1.setImageBitmap(bitmap1);
            }
            if (post.photo2Uri != null && !post.photo2Uri.isEmpty()) {
                Bitmap bitmap2 = decodeBase64ToBitmap(post.photo2Uri);
                if (bitmap2 != null) imgPhoto2.setImageBitmap(bitmap2);
            }
            if (post.photo3Uri != null && !post.photo3Uri.isEmpty()) {
                Bitmap bitmap3 = decodeBase64ToBitmap(post.photo3Uri);
                if (bitmap3 != null) imgPhoto3.setImageBitmap(bitmap3);
            }
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

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void uploadPost() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String postDate = edtPostDate.getText().toString().trim();
        String city = "Hà Nội";
        String address = edtAddress.getText().toString().trim();
        String price = edtPrice.getText().toString().trim().replaceAll("[^0-9]", "");
        String electricity = edtElectricity.getText().toString().trim().replaceAll("[^0-9]", "");
        String water = edtWater.getText().toString().trim().replaceAll("[^0-9]", "");
        String service = edtService.getText().toString().trim().replaceAll("[^0-9]", "");
        String wifi = edtWifi.getText().toString().trim().replaceAll("[^0-9]", "");

        // Kiểm tra các trường bắt buộc
        boolean isValid = true;

        if (fullName.isEmpty()) {
            tvFullNameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvFullNameError.setVisibility(View.GONE);
        }

        if (phone.isEmpty()) {
            tvPhoneError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPhoneError.setVisibility(View.GONE);
        }

        if (postDate.isEmpty()) {
            tvPostDateError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPostDateError.setVisibility(View.GONE);
        }

        if (selectedDistrict == null) {
            tvDistrictError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvDistrictError.setVisibility(View.GONE);
        }

        if (address.isEmpty()) {
            tvAddressError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvAddressError.setVisibility(View.GONE);
        }

        if (selectedRoomType == null) {
            tvRoomTypeError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvRoomTypeError.setVisibility(View.GONE);
        }

        if (price.isEmpty()) {
            tvPriceError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPriceError.setVisibility(View.GONE);
        }

        // Nếu tất cả trường bắt buộc đã được điền, tiến hành đăng bài
        if (isValid) {
            String photo1Base64 = imageUri1 != null ? convertImageToBase64(imageUri1) : "";
            String photo2Base64 = imageUri2 != null ? convertImageToBase64(imageUri2) : "";
            String photo3Base64 = imageUri3 != null ? convertImageToBase64(imageUri3) : "";

            Post post = new Post(currentUsername, fullName, phone, city, selectedDistrict, address, selectedRoomType, price,
                    electricity, water, service, wifi, photo1Base64, photo2Base64, photo3Base64, postDate);

            String newPostId = databaseReference.push().getKey();
            if (newPostId != null) {
                databaseReference.child(newPostId).setValue(post)
                        .addOnSuccessListener(aVoid -> showSuccessDialog("Đăng bài thành công"))
                        .addOnFailureListener(e -> showErrorDialog("Đăng bài thất bại: " + e.getMessage()));
            }
        }
    }

    private void updatePost() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String postDate = edtPostDate.getText().toString().trim();
        String city = "Hà Nội";
        String address = edtAddress.getText().toString().trim();
        String price = edtPrice.getText().toString().trim().replaceAll("[^0-9]", "");
        String electricity = edtElectricity.getText().toString().trim().replaceAll("[^0-9]", "");
        String water = edtWater.getText().toString().trim().replaceAll("[^0-9]", "");
        String service = edtService.getText().toString().trim().replaceAll("[^0-9]", "");
        String wifi = edtWifi.getText().toString().trim().replaceAll("[^0-9]", "");

        // Kiểm tra các trường bắt buộc
        boolean isValid = true;

        if (fullName.isEmpty()) {
            tvFullNameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvFullNameError.setVisibility(View.GONE);
        }

        if (phone.isEmpty()) {
            tvPhoneError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPhoneError.setVisibility(View.GONE);
        }

        if (postDate.isEmpty()) {
            tvPostDateError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPostDateError.setVisibility(View.GONE);
        }

        if (selectedDistrict == null) {
            tvDistrictError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvDistrictError.setVisibility(View.GONE);
        }

        if (address.isEmpty()) {
            tvAddressError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvAddressError.setVisibility(View.GONE);
        }

        if (selectedRoomType == null) {
            tvRoomTypeError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvRoomTypeError.setVisibility(View.GONE);
        }

        if (price.isEmpty()) {
            tvPriceError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPriceError.setVisibility(View.GONE);
        }

        // Nếu tất cả trường bắt buộc đã được điền, tiến hành cập nhật bài
        if (isValid) {
            String photo1Base64 = imageUri1 != null ? convertImageToBase64(imageUri1) : "";
            String photo2Base64 = imageUri2 != null ? convertImageToBase64(imageUri2) : "";
            String photo3Base64 = imageUri3 != null ? convertImageToBase64(imageUri3) : "";

            Post post = new Post(currentUsername, fullName, phone, city, selectedDistrict, address, selectedRoomType, price,
                    electricity, water, service, wifi, photo1Base64, photo2Base64, photo3Base64, postDate);

            if (postId != null) {
                databaseReference.child(postId).setValue(post)
                        .addOnSuccessListener(aVoid -> showSuccessDialog("Cập nhật bài đăng thành công"))
                        .addOnFailureListener(e -> showErrorDialog("Cập nhật thất bại: " + e.getMessage()));
            }
        }
    }

    private String convertImageToBase64(Uri uri) {
        if (getActivity() == null) return "";
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
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

    private void navigateToHomeFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();

            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        }
    }

    public static class Post implements java.io.Serializable {
        public String username, fullName, phone, city, district, address, roomType, price, electricity, water, service, wifi;
        public String photo1Uri, photo2Uri, photo3Uri;
        public String postDate;

        public Post() {
            // Default constructor required for Firebase
        }

        public Post(String username, String fullName, String phone, String city, String district, String address, String roomType,
                    String price, String electricity, String water, String service, String wifi,
                    String photo1Uri, String photo2Uri, String photo3Uri, String postDate) {
            this.username = username;
            this.fullName = fullName;
            this.phone = phone;
            this.city = city;
            this.district = district;
            this.address = address;
            this.roomType = roomType;
            this.price = price;
            this.electricity = electricity;
            this.water = water;
            this.service = service;
            this.wifi = wifi;
            this.photo1Uri = photo1Uri;
            this.photo2Uri = photo2Uri;
            this.photo3Uri = photo3Uri;
            this.postDate = postDate;
        }
    }
}