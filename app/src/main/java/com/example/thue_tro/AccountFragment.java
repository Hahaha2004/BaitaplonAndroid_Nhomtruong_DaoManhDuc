package com.example.thue_tro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    // Các thành phần giao diện để hiển thị thông tin người dùng và tương tác
    private TextView tvUserName, tvViewPostedPosts;
    // Tham chiếu Firebase để truy cập vào các node "users" và "admins" trong cơ sở dữ liệu
    private DatabaseReference usersReference;
    private DatabaseReference adminsReference;
    // Biến lưu tên đăng nhập và vai trò (admin hoặc người dùng thường) của người dùng hiện tại
    private String currentUsername;
    private String userRole;

    // Constructor mặc định bắt buộc cho Fragment
    public AccountFragment() {
        // Constructor rỗng bắt buộc
    }

    // Được gọi để tạo giao diện của Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Nạp layout fragment_account.xml vào để hiển thị giao diện
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Ánh xạ các thành phần giao diện từ layout
        tvUserName = view.findViewById(R.id.tvUserName); // TextView hiển thị tên người dùng
        tvViewPostedPosts = view.findViewById(R.id.tvViewPostedPosts); // TextView hiển thị tùy chọn xem bài đăng
        LinearLayout layoutChangePassword = view.findViewById(R.id.layoutChangePassword); // Layout để chuyển đến màn hình đổi mật khẩu
        LinearLayout layoutViewPostedPosts = view.findViewById(R.id.layoutViewPostedPosts); // Layout để xem bài đã đăng
        LinearLayout layoutDeleteAccount = view.findViewById(R.id.layoutDeleteAccount); // Layout để xóa tài khoản
        Button btnLogout = view.findViewById(R.id.btnLogout); // Nút đăng xuất
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile); // Nút chỉnh sửa hồ sơ

        // Khởi tạo tham chiếu Firebase đến node "users" và "admins"
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy thông tin tên đăng nhập và vai trò từ HomeActivity
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            currentUsername = homeActivity.getCurrentUsername(); // Lấy tên đăng nhập
            userRole = homeActivity.getUserRole(); // Lấy vai trò (admin/user)
            Log.d("AccountFragment", "Vai trò người dùng: " + userRole); // Ghi log để kiểm tra vai trò
        }

        // Nếu có tên đăng nhập, tải dữ liệu người dùng
        if (currentUsername != null) {
            loadUserData(currentUsername);
        } else {
            // Hiển thị thông báo nếu không tìm thấy thông tin người dùng
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }

        // Điều chỉnh giao diện dựa trên vai trò người dùng
        if ("admin".equals(userRole)) {
            tvViewPostedPosts.setText("Xem tất cả bài đăng"); // Admin thấy tất cả bài đăng
        } else {
            tvViewPostedPosts.setText("Xem bài đã đăng"); // Người dùng thường chỉ thấy bài của họ
        }

        // Xử lý sự kiện khi nhấn nút chỉnh sửa hồ sơ
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            intent.putExtra("username", currentUsername); // Gửi tên đăng nhập
            intent.putExtra("role", userRole); // Gửi vai trò
            startActivity(intent); // Chuyển đến màn hình chỉnh sửa hồ sơ
        });

        // Xử lý sự kiện khi nhấn vào mục đổi mật khẩu
        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            intent.putExtra("username", currentUsername); // Gửi tên đăng nhập
            intent.putExtra("role", userRole); // Gửi vai trò
            startActivity(intent); // Chuyển đến màn hình đổi mật khẩu
        });

        // Xử lý sự kiện khi nhấn vào mục xem bài đã đăng
        layoutViewPostedPosts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PostedPostsActivity.class);
            intent.putExtra("username", currentUsername); // Gửi tên đăng nhập
            intent.putExtra("role", userRole); // Gửi vai trò
            startActivity(intent); // Chuyển đến màn hình xem bài đăng
        });

        // Xử lý sự kiện khi nhấn vào mục xóa tài khoản
        layoutDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog()); // Hiển thị hộp thoại xác nhận xóa

        // Xử lý sự kiện khi nhấn nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Dangnhap.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa stack activity
            startActivity(intent); // Chuyển đến màn hình đăng nhập
            if (getActivity() != null) {
                getActivity().finish(); // Đóng activity hiện tại
            }
        });

        // Trả về giao diện đã được tạo
        return view;
    }

    // Hàm tải dữ liệu người dùng từ Firebase
    private void loadUserData(String username) {
        // Chọn node phù hợp dựa trên vai trò (admin hoặc user)
        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

        // Tìm dữ liệu người dùng theo tên đăng nhập
        targetReference.orderByChild("dangNhap").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Nếu tìm thấy dữ liệu
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class); // Chuyển dữ liệu thành đối tượng User
                                if (user != null) {
                                    // Hiển thị tên người dùng lên TextView
                                    tvUserName.setText(user.nguoiDung != null ? user.nguoiDung : "Không có tên");
                                }
                            }
                        } else {
                            // Hiển thị thông báo nếu không tìm thấy dữ liệu
                            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Hiển thị thông báo lỗi nếu truy vấn bị hủy
                        Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm hiển thị hộp thoại xác nhận xóa tài khoản
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có muốn xóa tài khoản này không?")
                .setPositiveButton("Có", (dialog, which) -> deleteAccount()) // Gọi hàm xóa nếu người dùng đồng ý
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss()) // Đóng hộp thoại nếu từ chối
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Hàm xóa tài khoản khỏi Firebase
    private void deleteAccount() {
        if (currentUsername != null) {
            // Chọn node phù hợp dựa trên vai trò
            DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;
            // Tìm tài khoản theo tên đăng nhập
            targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Nếu tìm thấy tài khoản
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // Xóa tài khoản khỏi Firebase
                                    snapshot.getRef().removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                // Hiển thị thông báo thành công
                                                Toast.makeText(getContext(), "Tài khoản đã được xóa!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), Dangnhap.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent); // Chuyển về màn hình đăng nhập
                                                if (getActivity() != null) {
                                                    getActivity().finish(); // Đóng activity hiện tại
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                // Hiển thị thông báo lỗi nếu xóa thất bại
                                                Toast.makeText(getContext(), "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                // Hiển thị thông báo nếu không tìm thấy tài khoản
                                Toast.makeText(getContext(), "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Hiển thị thông báo lỗi nếu truy vấn bị hủy
                            Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Hiển thị thông báo nếu không có thông tin người dùng
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }
    }
}