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

    private EditText edtNguoiDung, edtSoDienThoai, edtEmail;
    private TextView tvErrorNguoiDung, tvErrorSoDienThoai, tvErrorEmail, tvQuayLai;
    private Button btnSaveProfile;
    private DatabaseReference usersReference;
    private DatabaseReference adminsReference;
    private String currentUsername;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // Ánh xạ view
        tvQuayLai = findViewById(R.id.tvQuayLai);
        edtNguoiDung = findViewById(R.id.edtNguoiDung);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        tvErrorNguoiDung = findViewById(R.id.tvErrorNguoiDung);
        tvErrorSoDienThoai = findViewById(R.id.tvErrorSoDienThoai);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Khởi tạo Firebase
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy username và role từ Intent
        currentUsername = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("role");
        Log.d("UpdateProfileActivity", "User role: " + userRole);

        // Hiển thị thông tin hiện tại
        if (currentUsername != null) {
            loadUserData(currentUsername);
        } else {
            showErrorDialog("Không tìm thấy thông tin tài khoản!");
            finish();
        }

        // Xử lý nút "Quay lại"
        tvQuayLai.setOnClickListener(v -> finish());

        // Xử lý nút "Lưu"
        btnSaveProfile.setOnClickListener(v -> validateAndUpdateProfile());
    }

    //Show dialog nếu bị lỗi
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    //Show dialog thành công khi cập nhật hồ sơ thành công
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("Cập nhật hồ sơ thành công!")
                .setIcon(R.drawable.ic_check_green) // Sử dụng biểu tượng tích xanh
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    // Hàm này lấy thông tin người dùng từ Firebase và hiển thị lên các ô nhập.
    private void loadUserData(String username) {
        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

        targetReference.orderByChild("dangNhap").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class);
                                if (user != null) {
                                    edtNguoiDung.setText(user.nguoiDung != null ? user.nguoiDung : "");
                                    edtSoDienThoai.setText(user.soDienThoai != null ? user.soDienThoai : "");
                                    edtEmail.setText(user.email != null ? user.email : "");
                                }
                            }
                        } else {
                            showErrorDialog("Không tìm thấy thông tin người dùng!");
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showErrorDialog("Lỗi: " + databaseError.getMessage());
                        finish();
                    }
                });
    }

    //Kiểm tra xem điều kiện nhập thông tin
    private void validateAndUpdateProfile() {
        String nguoiDung = edtNguoiDung.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        boolean isValid = true;

        // Kiểm tra Tên người dùng
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

        // Kiểm tra Số điện thoại
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

        // Kiểm tra Email
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
        // Nếu thông tin nhập vào (tên, số điện thoại, email) hợp lệ.
        if (isValid) {
            checkExistingFields(soDienThoai, email, () -> {
                DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

                targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef().child("nguoiDung").setValue(nguoiDung);
                                        snapshot.getRef().child("soDienThoai").setValue(soDienThoai);
                                        snapshot.getRef().child("email").setValue(email)
                                                .addOnSuccessListener(aVoid -> showSuccessDialog())
                                                .addOnFailureListener(e -> showErrorDialog("Cập nhật thất bại: " + e.getMessage()));
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

    //Kiểm tra số điện thoại và email đã tạo tồn tại từ trước trong Firebase
    private void checkExistingFields(String soDienThoai, String email, Runnable onSuccess) {
        boolean[] checks = new boolean[2]; // 0: soDienThoai, 1: email
        checks[0] = checks[1] = true;
        int[] completedChecks = {0};

        // Kiểm tra số điện thoại
        checkField(usersReference, "soDienThoai", soDienThoai, () -> {
            tvErrorSoDienThoai.setText("Số điện thoại đã được sử dụng");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            checks[0] = false;
        }, () -> checkField(adminsReference, "soDienThoai", soDienThoai, () -> {
            tvErrorSoDienThoai.setText("Số điện thoại đã được sử dụng");
            tvErrorSoDienThoai.setVisibility(View.VISIBLE);
            checks[0] = false;
        }, () -> {
            completedChecks[0]++;
            proceedIfAllChecksDone(checks, completedChecks, onSuccess);
        }));

        // Kiểm tra email
        checkField(usersReference, "email", email, () -> {
            tvErrorEmail.setText("Email đã được sử dụng");
            tvErrorEmail.setVisibility(View.VISIBLE);
            checks[1] = false;
        }, () -> checkField(adminsReference, "email", email, () -> {
            tvErrorEmail.setText("Email đã được sử dụng");
            tvErrorEmail.setVisibility(View.VISIBLE);
            checks[1] = false;
        }, () -> {
            completedChecks[0]++;
            proceedIfAllChecksDone(checks, completedChecks, onSuccess);
        }));
    }
    // Tìm kiếm trong Firebase dựa trên trường (field) và giá trị (value) cụ thể
    private void checkField(DatabaseReference reference, String field, String value, Runnable onExist, Runnable onNext) {
        reference.orderByChild(field).equalTo(value)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String username = snapshot.child("dangNhap").getValue(String.class);// Kiểm tra nếu tên đăng nhập tồn tại và không phải của người dùng hiện tại
                                if (username != null && !username.equals(currentUsername)) {
                                    onExist.run();
                                    onNext.run();
                                    return;
                                }
                            }
                        }
                        onNext.run();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("UpdateProfile", "Lỗi kiểm tra " + field + ": " + databaseError.getMessage());
                        onNext.run();
                    }
                });
    }

    private void proceedIfAllChecksDone(boolean[] checks, int[] completedChecks, Runnable onSuccess) {
        if (completedChecks[0] == 2) {
            if (checks[0] && checks[1]) {
                onSuccess.run();
            }
        }
    }

}