package com.example.thue_tro;

// Lớp ChangePasswordActivity là một Activity trong ứng dụng Android, thuộc gói com.example.thue_tro.
// Chức năng chính: Cung cấp giao diện để người dùng thay đổi mật khẩu tài khoản.
// - Người dùng nhập mật khẩu cũ, mật khẩu mới, và xác nhận mật khẩu mới.
// - Kiểm tra tính hợp lệ của dữ liệu nhập vào (mật khẩu cũ đúng, mật khẩu mới đủ dài, xác nhận khớp).
// - Tương tác với Firebase Realtime Database để kiểm tra và cập nhật mật khẩu.
// - Hỗ trợ tính năng "hiển thị/ẩn" mật khẩu (biểu tượng con mắt).
// - Phân biệt vai trò người dùng (admin hoặc user thường) để truy cập node phù hợp trong Firebase.

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

    // Khai báo các thành phần giao diện và biến trạng thái
    private EditText edtOldPassword, edtNewPassword, edtConfirmNewPassword; // Ô nhập liệu cho mật khẩu
    private TextView tvErrorOldPassword, tvErrorNewPassword, tvErrorConfirmNewPassword; // Hiển thị thông báo lỗi
    private Button btnSavePassword; // Nút lưu mật khẩu mới
    private TextView tvQuayLai; // TextView để quay lại màn hình trước
    private DatabaseReference usersReference; // Tham chiếu Firebase node "users"
    private DatabaseReference adminsReference; // Tham chiếu Firebase node "admins"
    private String currentUsername; // Tên đăng nhập của người dùng hiện tại
    private String userRole; // Vai trò của người dùng (admin hoặc user)
    private boolean isOldPasswordVisible = false; // Trạng thái hiển thị/ẩn mật khẩu cũ
    private boolean isNewPasswordVisible = false; // Trạng thái hiển thị/ẩn mật khẩu mới
    private boolean isConfirmPasswordVisible = false; // Trạng thái hiển thị/ẩn xác nhận mật khẩu

    // Hàm onCreate: Khởi tạo giao diện và thiết lập các sự kiện
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // Nạp layout activity_change_password.xml

        // Ánh xạ các thành phần giao diện từ layout
        edtOldPassword = findViewById(R.id.edtOldPassword); // Ô nhập mật khẩu cũ
        edtNewPassword = findViewById(R.id.edtNewPassword); // Ô nhập mật khẩu mới
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword); // Ô nhập xác nhận mật khẩu mới
        tvErrorOldPassword = findViewById(R.id.tvErrorOldPassword); // TextView hiển thị lỗi mật khẩu cũ
        tvErrorNewPassword = findViewById(R.id.tvErrorNewPassword); // TextView hiển thị lỗi mật khẩu mới
        tvErrorConfirmNewPassword = findViewById(R.id.tvErrorConfirmNewPassword); // TextView hiển thị lỗi xác nhận
        btnSavePassword = findViewById(R.id.btnSavePassword); // Nút lưu mật khẩu
        tvQuayLai = findViewById(R.id.tvQuayLai); // TextView để quay lại

        // Khởi tạo tham chiếu Firebase đến node "users" và "admins"
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy thông tin từ Intent (tên đăng nhập và vai trò)
        currentUsername = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("role");

        // Kiểm tra nếu thiếu thông tin người dùng
        if (currentUsername == null || userRole == null) {
            // Hiển thị hộp thoại lỗi và thoát Activity nếu không có thông tin
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Lỗi")
                    .setMessage("Không tìm thấy thông tin người dùng!")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
            return;
        }

        // Thiết lập tính năng hiển thị/ẩn mật khẩu (biểu tượng con mắt)
        // Quan trọng: Tính năng này cải thiện trải nghiệm người dùng, cho phép bật/tắt chế độ hiển thị mật khẩu
        setupPasswordToggle(edtOldPassword, () -> isOldPasswordVisible = !isOldPasswordVisible);
        setupPasswordToggle(edtNewPassword, () -> isNewPasswordVisible = !isNewPasswordVisible);
        setupPasswordToggle(edtConfirmNewPassword, () -> isConfirmPasswordVisible = !isConfirmPasswordVisible);

        // Xử lý sự kiện nhấn nút "Lưu"
        btnSavePassword.setOnClickListener(v -> validateAndChangePassword());

        // Xử lý sự kiện nhấn "Quay lại"
        tvQuayLai.setOnClickListener(v -> finish()); // Đóng Activity và quay lại màn hình trước
    }

    // Hàm thiết lập tính năng hiển thị/ẩn mật khẩu
    // Quan trọng: Đoạn code này xử lý sự kiện chạm vào biểu tượng con mắt, thay đổi kiểu hiển thị của EditText
    private void setupPasswordToggle(EditText editText, Runnable toggleVisibility) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Kiểm tra nếu người dùng chạm vào vùng biểu tượng con mắt (phía bên phải EditText)
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                    toggleVisibility.run(); // Cập nhật trạng thái hiển thị/ẩn
                    if (editText == edtOldPassword) {
                        if (isOldPasswordVisible) {
                            // Hiển thị mật khẩu cũ
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            // Ẩn mật khẩu cũ
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                    } else if (editText == edtNewPassword) {
                        if (isNewPasswordVisible) {
                            // Hiển thị mật khẩu mới
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            // Ẩn mật khẩu mới
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                    } else if (editText == edtConfirmNewPassword) {
                        if (isConfirmPasswordVisible) {
                            // Hiển thị xác nhận mật khẩu
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            // Ẩn xác nhận mật khẩu
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                    }
                    editText.setSelection(editText.getText().length()); // Đặt con trỏ về cuối văn bản
                    return true; // Sự kiện đã được xử lý
                }
            }
            return false; // Cho phép các sự kiện khác được xử lý
        });
    }

    // Hàm kiểm tra và xử lý thay đổi mật khẩu
    // Quan trọng: Đây là hàm trung tâm, kiểm tra dữ liệu đầu vào và quyết định có gọi hàm đổi mật khẩu hay không
    private void validateAndChangePassword() {
        // Lấy giá trị từ các ô nhập liệu
        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        boolean isValid = true; // Biến kiểm tra tính hợp lệ

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

        // Kiểm tra xác nhận mật khẩu mới
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

        // Nếu tất cả dữ liệu hợp lệ, hiển thị hộp thoại xác nhận
        if (isValid) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn đổi mật khẩu mới?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Gọi hàm đổi mật khẩu nếu người dùng xác nhận
                        changePassword(oldPassword, newPassword);
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        dialog.dismiss(); // Đóng hộp thoại nếu từ chối
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    // Hàm thực hiện thay đổi mật khẩu trong Firebase
    // Quan trọng: Đoạn code này tương tác trực tiếp với Firebase để kiểm tra mật khẩu cũ và cập nhật mật khẩu mới
    private void changePassword(String oldPassword, String newPassword) {
        // Chọn node phù hợp dựa trên vai trò (admin hoặc user)
        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

        // Tìm tài khoản theo tên đăng nhập
        targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Nếu tìm thấy tài khoản
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class); // Chuyển dữ liệu thành đối tượng User
                                if (user != null && user.matKhau.equals(oldPassword)) {
                                    // Nếu mật khẩu cũ đúng, cập nhật mật khẩu mới
                                    snapshot.getRef().child("matKhau").setValue(newPassword)
                                            .addOnSuccessListener(aVoid -> {
                                                // Hiển thị thông báo thành công và đóng Activity
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                                                builder.setTitle("Thành công")
                                                        .setMessage("Đổi mật khẩu thành công!")
                                                        .setIcon(R.drawable.ic_check_green)
                                                        .setPositiveButton("OK", (dialog, which) -> finish())
                                                        .setCancelable(false)
                                                        .show();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Hiển thị thông báo lỗi nếu cập nhật thất bại
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                                                builder.setTitle("Lỗi")
                                                        .setMessage("Đổi mật khẩu thất bại: " + e.getMessage())
                                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                        .show();
                                            });
                                } else {
                                    // Hiển thị lỗi nếu mật khẩu cũ không đúng
                                    tvErrorOldPassword.setText("Mật khẩu cũ không đúng");
                                    tvErrorOldPassword.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            // Hiển thị lỗi nếu không tìm thấy tài khoản
                            AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                            builder.setTitle("Lỗi")
                                    .setMessage("Không tìm thấy thông tin tài khoản!")
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Hiển thị thông báo lỗi nếu truy vấn Firebase bị hủy
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
                        builder.setTitle("Lỗi")
                                .setMessage("Lỗi: " + databaseError.getMessage())
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                });
    }
}