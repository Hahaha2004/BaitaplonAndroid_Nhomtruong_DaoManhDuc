package com.example.thue_tro; // Định nghĩa gói chứa lớp này, giúp tổ chức code trong dự án

import android.Manifest; // Thư viện để quản lý quyền truy cập (permission) như gọi điện
import android.content.Intent; // Thư viện để chuyển đổi giữa các Activity (màn hình)
import android.content.pm.PackageManager; // Thư viện để kiểm tra và yêu cầu quyền từ người dùng
import android.os.Bundle; // Thư viện để lưu trữ trạng thái của Activity
import android.text.InputType; // Thư viện để thay đổi kiểu nhập liệu (ví dụ: ẩn/hiện mật khẩu)
import android.util.Log; // Thư viện để ghi log (dùng để debug)
import android.view.MotionEvent; // Thư viện để xử lý sự kiện chạm trên màn hình
import android.view.View; // Thư viện cơ sở cho các thành phần giao diện
import android.widget.Button; // Thư viện cung cấp đối tượng Button (nút bấm)
import android.widget.EditText; // Thư viện cung cấp ô nhập liệu (ví dụ: nhập username, password)
import android.widget.TextView; // Thư viện cung cấp đối tượng văn bản hiển thị
import android.widget.Toast; // Thư viện để hiển thị thông báo ngắn trên màn hình

import androidx.annotation.NonNull; // Annotation để đảm bảo tham số không null
import androidx.appcompat.app.AppCompatActivity; // Thư viện cơ sở cho Activity hiện đại
import androidx.core.app.ActivityCompat; // Thư viện hỗ trợ yêu cầu quyền từ người dùng
import androidx.core.content.ContextCompat; // Thư viện hỗ trợ kiểm tra quyền đã được cấp chưa

import com.google.firebase.database.DataSnapshot; // Thư viện Firebase để lấy dữ liệu từ cơ sở dữ liệu
import com.google.firebase.database.DatabaseError; // Thư viện Firebase để xử lý lỗi cơ sở dữ liệu
import com.google.firebase.database.DatabaseReference; // Thư viện Firebase để tham chiếu đến một vị trí trong cơ sở dữ liệu
import com.google.firebase.database.FirebaseDatabase; // Thư viện chính để kết nối với Firebase Realtime Database
import com.google.firebase.database.ValueEventListener; // Thư viện Firebase để lắng nghe sự thay đổi dữ liệu

public class Dangnhap extends AppCompatActivity { // Lớp Dangnhap kế thừa từ AppCompatActivity, là màn hình đăng nhập

    // Khai báo các thành phần giao diện và biến
    private EditText edtUsername, edtPassword;
    private TextView tvUsernameError, tvPasswordError, tvRegister; // Văn bản hiển thị lỗi và nút đăng ký
    private Button btnLogin;
    private DatabaseReference databaseReference; // Tham chiếu đến node "users" trong Firebase
    private DatabaseReference adminReference; // Tham chiếu đến node "admins" trong Firebase
    private boolean isPasswordVisible = false; // Biến kiểm soát trạng thái ẩn/hiện mật khẩu
    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 100; // Mã yêu cầu quyền gọi điện

    // Các hằng số để lưu trạng thái khi xoay màn hình
    private static final String KEY_USERNAME = "username"; // Key để lưu tên đăng nhập
    private static final String KEY_PASSWORD = "password"; // Key để lưu mật khẩu
    private static final String KEY_USERNAME_ERROR_VISIBLE = "username_error_visible"; // Key để lưu trạng thái lỗi tên đăng nhập
    private static final String KEY_PASSWORD_ERROR_VISIBLE = "password_error_visible"; // Key để lưu trạng thái lỗi mật khẩu

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Phương thức được gọi khi Activity khởi tạo
        super.onCreate(savedInstanceState); // Gọi phương thức gốc từ lớp cha để khởi tạo Activity
        setContentView(R.layout.dang_nhap); // Liên kết Activity với file giao diện XML (dang_nhap.xml)

        // Kết nối tới Firebase Realtime Database, tham chiếu đến node "users"
        databaseReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Tham chiếu đến node "admins" trong Firebase
        adminReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Liên kết các thành phần giao diện với code
        edtUsername = findViewById(R.id.edtUsername); // Tìm ô nhập tên đăng nhập
        edtPassword = findViewById(R.id.edtPassword); // Tìm ô nhập mật khẩu
        tvUsernameError = findViewById(R.id.tvUsernameError); // Tìm văn bản lỗi tên đăng nhập
        tvPasswordError = findViewById(R.id.tvPasswordError); // Tìm văn bản lỗi mật khẩu
        tvRegister = findViewById(R.id.tvRegister); // Tìm văn bản "Đăng ký"
        btnLogin = findViewById(R.id.btnLogin); // Tìm nút đăng nhập

