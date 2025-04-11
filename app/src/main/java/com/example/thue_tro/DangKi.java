package com.example.thue_tro; // Định nghĩa gói chứa lớp này, giúp tổ chức code trong dự án

import android.app.AlertDialog; // Thư viện để hiển thị hộp thoại (dialog)
import android.content.Intent; // Thư viện để chuyển đổi giữa các Activity (màn hình)
import android.graphics.Color; // Thư viện để xử lý màu sắc
import android.graphics.drawable.ColorDrawable; // Thư viện để tạo nền trong suốt cho dialog
import android.os.Bundle; // Thư viện để lưu trữ trạng thái của Activity
import android.text.Editable; // Thư viện để xử lý văn bản có thể chỉnh sửa
import android.text.InputType; // Thư viện để thay đổi kiểu nhập liệu (ví dụ: ẩn/hiện mật khẩu)
import android.text.TextWatcher; // Thư viện để theo dõi thay đổi văn bản trong ô nhập liệu
import android.util.Log; // Thư viện để ghi log (dùng để debug)
import android.util.Patterns; // Thư viện để kiểm tra định dạng (ví dụ: email)
import android.view.MotionEvent; // Thư viện để xử lý sự kiện chạm trên màn hình
import android.view.View; // Thư viện cơ sở cho các thành phần giao diện
import android.widget.Button; // Thư viện cung cấp đối tượng Button (nút bấm)
import android.widget.CheckBox; // Thư viện cung cấp ô checkbox
import android.widget.EditText; // Thư viện cung cấp ô nhập liệu
import android.widget.TextView; // Thư viện cung cấp đối tượng văn bản hiển thị
import android.widget.Toast; // Thư viện để hiển thị thông báo ngắn

import androidx.annotation.NonNull; // Annotation để đảm bảo tham số không null
import androidx.appcompat.app.AppCompatActivity; // Thư viện cơ sở cho Activity hiện đại

import com.google.android.material.button.MaterialButton; // Thư viện nút kiểu Material Design
import com.google.firebase.database.DataSnapshot; // Thư viện Firebase để lấy dữ liệu từ cơ sở dữ liệu
import com.google.firebase.database.DatabaseError; // Thư viện Firebase để xử lý lỗi cơ sở dữ liệu
import com.google.firebase.database.DatabaseReference; // Thư viện Firebase để tham chiếu đến một vị trí trong cơ sở dữ liệu
import com.google.firebase.database.FirebaseDatabase; // Thư viện chính để kết nối với Firebase Realtime Database
import com.google.firebase.database.ValueEventListener; // Thư viện Firebase để lắng nghe sự thay đổi dữ liệu

public class DangKi extends AppCompatActivity { // Lớp DangKi kế thừa từ AppCompatActivity, là màn hình đăng ký

    // Khai báo các thành phần giao diện và biến
    private EditText edtNguoiDung, edtDangNhap, edtEmail, edtMatKhau, edtNhapLaiMatKhau, edtSoDienThoai; // Các ô nhập liệu
    private TextView tvLoiNguoiDung, tvLoiDangNhap, tvLoiEmail, tvLoiMatKhau, tvLoiNhapLaiMatKhau, tvLoiSoDienThoai; // Các văn bản lỗi
    private TextView btnQuayLai; // Nút "Quay lại"
    private Button btnDangKi; // Nút "Đăng ký"
    private DatabaseReference usersReference; // Tham chiếu đến node "users" trong Firebase
    private DatabaseReference adminsReference; // Tham chiếu đến node "admins" trong Firebase
    private boolean isPasswordVisible = false; // Trạng thái ẩn/hiện mật khẩu
    private boolean isConfirmPasswordVisible = false; // Trạng thái ẩn/hiện nhập lại mật khẩu
    private boolean hasAgreedToTerms = false; // Trạng thái đồng ý điều khoản
    private AlertDialog successDialog; // Hộp thoại hiển thị khi đăng ký thành công

