package com.example.thue_tro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateProfileActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện và biến trạng thái
    private EditText edtNguoiDung, edtSoDienThoai, edtEmail; // Ô nhập liệu cho tên, số điện thoại, email
    private TextView tvErrorNguoiDung, tvErrorSoDienThoai, tvErrorEmail, tvQuayLai; // Hiển thị lỗi và nút quay lại
    private Button btnSaveProfile; // Nút lưu thông tin hồ sơ
    private DatabaseReference usersReference; // Tham chiếu Firebase node "users"
    private DatabaseReference adminsReference; // Tham chiếu Firebase node "admins"
    private String currentUsername; // Tên đăng nhập của người dùng hiện tại
    private String userRole; // Vai trò của người dùng (admin hoặc user)

    // Hàm onCreate: Khởi tạo giao diện và thiết lập các sự kiện
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile); // Nạp layout activity_update_profile.xml

        // Ánh xạ các thành phần giao diện từ layout
        tvQuayLai = findViewById(R.id.tvQuayLai); // TextView để quay lại
        edtNguoiDung = findViewById(R.id.edtNguoiDung); // Ô nhập tên người dùng
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai); // Ô nhập số điện thoại
        edtEmail = findViewById(R.id.edtEmail); // Ô nhập email
        tvErrorNguoiDung = findViewById(R.id.tvErrorNguoiDung); // TextView hiển thị lỗi tên
        tvErrorSoDienThoai = findViewById(R.id.tvErrorSoDienThoai); // TextView hiển thị lỗi số điện thoại
        tvErrorEmail = findViewById(R.id.tvErrorEmail); // TextView hiển thị lỗi email
        btnSaveProfile = findViewById(R.id.btnSaveProfile); // Nút lưu hồ sơ

        // Khởi tạo tham chiếu Firebase đến node "users" và "admins"
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy thông tin từ Intent (tên đăng nhập và vai trò)
        currentUsername = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("role");
        Log.d("UpdateProfileActivity", "Vai trò người dùng: " + userRole); // Ghi log để kiểm tra vai trò

        // Tải thông tin người dùng nếu có tên đăng nhập
        if (currentUsername != null) {
            loadUserData(currentUsername); // Tải dữ liệu từ Firebase
        } else {
            showErrorDialog("Không tìm thấy thông tin tài khoản!"); // Hiển thị lỗi nếu thiếu thông tin
            finish(); // Đóng Activity
        }

        // Xử lý sự kiện nhấn "Quay lại"
        tvQuayLai.setOnClickListener(v -> finish()); // Đóng Activity và quay lại màn hình trước

        // Xử lý sự kiện nhấn nút "Lưu"
        btnSaveProfile.setOnClickListener(v -> validateAndUpdateProfile()); // Kiểm tra và cập nhật hồ sơ
    }

    // Hàm hiển thị hộp thoại lỗi
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    // Hàm hiển thị hộp thoại thành công
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("Cập nhật hồ sơ thành công!")
                .setIcon(R.drawable.ic_check_green) // Biểu tượng tích xanh để tăng tính trực quan
                .setPositiveButton("OK", (dialog, which) -> finish()) // Đóng Activity
                .setCancelable(false)
                .show();
    }

    // Hàm tải thông tin người dùng từ Firebase
    private void loadUserData(String username) {
        // Chọn node phù hợp dựa trên vai trò (admin hoặc user)
        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

        // Tìm tài khoản theo tên đăng nhập
        targetReference.orderByChild("dangNhap").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Nếu tìm thấy tài khoản
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class); // Chuyển dữ liệu thành đối tượng User
                                if (user != null) {
                                    // Hiển thị thông tin lên các ô nhập liệu
                                    edtNguoiDung.setText(user.nguoiDung != null ? user.nguoiDung : "");
                                    edtSoDienThoai.setText(user.soDienThoai != null ? user.soDienThoai : "");
                                    edtEmail.setText(user.email != null ? user.email : "");
                                }
                            }
                        } else {
                            // Hiển thị lỗi và đóng Activity nếu không tìm thấy tài khoản
                            showErrorDialog("Không tìm thấy thông tin người dùng!");
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Hiển thị lỗi Firebase và đóng Activity
                        showErrorDialog("Lỗi: " + databaseError.getMessage());
                        finish();
                    }
                });
    }

    // Hàm kiểm tra và cập nhật thông tin hồ sơ
    private void validateAndUpdateProfile() {
        // Lấy giá trị từ các ô nhập liệu
        String nguoiDung = edtNguoiDung.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        boolean isValid = true; // Biến kiểm tra tính hợp lệ

        // Kiểm tra tên người dùng
        if (nguoiDung.isEmpty()) {
            tvErrorNguoiDung.setText("Vui lòng nhập tên người dùng");
            tvErrorNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!nguoiDung.matches("[a-zA-Z\\s]+")) {
            tvErrorNguoiDung.setText("Tên người dùng chỉ được chứa chữ cái");
            tvErrorNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorNguoiDung.setVisibility(View.GONE);
        }

        // Kiểm tra số điện thoại
        if (soDienThoai.isEmpty()) {
            tvErrorSoDienThoai.setText("Vui lòng nhập số điện thoại");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!soDienThoai.matches("\\d+")) {
            tvErrorSoDienThoai.setText("Số điện thoại chỉ được chứa số");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (soDienThoai.length() != 10) {
            tvErrorSoDienThoai.setText("Số điện thoại phải có 10 số");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorSoDienThoai.setVisibility(View.GONE);
        }

        // Kiểm tra email
        if (email.isEmpty()) {
            tvErrorEmail.setText("Vui lòng nhập email");
            tvErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvErrorEmail.setText("Email không hợp lệ");
            tvErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorEmail.setVisibility(View.GONE);
        }

        // Nếu dữ liệu hợp lệ, kiểm tra trùng lặp số điện thoại và email
        if (isValid) {
            checkExistingFields(soDienThoai, email, () -> {
                // Chọn node phù hợp để cập nhật
                DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

                // Tìm tài khoản và cập nhật thông tin
                targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Nếu tìm thấy tài khoản
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Cập nhật từng trường dữ liệu
                                        snapshot.getRef().child("nguoiDung").setValue(nguoiDung);
                                        snapshot.getRef().child("soDienThoai").setValue(soDienThoai);
                                        snapshot.getRef().child("email").setValue(email)
                                                .addOnSuccessListener(aVoid -> showSuccessDialog()) // Hiển thị thông báo thành công
                                                .addOnFailureListener(e -> showErrorDialog("Cập nhật thất bại: " + e.getMessage())); // Hiển thị lỗi
                                    }
                                } else {
                                    showErrorDialog("Không tìm thấy thông tin tài khoản!");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                showErrorDialog("Lỗi: " + databaseError.getMessage());
                            }
                        });
            });
        }
    }

    // Hàm kiểm tra trùng lặp số điện thoại và email
    private void checkExistingFields(String soDienThoai, String email, Runnable onSuccess) {
        boolean[] checks = new boolean[2]; // 0: số điện thoại, 1: email
        checks[0] = checks[1] = true; // Mặc định là không trùng
        int[] completedChecks = {0}; // Đếm số lần kiểm tra hoàn tất

        // Kiểm tra số điện thoại
        checkField(usersReference, "soDienThoai", soDienThoai, () -> {
            tvErrorSoDienThoai.setText("Số điện thoại đã được sử dụng");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            checks[0] = false; // Đánh dấu số điện thoại trùng
        }, () -> checkField(adminsReference, "soDienThoai", soDienThoai, () -> {
            tvErrorSoDienThoai.setText("Số điện thoại đã được sử dụng");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            checks[0] = false;
        }, () -> {
            completedChecks[0]++; // Tăng số lần kiểm tra
            proceedIfAllChecksDone(checks, completedChecks, onSuccess); // Kiểm tra xem có thể tiếp tục không
        }));

        // Kiểm tra email
        checkField(usersReference, "email", email, () -> {
            tvErrorEmail.setText("Email đã được sử dụng");
            tvErrorEmail.setVisibility(View.VISIBLE);
            checks[1] = false; // Đánh dấu email trùng
        }, () -> checkField(adminsReference, "email", email, () -> {
            tvErrorEmail.setText("Email đã được sử dụng");
            tvErrorEmail.setVisibility(View.VISIBLE);
            checks[1] = false;
        }, () -> {
            completedChecks[0]++;
            proceedIfAllChecksDone(checks, completedChecks, onSuccess);
        }));
    }

    // Hàm kiểm tra một trường dữ liệu cụ thể trong Firebase
    private void checkField(DatabaseReference reference, String field, String value, Runnable onExist, Runnable onNext) {
        reference.orderByChild(field).equalTo(value)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String username = snapshot.child("dangNhap").getValue(String.class);
                                // Kiểm tra nếu trường tồn tại và không thuộc tài khoản hiện tại
                                if (username != null && !username.equals(currentUsername)) {
                                    onExist.run(); // Gọi hàm báo trùng
                                    onNext.run();
                                    return;
                                }
                            }
                        }
                        onNext.run(); // Tiếp tục nếu không trùng
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("UpdateProfile", "Lỗi kiểm tra " + field + ": " + databaseError.getMessage());
                        onNext.run(); // Tiếp tục ngay cả khi có lỗi Firebase
                    }
                });
    }

    // Hàm kiểm tra nếu tất cả kiểm tra trùng lặp đã hoàn tất
    private void proceedIfAllChecksDone(boolean[] checks, int[] completedChecks, Runnable onSuccess) {
        if (completedChecks[0] == 2) { // Nếu cả hai kiểm tra (số điện thoại, email) hoàn tất
            if (checks[0] && checks[1]) { // Nếu không có trùng lặp
                onSuccess.run(); // Tiến hành cập nhật hồ sơ
            }
        }
    }
}