        // Khôi phục trạng thái nếu có (khi xoay màn hình)
        if (savedInstanceState != null) {
            edtUsername.setText(savedInstanceState.getString(KEY_USERNAME)); // Đặt lại tên đăng nhập
            edtPassword.setText(savedInstanceState.getString(KEY_PASSWORD)); // Đặt lại mật khẩu
            tvUsernameError.setVisibility(savedInstanceState.getBoolean(KEY_USERNAME_ERROR_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị hoặc ẩn lỗi tên đăng nhập
            tvPasswordError.setVisibility(savedInstanceState.getBoolean(KEY_PASSWORD_ERROR_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị hoặc ẩn lỗi mật khẩu
        }

        setupPasswordToggle(edtPassword); // Thiết lập chức năng ẩn/hiện mật khẩu

        // Tạo tài khoản admin mặc định
        createAdminAccount();

        // Sự kiện khi nhấn nút đăng nhập
        btnLogin.setOnClickListener(v -> validateAndLogin());
        // Sự kiện khi nhấn "Đăng ký"
        tvRegister.setOnClickListener(v -> onRegisterClick(v));

        // Kiểm tra và yêu cầu quyền gọi điện
        checkCallPhonePermission();
    }

    // Yêu cầu quyền gọi điện
    private void checkCallPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) { // Kiểm tra xem quyền gọi điện đã được cấp chưa
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, // Yêu cầu quyền gọi điện
                    CALL_PHONE_PERMISSION_REQUEST_CODE); // Gửi yêu cầu với mã định danh
        }
    }

