package com.example.thue_tro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
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

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmNewPassword;
    private TextView tvErrorOldPassword, tvErrorNewPassword, tvErrorConfirmNewPassword;
    private Button btnSavePassword;
    private TextView tvQuayLai;
    private DatabaseReference databaseReference;
    private DatabaseReference adminsReference;
    private String currentUsername;
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Ánh xạ view
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        tvErrorOldPassword = findViewById(R.id.tvErrorOldPassword);
        tvErrorNewPassword = findViewById(R.id.tvErrorNewPassword);
        tvErrorConfirmNewPassword = findViewById(R.id.tvErrorConfirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        tvQuayLai = findViewById(R.id.tvQuayLai);

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        DatabaseReference adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy username từ Intent
        currentUsername = getIntent().getStringExtra("username");

        if (currentUsername == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Lỗi")
                    .setMessage("Không tìm thấy thông tin người dùng!")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
            return;
        }

        // Thiết lập tính năng con mắt cho mật khẩu
        setupPasswordToggle(edtOldPassword, () -> isOldPasswordVisible = !isOldPasswordVisible);
        setupPasswordToggle(edtNewPassword, () -> isNewPasswordVisible = !isNewPasswordVisible);
        setupPasswordToggle(edtConfirmNewPassword, () -> isConfirmPasswordVisible = !isConfirmPasswordVisible);

        // Xử lý nút "Lưu"
        btnSavePassword.setOnClickListener(v -> validateAndChangePassword());

        // Xử lý nút "Quay lại"
        tvQuayLai.setOnClickListener(v -> finish());
    }

    private void setupPasswordToggle(EditText editText, Runnable toggleVisibility) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                    toggleVisibility.run();
                    if (editText == edtOldPassword) {
                        if (isOldPasswordVisible) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                    } else if (editText == edtNewPassword) {
                        if (isNewPasswordVisible) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                    } else if (editText == edtConfirmNewPassword) {
                        if (isConfirmPasswordVisible) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void validateAndChangePassword() {
        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        boolean isValid = true;

        // Kiểm tra mật khẩu cũ
        if (oldPassword.isEmpty()) {
            tvErrorOldPassword.setText("Vui lòng nhập mật khẩu cũ");
            tvErrorOldPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorOldPassword.setVisibility(View.GONE);
        }

        // Kiểm tra mật khẩu mới
        if (newPassword.isEmpty()) {
            tvErrorNewPassword.setText("Vui lòng nhập mật khẩu mới");
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (newPassword.length() < 8) {
            tvErrorNewPassword.setText("Mật khẩu mới phải có ít nhất 8 ký tự");
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorNewPassword.setVisibility(View.GONE);
        }

        // Kiểm tra nhập lại mật khẩu mới
        if (confirmNewPassword.isEmpty()) {
            tvErrorConfirmNewPassword.setText("Vui lòng nhập lại mật khẩu mới");
            tvErrorConfirmNewPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!confirmNewPassword.equals(newPassword)) {
            tvErrorConfirmNewPassword.setText("Mật khẩu không khớp");
            tvErrorConfirmNewPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorConfirmNewPassword.setVisibility(View.GONE);
        }

        if (isValid) {
            // Hiển thị dialog xác nhận
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn đổi mật khẩu mới?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Nếu người dùng chọn "Có", thực hiện đổi mật khẩu
                        changePassword(oldPassword, newPassword);
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        // Nếu người dùng chọn "Không", đóng dialog và không làm gì
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void changePassword(String oldPassword, String newPassword) {
        // Kiểm tra mật khẩu cũ và cập nhật mật khẩu mới
        databaseReference.orderByChild("dangNhap").equalTo(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class);
                                if (user != null && user.matKhau.equals(oldPassword)) {
                                    // Cập nhật mật khẩu mới
                                    snapshot.getRef().child("matKhau").setValue(newPassword)
                                            .addOnSuccessListener(aVoid -> {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                                                builder.setTitle("Thành công")
                                                        .setMessage("Đổi mật khẩu thành công!")
                                                        .setIcon(R.drawable.ic_check_green)
                                                        .setPositiveButton("OK", (dialog, which) -> finish())
                                                        .setCancelable(false)
                                                        .show();
                                            })
                                            .addOnFailureListener(e -> {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                                                builder.setTitle("Lỗi")
                                                        .setMessage("Đổi mật khẩu thất bại: " + e.getMessage())
                                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                        .show();
                                            });
                                } else {
                                    tvErrorOldPassword.setText("Mật khẩu cũ không đúng");
                                    tvErrorOldPassword.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                            builder.setTitle("Lỗi")
                                    .setMessage("Không tìm thấy thông tin tài khoản!")
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                        builder.setTitle("Lỗi")
                                .setMessage("Lỗi: " + databaseError.getMessage())
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                });
    }
}