    // Các hằng số để lưu trạng thái khi xoay màn hình
    private static final String KEY_NGUOI_DUNG = "nguoi_dung"; // Key để lưu tên người dùng
    private static final String KEY_DANG_NHAP = "dang_nhap"; // Key để lưu tên đăng nhập
    private static final String KEY_EMAIL = "email"; // Key để lưu email
    private static final String KEY_MAT_KHAU = "mat_khau"; // Key để lưu mật khẩu
    private static final String KEY_NHAP_LAI_MAT_KHAU = "nhap_lai_mat_khau"; // Key để lưu nhập lại mật khẩu
    private static final String KEY_SO_DIEN_THOAI = "so_dien_thoai"; // Key để lưu số điện thoại
    private static final String KEY_LOI_NGUOI_DUNG_VISIBLE = "loi_nguoi_dung_visible"; // Key để lưu trạng thái lỗi tên người dùng
    private static final String KEY_LOI_DANG_NHAP_VISIBLE = "loi_dang_nhap_visible"; // Key để lưu trạng thái lỗi tên đăng nhập
    private static final String KEY_LOI_EMAIL_VISIBLE = "loi_email_visible"; // Key để lưu trạng thái lỗi email
    private static final String KEY_LOI_MAT_KHAU_VISIBLE = "loi_mat_khau_visible"; // Key để lưu trạng thái lỗi mật khẩu
    private static final String KEY_LOI_NHAP_LAI_MAT_KHAU_VISIBLE = "loi_nhap_lai_mat_khau_visible"; // Key để lưu trạng thái lỗi nhập lại mật khẩu
    private static final String KEY_LOI_SO_DIEN_THOAI_VISIBLE = "loi_so_dien_thoai_visible"; // Key để lưu trạng thái lỗi số điện thoại
    private static final String KEY_TERMS_AGREED = "terms_agreed"; // Key để lưu trạng thái đồng ý điều khoản

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Phương thức được gọi khi Activity khởi tạo
        super.onCreate(savedInstanceState); // Gọi phương thức gốc từ lớp cha để khởi tạo Activity
        setContentView(R.layout.dang_ki); // Liên kết Activity với file giao diện XML (dang_ki.xml)

        // Kết nối tới Firebase Realtime Database
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users"); // Tham chiếu đến node "users"
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins"); // Tham chiếu đến node "admins"

