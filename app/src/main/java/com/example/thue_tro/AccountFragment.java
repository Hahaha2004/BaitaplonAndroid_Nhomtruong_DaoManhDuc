package com.example.thue_tro;

// Định nghĩa package (gói) của ứng dụng, giúp tổ chức code theo cấu trúc thư mục. Ở đây là com.example.thue_tro.

import android.app.AlertDialog;
// Import lớp AlertDialog để hiển thị hộp thoại xác nhận (ví dụ: khi xóa tài khoản).

import android.content.Intent;
// Import lớp Intent để chuyển đổi giữa các Activity (màn hình) trong ứng dụng.

import android.os.Bundle;
// Import lớp Bundle để lưu trữ và truyền dữ liệu giữa các Fragment/Activity.

import android.util.Log;
// Import lớp Log để ghi log (dùng để debug, in thông tin ra console).

import android.view.LayoutInflater;
// Import lớp LayoutInflater để chuyển file XML layout thành đối tượng View.

import android.view.View;
// Import lớp View để làm việc với các thành phần giao diện (như button, textview).

import android.view.ViewGroup;
// Import lớp ViewGroup để làm việc với các container chứa nhiều View (như LinearLayout).

import android.widget.Button;
// Import lớp Button để làm việc với nút bấm.

import android.widget.LinearLayout;
// Import lớp LinearLayout để làm việc với layout dạng tuyến tính (sắp xếp các View theo hàng/cột).

import android.widget.TextView;
// Import lớp TextView để làm việc với các văn bản hiển thị trên giao diện.

import android.widget.Toast;
// Import lớp Toast để hiển thị thông báo ngắn trên màn hình.

import androidx.fragment.app.Fragment;
// Import lớp Fragment để tạo Fragment (một phần giao diện trong Activity).

import com.google.firebase.database.DataSnapshot;
// Import lớp DataSnapshot để lấy dữ liệu từ Firebase Realtime Database.

import com.google.firebase.database.DatabaseError;
// Import lớp DatabaseError để xử lý lỗi khi làm việc với Firebase.

import com.google.firebase.database.DatabaseReference;
// Import lớp DatabaseReference để tham chiếu đến một node cụ thể trong Firebase.

import com.google.firebase.database.FirebaseDatabase;
// Import lớp FirebaseDatabase để kết nối và làm việc với Firebase Realtime Database.

import com.google.firebase.database.ValueEventListener;
// Import lớp ValueEventListener để lắng nghe sự thay đổi dữ liệu trong Firebase.

public class AccountFragment extends Fragment {
    // Định nghĩa lớp AccountFragment, kế thừa từ Fragment. Đây là một Fragment hiển thị thông tin tài khoản người dùng.

    private TextView tvUserName, tvViewPostedPosts;
    // Khai báo hai TextView: tvUserName để hiển thị tên người dùng, tvViewPostedPosts để hiển thị văn bản liên quan đến bài đăng.

    private DatabaseReference usersReference;
    // Khai báo tham chiếu đến node "users" trong Firebase (chứa thông tin người dùng thường).

    private DatabaseReference adminsReference;
    // Khai báo tham chiếu đến node "admins" trong Firebase (chứa thông tin admin).

    private String currentUsername;
    // Khai báo biến lưu tên đăng nhập của người dùng hiện tại.

    private String userRole;
    // Khai báo biến lưu vai trò của người dùng (admin hoặc user).

