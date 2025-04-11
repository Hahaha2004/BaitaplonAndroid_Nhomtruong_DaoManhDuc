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

    private TextView tvUserName, tvViewPostedPosts;
    private DatabaseReference usersReference; // Tham chiếu đến node users
    private DatabaseReference adminsReference; // Tham chiếu đến node admins
    private String currentUsername;
    private String userRole;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Ánh xạ
        tvUserName = view.findViewById(R.id.tvUserName);
        tvViewPostedPosts = view.findViewById(R.id.tvViewPostedPosts);
        LinearLayout layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        LinearLayout layoutViewPostedPosts = view.findViewById(R.id.layoutViewPostedPosts);
        LinearLayout layoutDeleteAccount = view.findViewById(R.id.layoutDeleteAccount);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Khởi tạo tham chiếu đến cả users và admins
        usersReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");
        adminsReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("admins");

        // Lấy username và role từ HomeActivity
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            currentUsername = homeActivity.getCurrentUsername();
            userRole = homeActivity.getUserRole();
            Log.d("AccountFragment", "User role: " + userRole); // Log để kiểm tra role
        }

        if (currentUsername != null) {
            loadUserData(currentUsername);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }

        // Điều chỉnh giao diện cho admin(user ko có quyển)
        if ("admin".equals(userRole)) {
            tvViewPostedPosts.setText("Xem tất cả bài đăng");
        } else {
            tvViewPostedPosts.setText("Xem bài đã đăng");
        }

        // Sự kiện click
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("role", userRole);
            startActivity(intent);
        });

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("role", userRole);
            startActivity(intent);
        });

        layoutViewPostedPosts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PostedPostsActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("role", userRole);
            startActivity(intent);
        });

        layoutDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Dangnhap.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadUserData(String username) {
        DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;

        targetReference.orderByChild("dangNhap").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DangKi.User user = snapshot.getValue(DangKi.User.class);
                                if (user != null) {
                                    tvUserName.setText(user.nguoiDung != null ? user.nguoiDung : "Không có tên");
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có muốn xóa tài khoản này không?")
                .setPositiveButton("Có", (dialog, which) -> deleteAccount())
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount() {
        if (currentUsername != null) {
            DatabaseReference targetReference = "admin".equals(userRole) ? adminsReference : usersReference;
            targetReference.orderByChild("dangNhap").equalTo(currentUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    snapshot.getRef().removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Tài khoản đã được xóa!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), Dangnhap.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                if (getActivity() != null) {
                                                    getActivity().finish();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Toast.makeText(getContext(), "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }
    }
}