    // Tạo tài khoản admin mặc định nếu chưa tồn tại
    private void createAdminAccount() {
        String adminUsername = "admin12345"; // Tên đăng nhập admin
        String adminPassword = "@Admin12345"; // Mật khẩu admin
        String adminEmail = "admin12345@gmail.com"; // Email admin
        String adminFullName = "ADMIN"; // Tên đầy đủ admin
        String adminPhone = "0987654321"; // Số điện thoại admin

        // Kiểm tra xem tài khoản admin đã tồn tại trong node "admins" chưa
        adminReference.orderByChild("dangNhap").equalTo(adminUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) { // Nếu admin chưa tồn tại
                            DangKi.User admin = new DangKi.User(
                                    adminFullName, adminUsername, adminEmail, adminPassword, adminPhone, "admin"); // Tạo đối tượng admin
                            adminReference.child(adminUsername).setValue(admin) // Lưu admin vào Firebase
                                    .addOnSuccessListener(aVoid -> Log.d("Dangnhap", "Tạo tài khoản admin thành công trong admins")) // Log khi thành công
                                    .addOnFailureListener(e -> Log.e("Dangnhap", "Lỗi tạo admin: " + e.getMessage())); // Log khi thất bại
                        } else {
                            Log.d("Dangnhap", "Tài khoản admin đã tồn tại trong admins"); // Log nếu admin đã tồn tại
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Dangnhap.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi
                    }
                });
    }

    // Thiết lập chức năng ẩn/hiện mật khẩu
    private void setupPasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> { // Lắng nghe sự kiện chạm vào ô mật khẩu
            if (event.getAction() == MotionEvent.ACTION_UP) { // Khi người dùng thả tay
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) { // Kiểm tra xem chạm vào biểu tượng mắt không
                    isPasswordVisible = !isPasswordVisible; // Đổi trạng thái ẩn/hiện
                    if (isPasswordVisible) { // Nếu hiện mật khẩu
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // Hiển thị mật khẩu dạng văn bản
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0); // Đổi biểu tượng thành mắt mở
                    } else { // Nếu ẩn mật khẩu
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Ẩn mật khẩu
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0); // Đổi biểu tượng thành mắt đóng
                    }
                    editText.setSelection(editText.getText().length()); // Đặt con trỏ về cuối ô nhập
                    return true; // Xác nhận sự kiện đã được xử lý
                }
            }
            return false; // Sự kiện chưa được xử lý
        });
    }

    // Xác thực và đăng nhập
    private void validateAndLogin() {
        String username = edtUsername.getText().toString().trim(); // Lấy tên đăng nhập và loại bỏ khoảng trắng
        String password = edtPassword.getText().toString().trim(); // Lấy mật khẩu và loại bỏ khoảng trắng

        boolean isValid = true; // Biến kiểm tra dữ liệu hợp lệ

        // Kiểm tra tên đăng nhập
        if (username.isEmpty()) {
            tvUsernameError.setText("Vui lòng nhập tên đăng nhập"); // Thông báo lỗi nếu trống
            tvUsernameError.setVisibility(View.VISIBLE); // Hiển thị lỗi
            isValid = false;
        } else if (username.length() < 6) {
            tvUsernameError.setText("Tên đăng nhập phải có ít nhất 6 ký tự"); // Thông báo lỗi nếu quá ngắn
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!username.matches("[a-zA-Z0-9]+")) {
            tvUsernameError.setText("Tên đăng nhập chỉ được chứa chữ cái và số"); // Thông báo lỗi nếu chứa ký tự đặc biệt
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvUsernameError.setVisibility(View.GONE); // Ẩn thông báo lỗi nếu hợp lệ
        }

        // Kiểm tra mật khẩu
        if (password.isEmpty()) {
            tvPasswordError.setText("Vui lòng nhập mật khẩu"); // Thông báo lỗi nếu trống
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvPasswordError.setVisibility(View.GONE); // Ẩn thông báo lỗi nếu hợp lệ
        }

        if (isValid) { // Nếu dữ liệu hợp lệ, kiểm tra trên Firebase
            // Kiểm tra trong node "users" trước
            databaseReference.orderByChild("dangNhap").equalTo(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) { // Nếu tên đăng nhập tồn tại trong "users"
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    DangKi.User user = snapshot.getValue(DangKi.User.class); // Lấy thông tin người dùng
                                    if (user != null) {
                                        if (user.matKhau.equals(password)) { // So sánh mật khẩu
                                            Intent intent = new Intent(Dangnhap.this, HomeActivity.class); // Chuẩn bị chuyển sang HomeActivity
                                            intent.putExtra("username", username); // Gửi tên đăng nhập
                                            intent.putExtra("role", user.role != null ? user.role : "user"); // Gửi vai trò (mặc định là "user")
                                            startActivity(intent); // Mở màn hình chính
                                            finish(); // Đóng màn hình đăng nhập
                                        } else {
                                            tvPasswordError.setText("Nhập sai mật khẩu"); // Thông báo lỗi nếu mật khẩu sai
                                            tvPasswordError.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            } else { // Nếu không tìm thấy trong "users", kiểm tra trong "admins"
                                adminReference.orderByChild("dangNhap").equalTo(username)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot adminSnapshot) {
                                                if (adminSnapshot.exists()) { // Nếu tên đăng nhập tồn tại trong "admins"
                                                    for (DataSnapshot snapshot : adminSnapshot.getChildren()) {
                                                        DangKi.User admin = snapshot.getValue(DangKi.User.class); // Lấy thông tin admin
                                                        if (admin != null) {
                                                            if (admin.matKhau.equals(password)) { // So sánh mật khẩu
                                                                Intent intent = new Intent(Dangnhap.this, HomeActivity.class); // Chuẩn bị chuyển sang HomeActivity
                                                                intent.putExtra("username", username); // Gửi tên đăng nhập
                                                                intent.putExtra("role", admin.role != null ? admin.role : "admin"); // Gửi vai trò (mặc định là "admin")
                                                                startActivity(intent); // Mở màn hình chính
                                                                finish(); // Đóng màn hình đăng nhập
                                                            } else {
                                                                tvPasswordError.setText("Nhập sai mật khẩu"); // Thông báo lỗi nếu mật khẩu sai
                                                                tvPasswordError.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    }
                                                } else { // Nếu không tìm thấy trong cả "users" và "admins"
                                                    tvUsernameError.setText("Tên đăng nhập không tồn tại"); // Thông báo lỗi
                                                    tvUsernameError.setVisibility(View.VISIBLE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(Dangnhap.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(Dangnhap.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi
                        }
                    });
        }
    }

    // Chuyển sang màn hình đăng ký
    public void onRegisterClick(View view) {
        Intent intent = new Intent(Dangnhap.this, DangKi.class); // Chuẩn bị chuyển sang màn hình DangKi
        startActivity(intent); // Mở màn hình đăng ký
    }

    // Lưu trạng thái khi xoay màn hình
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_USERNAME, edtUsername.getText().toString()); // Lưu tên đăng nhập
        outState.putString(KEY_PASSWORD, edtPassword.getText().toString()); // Lưu mật khẩu
        outState.putBoolean(KEY_USERNAME_ERROR_VISIBLE, tvUsernameError.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi tên đăng nhập
        outState.putBoolean(KEY_PASSWORD_ERROR_VISIBLE, tvPasswordError.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi mật khẩu
    }
}