    public AccountFragment() {
        // Hàm khởi tạo rỗng, bắt buộc khi tạo Fragment. Không làm gì ở đây.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Phương thức onCreateView: tạo và trả về giao diện của Fragment.

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        // Chuyển file XML fragment_account.xml thành đối tượng View để hiển thị giao diện.

        // Ánh xạ (liên kết biến với các thành phần giao diện trong XML)
        tvUserName = view.findViewById(R.id.tvUserName);
        // Liên kết tvUserName với TextView có id tvUserName trong XML.

        tvViewPostedPosts = view.findViewById(R.id.tvViewPostedPosts);
        // Liên kết tvViewPostedPosts với TextView có id tvViewPostedPosts trong XML.

        LinearLayout layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        // Liên kết layoutChangePassword với LinearLayout có id layoutChangePassword.

        LinearLayout layoutViewPostedPosts = view.findViewById(R.id.layoutViewPostedPosts);
        // Liên kết layoutViewPostedPosts với LinearLayout có id layoutViewPostedPosts.

        LinearLayout layoutDeleteAccount = view.findViewById(R.id.layoutDeleteAccount);
        // Liên kết layoutDeleteAccount với LinearLayout có id layoutDeleteAccount.

        Button btnLogout = view.findViewById(R.id.btnLogout);
        // Liên kết btnLogout với Button có id btnLogout.

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        // Liên kết btnEditProfile với Button có id btnEditProfile.

        // Khởi tạo tham chiếu đến cả users và admins
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        // Tạo tham chiếu đến node "users" trong Firebase, sử dụng URL cụ thể của cơ sở dữ liệu.

        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");
        // Tạo tham chiếu đến node "admins" trong Firebase.

        // Lấy username và role từ HomeActivity
        HomeActivity homeActivity = (HomeActivity) getActivity();
        // Lấy tham chiếu đến HomeActivity (Activity chứa Fragment này).

        if (homeActivity != null) {
            // Kiểm tra nếu HomeActivity không null.
            currentUsername = homeActivity.getCurrentUsername();
            // Lấy tên đăng nhập từ HomeActivity và lưu vào currentUsername.

            userRole = homeActivity.getUserRole();
            // Lấy vai trò (admin/user) từ HomeActivity và lưu vào userRole.

            Log.d("AccountFragment", "User role: " + userRole);
            // Ghi log để kiểm tra giá trị của userRole (dùng để debug).
        }

        if (currentUsername != null) {
            // Kiểm tra nếu currentUsername không null.
            loadUserData(currentUsername);
            // Gọi hàm loadUserData để tải thông tin người dùng từ Firebase.
        } else {
            // Nếu không có username.
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            // Hiển thị thông báo ngắn: không tìm thấy thông tin người dùng.
        }

        // Điều chỉnh giao diện cho admin (user không có quyền)
        if ("admin".equals(userRole)) {
            // Nếu vai trò là admin.
            tvViewPostedPosts.setText("Xem tất cả bài đăng");
            // Đặt văn bản của tvViewPostedPosts thành "Xem tất cả bài đăng".
        } else {
            // Nếu không phải admin (là user).
            tvViewPostedPosts.setText("Xem bài đã đăng");
            // Đặt văn bản thành "Xem bài đã đăng".
        }

        // Sự kiện click
        btnEditProfile.setOnClickListener(v -> {
            // Gán sự kiện click cho nút btnEditProfile.
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            // Tạo Intent để chuyển sang UpdateProfileActivity.

            intent.putExtra("username", currentUsername);
            // Đưa username vào Intent để truyền sang Activity mới.

            intent.putExtra("role", userRole);
            // Đưa role vào Intent.

            startActivity(intent);
            // Khởi động UpdateProfileActivity.
        });

        layoutChangePassword.setOnClickListener(v -> {
            // Gán sự kiện click cho layoutChangePassword.
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            // Tạo Intent để chuyển sang ChangePasswordActivity.

            intent.putExtra("username", currentUsername);
            // Đưa username vào Intent.

            intent.putExtra("role", userRole);
            // Đưa role vào Intent.

            startActivity(intent);
            // Khởi động ChangePasswordActivity.
        });

        layoutViewPostedPosts.setOnClickListener(v -> {
            // Gán sự kiện click cho layoutViewPostedPosts.
            Intent intent = new Intent(getActivity(), PostedPostsActivity.class);
            // Tạo Intent để chuyển sang PostedPostsActivity.

            intent.putExtra("username", currentUsername);
            // Đưa username vào Intent.

            intent.putExtra("role", userRole);
            // Đưa role vào Intent.

            startActivity(intent);
            // Khởi động PostedPostsActivity.
        });

        layoutDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
        // Gán sự kiện click cho layoutDeleteAccount, gọi hàm showDeleteConfirmationDialog.

        btnLogout.setOnClickListener(v -> {
            // Gán sự kiện click cho nút btnLogout.
            Intent intent = new Intent(getActivity(), Dangnhap.class);
            // Tạo Intent để chuyển về màn hình đăng nhập (Dangnhap).

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            // Xóa toàn bộ Activity trước đó và tạo mới Activity đăng nhập.

            startActivity(intent);
            // Khởi động Activity Dangnhap.

            if (getActivity() != null) {
                // Kiểm tra nếu Activity hiện tại không null.
                getActivity().finish();
                // Đóng Activity hiện tại (HomeActivity).
            }
        });

        return view;
        // Trả về đối tượng View để hiển thị giao diện Fragment.
    }

