package com.example.thue_tro; // Định nghĩa gói (package) chứa lớp này, giúp tổ chức code trong dự án

import android.content.Intent; // Thư viện để chuyển đổi giữa các Activity (màn hình) trong ứng dụng
import android.os.Bundle; // Thư viện để lưu trữ và khôi phục trạng thái của Activity (ví dụ: khi xoay màn hình)
import android.widget.Button; // Thư viện cung cấp đối tượng Button (nút bấm) để sử dụng trong giao diện
import androidx.appcompat.app.AppCompatActivity; // Thư viện cơ sở cho Activity hiện đại, hỗ trợ các tính năng mới của Android

public class MainActivity extends AppCompatActivity { // Lớp MainActivity kế thừa từ AppCompatActivity, là màn hình chính của ứng dụng

    private Button btnBatDau; // Khai báo một biến Button (nút bấm) để tương tác với giao diện

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Phương thức được gọi khi Activity khởi tạo
        super.onCreate(savedInstanceState); // Gọi phương thức gốc từ lớp cha để đảm bảo Activity khởi tạo đúng cách
        setContentView(R.layout.activity_main); // Liên kết Activity với file giao diện XML (activity_main.xml)

        btnBatDau = findViewById(R.id.btn_bat_dau); // Tìm và gán nút bấm từ giao diện XML dựa trên ID (btn_bat_dau)
        if (btnBatDau != null) { // Kiểm tra xem nút bấm có được tìm thấy không (tránh lỗi null)
            btnBatDau.setOnClickListener(v -> { // Gắn sự kiện khi người dùng nhấn vào nút bấm
                Intent intent = new Intent(MainActivity.this, Dangnhap.class); // Tạo một Intent để chuyển từ MainActivity sang màn hình Dangnhap
                startActivity(intent); // Bắt đầu Activity mới (mở màn hình Dangnhap)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Thêm hiệu ứng chuyển cảnh mờ dần (fade) khi chuyển màn hình
            });
        }
    }
}