package com.example.thue_tro; // Định nghĩa gói chứa lớp này, giúp tổ chức code trong dự án

import android.content.Intent; // Thư viện để chuyển đổi giữa các Activity và nhận dữ liệu từ Intent
import android.os.Bundle; // Thư viện để lưu trữ trạng thái của Activity
import android.util.Log; // Thư viện để ghi log (dùng để debug)

import androidx.appcompat.app.AppCompatActivity; // Thư viện cơ sở cho Activity hiện đại
import androidx.fragment.app.Fragment; // Thư viện để làm việc với Fragment (các phần giao diện con)

import com.google.android.material.bottomnavigation.BottomNavigationView; // Thư viện cung cấp thanh điều hướng dưới dạng Material Design

public class HomeActivity extends AppCompatActivity { // Lớp HomeActivity kế thừa từ AppCompatActivity, là màn hình chính sau khi đăng nhập

    private String currentUsername; // Biến lưu tên đăng nhập của người dùng hiện tại
    private String userRole; // Biến lưu vai trò của người dùng (user hoặc admin)
    private BottomNavigationView bottomNavigationView; // Thanh điều hướng dưới cùng

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Phương thức được gọi khi Activity khởi tạo
        super.onCreate(savedInstanceState); // Gọi phương thức gốc từ lớp cha để khởi tạo Activity
        setContentView(R.layout.activity_home); // Liên kết Activity với file giao diện XML (activity_home.xml)

        // Lấy username và role từ Intent gửi từ màn hình đăng nhập
        currentUsername = getIntent().getStringExtra("username"); // Lấy tên đăng nhập từ Intent
        userRole = getIntent().getStringExtra("role"); // Lấy vai trò từ Intent
        Log.d("HomeActivity", "User role: " + userRole); // Ghi log để kiểm tra vai trò người dùng

        // Ánh xạ BottomNavigationView từ giao diện
        bottomNavigationView = findViewById(R.id.bottom_navigation); // Tìm thanh điều hướng dưới cùng theo ID

        // Thiết lập sự kiện khi người dùng chọn một mục trong BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null; // Biến lưu Fragment được chọn
            int itemId = item.getItemId(); // Lấy ID của mục được chọn
            if (itemId == R.id.navigation_home) { // Nếu chọn mục "Trang chủ"
                selectedFragment = new HomeFragment(); // Tạo Fragment Trang chủ
            } else if (itemId == R.id.navigation_search) { // Nếu chọn mục "Tìm kiếm"
                selectedFragment = new SearchFragment(); // Tạo Fragment Tìm kiếm
            } else if (itemId == R.id.navigation_post) { // Nếu chọn mục "Đăng bài"
                selectedFragment = new PostFragment(); // Tạo Fragment Đăng bài
            } else if (itemId == R.id.navigation_account) { // Nếu chọn mục "Tài khoản"
                selectedFragment = new AccountFragment(); // Tạo Fragment Tài khoản
            }
            if (selectedFragment != null) { // Nếu Fragment được chọn hợp lệ
                Bundle bundle = new Bundle(); // Tạo Bundle để gửi dữ liệu cho Fragment
                bundle.putString("username", currentUsername); // Gửi tên đăng nhập
                bundle.putString("role", userRole); // Gửi vai trò
                selectedFragment.setArguments(bundle); // Gắn dữ liệu vào Fragment
                getSupportFragmentManager().beginTransaction() // Bắt đầu giao dịch với FragmentManager
                        .replace(R.id.fragment_container, selectedFragment) // Thay thế Fragment trong container
                        .addToBackStack(null) // Thêm vào stack để quay lại được
                        .commit(); // Thực thi giao dịch
            }
            return true; // Xác nhận sự kiện đã được xử lý
        });

        // Xử lý Intent khi Activity khởi động
        handleIntent(getIntent()); // Gọi hàm xử lý Intent ban đầu
    }

    @Override
    protected void onNewIntent(Intent intent) { // Phương thức được gọi khi có Intent mới gửi đến Activity đang chạy
        super.onNewIntent(intent); // Gọi phương thức gốc từ lớp cha
        setIntent(intent); // Cập nhật Intent mới
        handleIntent(intent); // Xử lý Intent mới
    }

    // Hàm xử lý Intent để hiển thị Fragment tương ứng
    private void handleIntent(Intent intent) {
        String fragmentToShow = intent.getStringExtra("fragment_to_show"); // Lấy thông tin Fragment cần hiển thị từ Intent
        Fragment selectedFragment = null; // Biến lưu Fragment được chọn
        if ("home".equals(fragmentToShow)) { // Nếu Intent yêu cầu hiển thị "Trang chủ"
            selectedFragment = new HomeFragment(); // Tạo Fragment Trang chủ
            bottomNavigationView.setSelectedItemId(R.id.navigation_home); // Đánh dấu mục "Trang chủ" trên thanh điều hướng
        } else if ("search".equals(fragmentToShow)) { // Nếu Intent yêu cầu hiển thị "Tìm kiếm"
            selectedFragment = new SearchFragment(); // Tạo Fragment Tìm kiếm
            bottomNavigationView.setSelectedItemId(R.id.navigation_search); // Đánh dấu mục "Tìm kiếm" trên thanh điều hướng
        } else { // Mặc định hiển thị "Trang chủ"
            selectedFragment = new HomeFragment(); // Tạo Fragment Trang chủ
            bottomNavigationView.setSelectedItemId(R.id.navigation_home); // Đánh dấu mục "Trang chủ"
        }
        if (selectedFragment != null) { // Nếu Fragment được chọn hợp lệ
            Bundle bundle = new Bundle(); // Tạo Bundle để gửi dữ liệu
            bundle.putString("username", currentUsername); // Gửi tên đăng nhập
            bundle.putString("role", userRole); // Gửi vai trò
            selectedFragment.setArguments(bundle); // Gắn dữ liệu vào Fragment
            getSupportFragmentManager().beginTransaction() // Bắt đầu giao dịch với FragmentManager
                    .replace(R.id.fragment_container, selectedFragment) // Thay thế Fragment trong container
                    .addToBackStack(null) // Thêm vào stack để quay lại được
                    .commit(); // Thực thi giao dịch
        }
    }

    // Getter để lấy tên đăng nhập hiện tại
    public String getCurrentUsername() {
        return currentUsername; // Trả về tên đăng nhập
    }

    // Getter để lấy vai trò của người dùng
    public String getUserRole() {
        return userRole; // Trả về vai trò
    }
}