    private void loadUserData(String username) {
        // Hàm loadUserData: tải thông tin người dùng từ Firebase dựa trên username.

        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;
        // Chọn node phù hợp (admins hoặc users) dựa trên vai trò userRole.

        targetReference.orderByChild("dangNhap").equalTo(username)
                // Tìm kiếm trong node với điều kiện trường "dangNhap" bằng username.

                .addListenerForSingleValueEvent(new ValueEventListener() {
                    // Thêm listener để lấy dữ liệu một lần duy nhất.

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Hàm được gọi khi dữ liệu được tải thành công.

                        if (dataSnapshot.exists()) {
                            // Kiểm tra nếu dữ liệu tồn tại.
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Duyệt qua các kết quả trả về (thường chỉ có 1).

                                DangKi.User user = snapshot.getValue(DangKi.User.class);
                                // Chuyển dữ liệu từ snapshot thành đối tượng User (lớp User trong DangKi).

                                if (user != null) {
                                    // Kiểm tra nếu user không null.
                                    tvUserName.setText(user.nguoiDung != null ? user.nguoiDung : "Không có tên");
                                    // Đặt tên người dùng vào tvUserName, nếu không có tên thì hiển thị "Không có tên".
                                }
                            }
                        } else {
                            // Nếu không tìm thấy dữ liệu.
                            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                            // Hiển thị thông báo ngắn.
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Hàm được gọi nếu có lỗi khi tải dữ liệu.
                        Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        // Hiển thị thông báo lỗi.
                    }
                });
    }

    private void showDeleteConfirmationDialog() {
        // Hàm hiển thị hộp thoại xác nhận xóa tài khoản.

        new AlertDialog.Builder(getContext())
                // Tạo một AlertDialog mới.

                .setTitle("Xác nhận xóa tài khoản")
                // Đặt tiêu đề cho hộp thoại.

                .setMessage("Bạn có muốn xóa tài khoản này không?")
                // Đặt nội dung thông báo.

                .setPositiveButton("Có", (dialog, which) -> deleteAccount())
                // Thêm nút "Có", khi nhấn sẽ gọi hàm deleteAccount.

                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                // Thêm nút "Không", khi nhấn sẽ đóng hộp thoại.

                .setIcon(android.R.drawable.ic_dialog_alert)
                // Đặt biểu tượng cảnh báo cho hộp thoại.

                .show();
        // Hiển thị hộp thoại.
    }

    private void deleteAccount() {
        // Hàm xóa tài khoản người dùng khỏi Firebase.

        if (currentUsername != null) {
            // Kiểm tra nếu username không null.

            DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;
            // Chọn node phù hợp (admins hoặc users) để xóa.

            targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                    // Tìm kiếm tài khoản với "dangNhap" bằng currentUsername.

                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        // Thêm listener để lấy dữ liệu một lần.

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Hàm được gọi khi dữ liệu được tải.

                            if (dataSnapshot.exists()) {
                                // Nếu tìm thấy tài khoản.
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // Duyệt qua các kết quả.

                                    snapshot.getRef().removeValue()
                                            // Xóa node tương ứng với tài khoản.

                                            .addOnSuccessListener(aVoid -> {
                                                // Nếu xóa thành công.
                                                Toast.makeText(getContext(), "Tài khoản đã được xóa!", Toast.LENGTH_SHORT).show();
                                                // Hiển thị thông báo thành công.

                                                Intent intent = new Intent(getActivity(), Dangnhap.class);
                                                // Tạo Intent để chuyển về màn hình đăng nhập.

                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                // Xóa các Activity trước đó.

                                                startActivity(intent);
                                                // Khởi động Activity Dangnhap.

                                                if (getActivity() != null) {
                                                    // Kiểm tra nếu Activity hiện tại không null.
                                                    getActivity().finish();
                                                    // Đóng Activity hiện tại.
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                // Nếu xóa thất bại.
                                                Toast.makeText(getContext(), "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                // Hiển thị thông báo lỗi.
                                            });
                                }
                            } else {
                                // Nếu không tìm thấy tài khoản.
                                Toast.makeText(getContext(), "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                                // Hiển thị thông báo.
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Nếu có lỗi khi truy vấn.
                            Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            // Hiển thị thông báo lỗi.
                        }
                    });
        } else {
            // Nếu không có username.
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            // Hiển thị thông báo.
        }
    }
}