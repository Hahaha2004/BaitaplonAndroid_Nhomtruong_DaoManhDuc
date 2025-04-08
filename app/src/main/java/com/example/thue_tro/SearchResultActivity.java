package com.example.thue_tro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<PostFragment.Post> postList;
    private List<PostFragment.Post> allPosts;
    private DatabaseReference databaseReference;
    private TextView tvQuayLai, tvLocation, tvNoResults, tvPageNumber;
    private Button btnPrevious, btnNext, btnSort;
    private String selectedCity, selectedDistrict, selectedAddress, selectedRoomType, selectedPriceRange;
    private String currentUsername, userRole;
    private int currentPage = 1;
    private final int POSTS_PER_PAGE = 10;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private boolean isSortEarliestToLatest = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // Ánh xạ các view
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        tvQuayLai = findViewById(R.id.tvQuayLai);
        tvLocation = findViewById(R.id.tvLocation);
        tvNoResults = findViewById(R.id.tvNoResults);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSort = findViewById(R.id.btnSort);

        // Khởi tạo database
        databaseReference = FirebaseDatabase.getInstance("https://baitaplon-f5860-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("posts");

        // Khởi tạo danh sách và adapter
        postList = new ArrayList<>();
        allPosts = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerViewPosts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewPosts.setAdapter(postAdapter);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        selectedCity = intent.getStringExtra("city");
        selectedDistrict = intent.getStringExtra("district");
        selectedAddress = intent.getStringExtra("address");
        selectedRoomType = intent.getStringExtra("roomType");
        selectedPriceRange = intent.getStringExtra("priceRange");
        currentUsername = intent.getStringExtra("username");
        userRole = intent.getStringExtra("role");

        // Cập nhật vị trí
        if (selectedCity != null && !selectedCity.isEmpty()) {
            tvLocation.setText("Khu vực " + selectedCity);
        } else {
            tvLocation.setText("Khu vực không xác định");
        }

        // Sự kiện nút Quay lại
        tvQuayLai.setOnClickListener(v -> navigateToSearchFragment());

        // Sự kiện nút phân trang
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                updatePage();
            }
        });

        btnNext.setOnClickListener(v -> {
            int totalPages = (int) Math.ceil((double) allPosts.size() / POSTS_PER_PAGE);
            if (currentPage < totalPages) {
                currentPage++;
                updatePage();
            }
        });

        // Sự kiện nút Sắp xếp
        btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(SearchResultActivity.this, btnSort);
            popup.getMenu().add("Ngày đăng bài từ sớm nhất đến muộn nhất");
            popup.getMenu().add("Ngày đăng bài từ muộn nhất đến sớm nhất");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Ngày đăng bài từ sớm nhất đến muộn nhất")) {
                    isSortEarliestToLatest = true;
                    btnSort.setText("Sắp xếp: Sớm đến muộn");
                } else {
                    isSortEarliestToLatest = false;
                    btnSort.setText("Sắp xếp: Muộn đến sớm");
                }
                sortPosts();
                updatePage();
                return true;
            });

            popup.show();
        });

        // Tải danh sách bài đăng
        loadPosts();
    }

    private void loadPosts() {
        Query query = databaseReference.orderByChild("city").equalTo(selectedCity);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allPosts.clear();
                postList.clear();
                Log.d("SearchResult", "Total posts found: " + dataSnapshot.getChildrenCount());

                List<PostFragment.Post> exactMatches = new ArrayList<>(); // Bài khớp hoàn toàn
                List<PostFragment.Post> relatedMatches = new ArrayList<>(); // Bài liên quan

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostFragment.Post post = snapshot.getValue(PostFragment.Post.class);
                    if (post != null && post.city != null && post.city.equals(selectedCity)) {
                        boolean matchesDistrict = selectedDistrict != null && post.district != null && post.district.equals(selectedDistrict);
                        boolean matchesRoomType = selectedRoomType != null && post.roomType != null && post.roomType.equals(selectedRoomType);
                        boolean matchesPrice = selectedPriceRange != null && post.price != null && isPriceInRange(post.price);
                        boolean matchesAddress = selectedAddress != null && !selectedAddress.isEmpty() &&
                                post.address != null && post.address.toLowerCase().contains(selectedAddress.toLowerCase());

                        // Trường hợp nhập địa chỉ: Bài khớp hoàn toàn (quận, loại phòng, địa chỉ, giá)
                        if (selectedAddress != null && !selectedAddress.isEmpty()) {
                            if (matchesDistrict && matchesRoomType && matchesPrice && matchesAddress) {
                                exactMatches.add(post);
                                Log.d("SearchResult", "Exact match: " + post.address);
                            } else if (matchesDistrict && matchesRoomType) { // Bài liên quan: chỉ cần khớp quận và loại phòng
                                relatedMatches.add(post);
                                Log.d("SearchResult", "Related match (district + roomType): " + post.address);
                            }
                        }
                        // Trường hợp không nhập địa chỉ: Khớp quận, loại phòng, giá
                        else {
                            if (matchesDistrict && matchesRoomType && matchesPrice) {
                                relatedMatches.add(post);
                                Log.d("SearchResult", "Match (no address): " + post.address);
                            }
                        }
                    }
                }

                // Sắp xếp bài liên quan theo thời gian (sớm nhất đến muộn nhất)
                Collections.sort(relatedMatches, new Comparator<PostFragment.Post>() {
                    @Override
                    public int compare(PostFragment.Post p1, PostFragment.Post p2) {
                        try {
                            Date date1 = p1.postDate != null ? dateFormat.parse(p1.postDate) : new Date(0);
                            Date date2 = p2.postDate != null ? dateFormat.parse(p2.postDate) : new Date(0);
                            return date1.compareTo(date2); // Sớm nhất đến muộn nhất
                        } catch (ParseException e) {
                            Log.e("SearchResult", "Error parsing date: " + e.getMessage());
                            return 0;
                        }
                    }
                });

                // Thêm bài khớp hoàn toàn lên đầu, sau đó là bài liên quan
                allPosts.addAll(exactMatches);
                allPosts.addAll(relatedMatches);

                if (allPosts.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                    recyclerViewPosts.setVisibility(View.GONE);
                    btnPrevious.setVisibility(View.GONE);
                    btnNext.setVisibility(View.GONE);
                    tvPageNumber.setVisibility(View.GONE);
                    btnSort.setVisibility(View.GONE);
                } else {
                    tvNoResults.setVisibility(View.GONE);
                    recyclerViewPosts.setVisibility(View.VISIBLE);
                    btnPrevious.setVisibility(View.VISIBLE);
                    btnNext.setVisibility(View.VISIBLE);
                    tvPageNumber.setVisibility(View.VISIBLE);
                    btnSort.setVisibility(View.VISIBLE);
                    btnSort.setText("Sắp xếp: Sớm đến muộn");
                    currentPage = 1;
                    updatePage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SearchResult", "Error loading posts: " + databaseError.getMessage());
                tvNoResults.setVisibility(View.VISIBLE);
                recyclerViewPosts.setVisibility(View.GONE);
                btnPrevious.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
                tvPageNumber.setVisibility(View.GONE);
                btnSort.setVisibility(View.GONE);
            }
        });
    }

    private void sortPosts() {
        Collections.sort(allPosts, new Comparator<PostFragment.Post>() {
            @Override
            public int compare(PostFragment.Post p1, PostFragment.Post p2) {
                try {
                    Date date1 = p1.postDate != null ? dateFormat.parse(p1.postDate) : new Date(0);
                    Date date2 = p2.postDate != null ? dateFormat.parse(p2.postDate) : new Date(0);
                    return isSortEarliestToLatest ? date1.compareTo(date2) : date2.compareTo(date1);
                } catch (ParseException e) {
                    Log.e("SearchResult", "Error parsing date: " + e.getMessage());
                    return 0;
                }
            }
        });
    }

    private void updatePage() {
        postList.clear();
        int startIndex = (currentPage - 1) * POSTS_PER_PAGE;
        int endIndex = Math.min(startIndex + POSTS_PER_PAGE, allPosts.size());

        for (int i = startIndex; i < endIndex; i++) {
            postList.add(allPosts.get(i));
        }

        postAdapter.notifyDataSetChanged();
        tvPageNumber.setText("Trang " + currentPage);

        btnPrevious.setEnabled(currentPage > 1);
        int totalPages = (int) Math.ceil((double) allPosts.size() / POSTS_PER_PAGE);
        btnNext.setEnabled(currentPage < totalPages);
    }

    private boolean isPriceInRange(String priceStr) {
        try {
            long price = Long.parseLong(priceStr.replaceAll("[^0-9]", ""));
            if (selectedPriceRange == null || selectedPriceRange.equals("Tất cả")) {
                return true;
            }
            switch (selectedPriceRange) {
                case "<=2 triệu":
                    return price <= 2_000_000;
                case "2-4 triệu":
                    return price > 2_000_000 && price <= 4_000_000;
                case "4-6 triệu":
                    return price > 4_000_000 && price <= 6_000_000;
                case "6-8 triệu":
                    return price > 6_000_000 && price <= 8_000_000;
                case ">=10 triệu":
                    return price >= 10_000_000;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            Log.e("SearchResult", "Invalid price format: " + priceStr);
            return false;
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("SearchResult", "Error decoding Base64: " + e.getMessage());
            return null;
        }
    }

    private void navigateToSearchFragment() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("fragment_to_show", "search");
        intent.putExtra("username", currentUsername);
        intent.putExtra("role", userRole);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToSearchFragment();
    }

    private class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
        private List<PostFragment.Post> posts;

        public PostAdapter(List<PostFragment.Post> posts) {
            this.posts = posts;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            PostFragment.Post post = posts.get(position);

            if (holder.tvAddress != null) {
                holder.tvAddress.setText("Địa chỉ: " + (post.address != null ? post.address : "Không có"));
            }
            if (holder.tvContact != null) {
                holder.tvContact.setText("Liên hệ: " + (post.phone != null ? post.phone : "Không có"));
            }
            if (holder.tvPrice != null) {
                String priceText = "Giá thuê: " + (post.price != null ? formatPrice(post.price) : "Không có") + " VNĐ/tháng";
                holder.tvPrice.setText(priceText);
            }
            if (holder.tvPostDate != null) {
                holder.tvPostDate.setText("Ngày đăng: " + (post.postDate != null ? post.postDate : "Không có"));
            }
            if (holder.imgPhoto1 != null) {
                if (post.photo1Uri != null && !post.photo1Uri.isEmpty()) {
                    Bitmap bitmap = decodeBase64ToBitmap(post.photo1Uri);
                    if (bitmap != null) {
                        holder.imgPhoto1.setImageBitmap(bitmap);
                    } else {
                        holder.imgPhoto1.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    holder.imgPhoto1.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                Log.d("SearchResult", "Post clicked: " + post.address);
                Intent intent = new Intent(SearchResultActivity.this, PostDetailActivity.class);
                intent.putExtra("post", post);
                intent.putExtra("username", currentUsername);
                intent.putExtra("role", userRole);
                startActivity(intent);
            });
        }

        private String formatPrice(String price) {
            try {
                long priceValue = Long.parseLong(price.replaceAll("[^0-9]", ""));
                return new DecimalFormat("#,###").format(priceValue);
            } catch (NumberFormatException e) {
                Log.e("SearchResult", "Invalid price format: " + price);
                return price;
            }
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView imgPhoto1;
            TextView tvAddress, tvContact, tvPrice, tvPostDate;

            public PostViewHolder(@NonNull View itemView) {
                super(itemView);
                imgPhoto1 = itemView.findViewById(R.id.imgPhoto1);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvContact = itemView.findViewById(R.id.tvContact);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvPostDate = itemView.findViewById(R.id.tvPostDate);

                if (imgPhoto1 == null) Log.e("SearchResult", "imgPhoto1 is null in item_post.xml");
                if (tvAddress == null) Log.e("SearchResult", "tvAddress is null in item_post.xml");
                if (tvContact == null) Log.e("SearchResult", "tvContact is null in item_post.xml");
                if (tvPrice == null) Log.e("SearchResult", "tvPrice is null in item_post.xml");
                if (tvPostDate == null) Log.e("SearchResult", "tvPostDate is null in item_post.xml");
            }
        }
    }
}