        // Liên kết các thành phần giao diện với code
        edtNguoiDung = findViewById(R.id.edtNguoiDung); // Ô nhập tên người dùng
        edtDangNhap = findViewById(R.id.edtDangNhap); // Ô nhập tên đăng nhập
        edtEmail = findViewById(R.id.edtEmail); // Ô nhập email
        edtMatKhau = findViewById(R.id.edtMatKhau); // Ô nhập mật khẩu
        edtNhapLaiMatKhau = findViewById(R.id.edtNhapLaiMatKhau); // Ô nhập lại mật khẩu
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai); // Ô nhập số điện thoại

        tvLoiNguoiDung = findViewById(R.id.loiNguoiDung); // Văn bản lỗi tên người dùng
        tvLoiDangNhap = findViewById(R.id.loiDangNhap); // Văn bản lỗi tên đăng nhập
        tvLoiEmail = findViewById(R.id.loiEmail); // Văn bản lỗi email
        tvLoiMatKhau = findViewById(R.id.loiMatKhau); // Văn bản lỗi mật khẩu
        tvLoiNhapLaiMatKhau = findViewById(R.id.loiNhapLaiMatKhau); // Văn bản lỗi nhập lại mật khẩu
        tvLoiSoDienThoai = findViewById(R.id.loiSoDienThoai); // Văn bản lỗi số điện thoại

        btnDangKi = findViewById(R.id.btnDangKi); // Nút "Đăng ký"
        btnQuayLai = findViewById(R.id.btnQuayLai); // Nút "Quay lại"

        // Ban đầu nút "Đăng ký" bị vô hiệu hóa cho đến khi đồng ý điều khoản
        btnDangKi.setEnabled(false);

        // Tự động chuyển tên người dùng thành chữ in hoa
        edtNguoiDung.addTextChangedListener(new TextWatcher() { // Lắng nghe thay đổi trong ô tên người dùng
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Trước khi thay đổi (không dùng)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {} // Trong khi thay đổi (không dùng)
            @Override
            public void afterTextChanged(Editable s) { // Sau khi thay đổi
                String text = s.toString(); // Lấy nội dung hiện tại
                String upperText = text.toUpperCase(); // Chuyển thành chữ in hoa
                if (!text.equals(upperText)) { // Nếu chưa phải chữ in hoa
                    edtNguoiDung.removeTextChangedListener(this); // Ngừng lắng nghe để tránh vòng lặp
                    edtNguoiDung.setText(upperText); // Đặt lại thành chữ in hoa
                    edtNguoiDung.setSelection(upperText.length()); // Đặt con trỏ về cuối
                    edtNguoiDung.addTextChangedListener(this); // Tiếp tục lắng nghe
                }
            }
        });

        // Khôi phục trạng thái nếu có (khi xoay màn hình)
        if (savedInstanceState != null) {
            edtNguoiDung.setText(savedInstanceState.getString(KEY_NGUOI_DUNG)); // Đặt lại tên người dùng
            edtDangNhap.setText(savedInstanceState.getString(KEY_DANG_NHAP)); // Đặt lại tên đăng nhập
            edtEmail.setText(savedInstanceState.getString(KEY_EMAIL)); // Đặt lại email
            edtMatKhau.setText(savedInstanceState.getString(KEY_MAT_KHAU)); // Đặt lại mật khẩu
            edtNhapLaiMatKhau.setText(savedInstanceState.getString(KEY_NHAP_LAI_MAT_KHAU)); // Đặt lại nhập lại mật khẩu
            edtSoDienThoai.setText(savedInstanceState.getString(KEY_SO_DIEN_THOAI)); // Đặt lại số điện thoại
            tvLoiNguoiDung.setVisibility(savedInstanceState.getBoolean(KEY_LOI_NGUOI_DUNG_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị/ẩn lỗi tên người dùng
            tvLoiDangNhap.setVisibility(savedInstanceState.getBoolean(KEY_LOI_DANG_NHAP_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị/ẩn lỗi tên đăng nhập
            tvLoiEmail.setVisibility(savedInstanceState.getBoolean(KEY_LOI_EMAIL_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị/ẩn lỗi email
            tvLoiMatKhau.setVisibility(savedInstanceState.getBoolean(KEY_LOI_MAT_KHAU_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị/ẩn lỗi mật khẩu
            tvLoiNhapLaiMatKhau.setVisibility(savedInstanceState.getBoolean(KEY_LOI_NHAP_LAI_MAT_KHAU_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị/ẩn lỗi nhập lại mật khẩu
            tvLoiSoDienThoai.setVisibility(savedInstanceState.getBoolean(KEY_LOI_SO_DIEN_THOAI_VISIBLE) ? View.VISIBLE : View.GONE); // Hiển thị/ẩn lỗi số điện thoại
            hasAgreedToTerms = savedInstanceState.getBoolean(KEY_TERMS_AGREED, false); // Khôi phục trạng thái đồng ý điều khoản
            btnDangKi.setEnabled(hasAgreedToTerms); // Cập nhật trạng thái nút "Đăng ký"
        }

        // Thiết lập chức năng ẩn/hiện mật khẩu
        setupPasswordToggle(edtMatKhau, () -> isPasswordVisible = !isPasswordVisible); // Cho ô mật khẩu
        setupPasswordToggle(edtNhapLaiMatKhau, () -> isConfirmPasswordVisible = !isConfirmPasswordVisible); // Cho ô nhập lại mật khẩu

        // Sự kiện nhấn nút
        btnDangKi.setOnClickListener(v -> validateAndRegister()); // Khi nhấn "Đăng ký"
        btnQuayLai.setOnClickListener(v -> finish()); // Khi nhấn "Quay lại" thì đóng Activity

        // Hiển thị dialog điều khoản nếu chưa đồng ý
        if (!hasAgreedToTerms) {
            showTermsDialog(); // Gọi hàm hiển thị dialog điều khoản
        }
    }

    // Hiển thị dialog điều khoản sử dụng
    private void showTermsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Tạo đối tượng xây dựng dialog
        View dialogView = getLayoutInflater().inflate(R.layout.terms_dialog, null); // Nạp giao diện từ file XML
        builder.setView(dialogView); // Gắn giao diện vào dialog

        AlertDialog dialog = builder.create(); // Tạo dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt

        CheckBox cbAgreeTerms = dialogView.findViewById(R.id.cbAgreeTerms); // Checkbox đồng ý điều khoản
        MaterialButton btnConfirmTerms = dialogView.findViewById(R.id.btnConfirmTerms); // Nút xác nhận

        btnConfirmTerms.setEnabled(false); // Ban đầu nút xác nhận bị vô hiệu hóa

        cbAgreeTerms.setOnCheckedChangeListener((buttonView, isChecked) -> btnConfirmTerms.setEnabled(isChecked)); // Kích hoạt nút khi checkbox được chọn

        btnConfirmTerms.setOnClickListener(v -> { // Sự kiện nhấn nút xác nhận
            if (cbAgreeTerms.isChecked()) { // Nếu checkbox được chọn
                hasAgreedToTerms = true; // Đánh dấu đã đồng ý điều khoản
                btnDangKi.setEnabled(true); // Kích hoạt nút "Đăng ký"
                dialog.dismiss(); // Đóng dialog
            }
        });

        dialog.setCancelable(false); // Không cho đóng dialog bằng nút back
        dialog.show(); // Hiển thị dialog
    }

    // Thiết lập chức năng ẩn/hiện mật khẩu
    private void setupPasswordToggle(EditText editText, Runnable toggleVisibility) {
        editText.setOnTouchListener((v, event) -> { // Lắng nghe sự kiện chạm vào ô nhập liệu
            if (event.getAction() == MotionEvent.ACTION_UP) { // Khi người dùng thả tay
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) { // Nếu chạm vào biểu tượng mắt
                    toggleVisibility.run(); // Đổi trạng thái ẩn/hiện
                    if (editText == edtMatKhau) { // Nếu là ô mật khẩu
                        editText.setInputType(isPasswordVisible ? // Cập nhật kiểu nhập liệu
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, isPasswordVisible ? R.drawable.ic_eye_open : R.drawable.ic_eye_closed, 0); // Đổi biểu tượng mắt
                    } else if (editText == edtNhapLaiMatKhau) { // Nếu là ô nhập lại mật khẩu
                        editText.setInputType(isConfirmPasswordVisible ?
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, isConfirmPasswordVisible ? R.drawable.ic_eye_open : R.drawable.ic_eye_closed, 0); // Đổi biểu tượng mắt
                    }
                    editText.setSelection(editText.getText().length()); // Đặt con trỏ về cuối
                    return true; // Xác nhận sự kiện đã được xử lý
                }
            }
            return false; // Sự kiện chưa được xử lý
        });
    }

    // Xác thực và đăng ký tài khoản
    private void validateAndRegister() {
        String nguoiDung = edtNguoiDung.getText().toString().trim(); // Lấy tên người dùng
        String dangNhap = edtDangNhap.getText().toString().trim(); // Lấy tên đăng nhập
        String email = edtEmail.getText().toString().trim(); // Lấy email
        String matKhau = edtMatKhau.getText().toString().trim(); // Lấy mật khẩu
        String nhapLaiMatKhau = edtNhapLaiMatKhau.getText().toString().trim(); // Lấy nhập lại mật khẩu
        String soDienThoai = edtSoDienThoai.getText().toString().trim(); // Lấy số điện thoại

        boolean isValid = true; // Biến kiểm tra dữ liệu hợp lệ

        // Kiểm tra Tên người dùng
        if (nguoiDung.isEmpty()) {
            tvLoiNguoiDung.setText("Vui lòng nhập tên người dùng"); // Lỗi nếu trống
            tvLoiNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (nguoiDung.length() < 2) {
            tvLoiNguoiDung.setText("Tên người dùng phải có ít nhất 2 ký tự"); // Lỗi nếu quá ngắn
            tvLoiNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (nguoiDung.matches(".*\\d.*")) {
            tvLoiNguoiDung.setText("Tên người dùng không được chứa số"); // Lỗi nếu chứa số
            tvLoiNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!nguoiDung.matches("[A-Z\\s]+")) {
            tvLoiNguoiDung.setText("Tên người dùng phải là chữ in hoa"); // Lỗi nếu không phải chữ in hoa
            tvLoiNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (nguoiDung.matches(".*[!@#$%^&*].*")) {
            tvLoiNguoiDung.setText("Tên người dùng không được chứa ký tự đặc biệt"); // Lỗi nếu chứa ký tự đặc biệt
            tvLoiNguoiDung.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvLoiNguoiDung.setVisibility(View.GONE); // Ẩn lỗi nếu hợp lệ
        }
        if (dangNhap.isEmpty()) { // Kiểm tra Tên đăng nhập
            tvLoiDangNhap.setText("Vui lòng nhập tên đăng nhập"); // Lỗi nếu trống
            tvLoiDangNhap.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (dangNhap.length() < 6) {
            tvLoiDangNhap.setText("Tên đăng nhập phải có ít nhất 6 ký tự"); // Lỗi nếu quá ngắn
            tvLoiDangNhap.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!dangNhap.matches("[a-zA-Z0-9]+")) {
            tvLoiDangNhap.setText("Tên đăng nhập chỉ được chứa chữ cái và số"); // Lỗi nếu chứa ký tự đặc biệt
            tvLoiDangNhap.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvLoiDangNhap.setVisibility(View.GONE); // Ẩn lỗi nếu hợp lệ
        }

        // Kiểm tra Email
        if (email.isEmpty()) {
            tvLoiEmail.setText("Vui lòng nhập Email"); // Lỗi nếu trống
            tvLoiEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvLoiEmail.setText("Email không hợp lệ"); // Lỗi nếu không đúng định dạng email
            tvLoiEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvLoiEmail.setVisibility(View.GONE); // Ẩn lỗi nếu hợp lệ
        }

        // Kiểm tra Mật khẩu
        if (matKhau.isEmpty()) {
            tvLoiMatKhau.setText("Vui lòng nhập mật khẩu"); // Lỗi nếu trống
            tvLoiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (matKhau.length() < 8) {
            tvLoiMatKhau.setText("Mật khẩu phải có ít nhất 8 ký tự"); // Lỗi nếu quá ngắn
            tvLoiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!matKhau.matches(".*[A-Za-z].*")) {
            tvLoiMatKhau.setText("Mật khẩu phải chứa ít nhất một chữ cái"); // Lỗi nếu không có chữ cái
            tvLoiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!matKhau.matches(".*\\d.*")) {
            tvLoiMatKhau.setText("Mật khẩu phải chứa ít nhất một số"); // Lỗi nếu không có số
            tvLoiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!matKhau.matches(".*[!@#$%^&*].*")) {
            tvLoiMatKhau.setText("Mật khẩu phải chứa ít nhất một ký tự đặc biệt (!@#$%^&*)"); // Lỗi nếu không có ký tự đặc biệt
            tvLoiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvLoiMatKhau.setVisibility(View.GONE); // Ẩn lỗi nếu hợp lệ
        }

        // Kiểm tra Nhập lại mật khẩu
        if (nhapLaiMatKhau.isEmpty()) {
            tvLoiNhapLaiMatKhau.setText("Vui lòng nhập lại mật khẩu"); // Lỗi nếu trống
            tvLoiNhapLaiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!nhapLaiMatKhau.equals(matKhau)) {
            tvLoiNhapLaiMatKhau.setText("Mật khẩu không khớp"); // Lỗi nếu không khớp với mật khẩu
            tvLoiNhapLaiMatKhau.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvLoiNhapLaiMatKhau.setVisibility(View.GONE); // Ẩn lỗi nếu hợp lệ
        }

        // Kiểm tra Số điện thoại
        if (soDienThoai.isEmpty()) {
            tvLoiSoDienThoai.setText("Vui lòng nhập số điện thoại"); // Lỗi nếu trống
            tvLoiSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!soDienThoai.matches("\\d+")) {
            tvLoiSoDienThoai.setText("Số điện thoại chỉ được chứa số"); // Lỗi nếu chứa ký tự không phải số
            tvLoiSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (soDienThoai.length() != 10) {
            tvLoiSoDienThoai.setText("Số điện thoại phải có 10 số"); // Lỗi nếu không đủ 10 số
            tvLoiSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!soDienThoai.startsWith("0")) {
            tvLoiSoDienThoai.setText("Số điện thoại phải bắt đầu bằng 0"); // Lỗi nếu không bắt đầu bằng 0
            tvLoiSoDienThoai.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvLoiSoDienThoai.setVisibility(View.GONE); // Ẩn lỗi nếu hợp lệ
        }

        if (isValid) { // Nếu tất cả dữ liệu hợp lệ
            checkExistingFields(dangNhap, email, soDienThoai, () -> { // Kiểm tra xem thông tin đã tồn tại chưa
                User user = new User(nguoiDung, dangNhap, email, matKhau, soDienThoai, "user"); // Tạo đối tượng người dùng mới
                String userId = usersReference.push().getKey(); // Tạo ID ngẫu nhiên cho người dùng
                usersReference.child(userId).setValue(user) // Lưu người dùng vào Firebase
                        // Nếu lưu dữ liệu thành công vào Firebase
                        .addOnSuccessListener(aVoid -> {
                            Log.d("DangKi", "Đăng ký thành công: " + dangNhap); // Ghi log khi thành công
                            if (!isFinishing() && !isDestroyed()) { // Kiểm tra Activity còn tồn tại
                                showSuccessDialog(); // Hiển thị dialog thành công
                            } else {
                                startDangNhapActivity(); // Nếu Activity đã hủy, chuyển thẳng sang Dangnhap
                            }
                        })
                        // Nếu đăng ký thất bại
                        .addOnFailureListener(e -> {
                            Log.e("DangKi", "Đăng ký thất bại: " + e.getMessage()); // Ghi log khi thất bại
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(DangKi.this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi
                            }
                        });
            });
        }
    }

    // Kiểm tra các trường đã tồn tại trong Firebase
    private void checkExistingFields(String dangNhap, String email, String phone, Runnable onSuccess) {
        boolean[] checks = new boolean[3]; // Mảng kiểm tra: 0 - tên đăng nhập, 1 - email, 2 - số điện thoại
        checks[0] = checks[1] = checks[2] = true; // Ban đầu giả định chưa tồn tại
        int[] completedChecks = {0}; // Đếm số lần kiểm tra hoàn tất

        // Kiểm tra tên đăng nhập trong "users" và "admins"
        checkField(usersReference, "dangNhap", dangNhap, () -> {
            tvLoiDangNhap.setText("Tên đăng nhập đã được sử dụng"); // Lỗi nếu tồn tại
            tvLoiDangNhap.setVisibility(View.VISIBLE);
            checks[0] = false;
        }, () -> checkField(adminsReference, "dangNhap", dangNhap, () -> {
            tvLoiDangNhap.setText("Tên đăng nhập đã được sử dụng"); // Lỗi nếu tồn tại
            tvLoiDangNhap.setVisibility(View.VISIBLE);
            checks[0] = false;
        }, () -> {
            completedChecks[0]++; // Tăng số lần kiểm tra hoàn tất
            proceedIfAllChecksDone(checks, completedChecks, onSuccess); // Tiếp tục nếu tất cả kiểm tra xong
        }));

        // Kiểm tra email trong "users" và "admins"
        checkField(usersReference, "email", email, () -> {
            tvLoiEmail.setText("Email đã được sử dụng"); // Lỗi nếu tồn tại
            tvLoiEmail.setVisibility(View.VISIBLE);
            checks[1] = false;
        }, () -> checkField(adminsReference, "email", email, () -> {
            tvLoiEmail.setText("Email đã được sử dụng"); // Lỗi nếu tồn tại
            tvLoiEmail.setVisibility(View.VISIBLE);
            checks[1] = false;
        }, () -> {
            completedChecks[0]++;
            proceedIfAllChecksDone(checks, completedChecks, onSuccess);
        }));

        // Kiểm tra số điện thoại trong "users" và "admins"
        checkField(usersReference, "soDienThoai", phone, () -> {
            tvLoiSoDienThoai.setText("Số điện thoại đã được sử dụng"); // Lỗi nếu tồn tại
            tvLoiSoDienThoai.setVisibility(View.VISIBLE);
            checks[2] = false;
        }, () -> checkField(adminsReference, "soDienThoai", phone, () -> {
            tvLoiSoDienThoai.setText("Số điện thoại đã được sử dụng"); // Lỗi nếu tồn tại
            tvLoiSoDienThoai.setVisibility(View.VISIBLE);
            checks[2] = false;
        }, () -> {
            completedChecks[0]++;
            proceedIfAllChecksDone(checks, completedChecks, onSuccess);
        }));
    }

    // Kiểm tra một trường cụ thể trong Firebase
    private void checkField(DatabaseReference reference, String field, String value, Runnable onExist, Runnable onNext) {
        reference.orderByChild(field).equalTo(value) // Tìm kiếm theo trường và giá trị
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) { // Nếu trường đã tồn tại
                            onExist.run(); // Gọi hàm xử lý khi tồn tại
                        }
                        onNext.run(); // Tiếp tục bước tiếp theo
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DangKi", "Lỗi kiểm tra " + field + ": " + databaseError.getMessage()); // Ghi log lỗi
                        onNext.run(); // Tiếp tục dù có lỗi
                    }
                });
    }

    // Tiếp tục nếu tất cả kiểm tra hoàn tất
    private void proceedIfAllChecksDone(boolean[] checks, int[] completedChecks, Runnable onSuccess) {
        if (completedChecks[0] == 3) { // Nếu đã kiểm tra xong 3 trường
            if (checks[0] && checks[1] && checks[2]) { // Nếu không có trường nào tồn tại
                onSuccess.run(); // Thực hiện đăng ký
            }
        }
    }

    // Hiển thị dialog đăng ký thành công
    private void showSuccessDialog() {
        if (isFinishing() || isDestroyed()) { // Kiểm tra Activity còn tồn tại không
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Tạo dialog
        View dialogView = getLayoutInflater().inflate(R.layout.success_dialog, null); // Nạp giao diện từ file XML
        builder.setView(dialogView);

        successDialog = builder.create(); // Tạo dialog
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt nền trong suốt

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage); // Văn bản thông báo
        Button btnOk = dialogView.findViewById(R.id.btnOk); // Nút "OK"

        tvMessage.setText("Đăng ký tài khoản thành công"); // Đặt nội dung thông báo

        btnOk.setOnClickListener(v -> { // Sự kiện nhấn "OK"
            if (successDialog != null && successDialog.isShowing()) {
                successDialog.dismiss(); // Đóng dialog
            }
            startDangNhapActivity(); // Chuyển sang màn hình đăng nhập
        });

        successDialog.setCancelable(false); // Không cho đóng bằng nút back
        if (!isFinishing() && !isDestroyed()) {
            successDialog.show(); // Hiển thị dialog
        }
    }

    // Chuyển sang màn hình đăng nhập
    private void startDangNhapActivity() {
        Intent intent = new Intent(DangKi.this, Dangnhap.class); // Chuẩn bị chuyển sang Dangnhap
        startActivity(intent); // Mở màn hình đăng nhập
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Thêm hiệu ứng chuyển cảnh
        finish(); // Đóng màn hình đăng ký
    }

    @Override
    protected void onDestroy() { // Khi Activity bị hủy
        super.onDestroy();
        if (successDialog != null && successDialog.isShowing()) { // Nếu dialog đang hiển thị
            successDialog.dismiss(); // Đóng dialog
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { // Lưu trạng thái khi xoay màn hình
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NGUOI_DUNG, edtNguoiDung.getText().toString()); // Lưu tên người dùng
        outState.putString(KEY_DANG_NHAP, edtDangNhap.getText().toString()); // Lưu tên đăng nhập
        outState.putString(KEY_EMAIL, edtEmail.getText().toString()); // Lưu email
        outState.putString(KEY_MAT_KHAU, edtMatKhau.getText().toString()); // Lưu mật khẩu
        outState.putString(KEY_NHAP_LAI_MAT_KHAU, edtNhapLaiMatKhau.getText().toString()); // Lưu nhập lại mật khẩu
        outState.putString(KEY_SO_DIEN_THOAI, edtSoDienThoai.getText().toString()); // Lưu số điện thoại
        outState.putBoolean(KEY_LOI_NGUOI_DUNG_VISIBLE, tvLoiNguoiDung.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi tên người dùng
        outState.putBoolean(KEY_LOI_DANG_NHAP_VISIBLE, tvLoiDangNhap.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi tên đăng nhập
        outState.putBoolean(KEY_LOI_EMAIL_VISIBLE, tvLoiEmail.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi email
        outState.putBoolean(KEY_LOI_MAT_KHAU_VISIBLE, tvLoiMatKhau.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi mật khẩu
        outState.putBoolean(KEY_LOI_NHAP_LAI_MAT_KHAU_VISIBLE, tvLoiNhapLaiMatKhau.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi nhập lại mật khẩu
        outState.putBoolean(KEY_LOI_SO_DIEN_THOAI_VISIBLE, tvLoiSoDienThoai.getVisibility() == View.VISIBLE); // Lưu trạng thái lỗi số điện thoại
        outState.putBoolean(KEY_TERMS_AGREED, hasAgreedToTerms); // Lưu trạng thái đồng ý điều khoản
    }

    // Lớp User để lưu thông tin người dùng
    public static class User {
        public String nguoiDung, dangNhap, email, matKhau, soDienThoai, role; // Các thuộc tính của người dùng

        public User() {} // Constructor mặc định cho Firebase
        public User(String nguoiDung, String dangNhap, String email, String matKhau, String soDienThoai, String role) { // Constructor có tham số
            this.nguoiDung = nguoiDung;
            this.dangNhap = dangNhap;
            this.email = email;
            this.matKhau = matKhau;
            this.soDienThoai = soDienThoai;
            this.role = role;
        }
    }
}