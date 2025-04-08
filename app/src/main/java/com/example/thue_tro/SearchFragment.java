package com.example.thue_tro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class SearchFragment extends Fragment {

    private Spinner spinnerCity, spinnerDistrict, spinnerRoomType;
    private EditText edtAddress;
    private Button btnApply, btnPriceAll, btnPriceUnder2M, btnPrice2Mto4M, btnPrice4Mto6M, btnPrice6Mto8M, btnPriceAbove10M;
    private TextView tvCityError, tvDistrictError, tvRoomTypeError, tvPriceError;
    private String selectedCity, selectedDistrict, selectedRoomType, selectedPriceRange = null;
    private Button selectedPriceButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ
        spinnerCity = view.findViewById(R.id.spinnerCity);
        spinnerDistrict = view.findViewById(R.id.spinnerDistrict);
        spinnerRoomType = view.findViewById(R.id.spinnerRoomType);
        edtAddress = view.findViewById(R.id.edtAddress);
        btnApply = view.findViewById(R.id.btnApply);
        btnPriceAll = view.findViewById(R.id.btnPriceAll);
        btnPriceUnder2M = view.findViewById(R.id.btnPriceUnder2M);
        btnPrice2Mto4M = view.findViewById(R.id.btnPrice2Mto4M);
        btnPrice4Mto6M = view.findViewById(R.id.btnPrice4Mto6M);
        btnPrice6Mto8M = view.findViewById(R.id.btnPrice6Mto8M);
        btnPriceAbove10M = view.findViewById(R.id.btnPriceAbove10M);
        tvCityError = view.findViewById(R.id.tvCityError);
        tvDistrictError = view.findViewById(R.id.tvDistrictError);
        tvRoomTypeError = view.findViewById(R.id.tvRoomTypeError);
        tvPriceError = view.findViewById(R.id.tvPriceError);

        // Thiết lập Spinner cho Thành phố
        String[] cities = {"Chọn thành phố", "Hà Nội"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, cities) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Vô hiệu hóa mục hint
            }
        };
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
        spinnerCity.setSelection(0);

        // Thiết lập Spinner cho Quận
        String[] districts = {"Chọn quận", "Ba Đình", "Hoàn Kiếm", "Hai Bà Trưng", "Đống Đa", "Tây Hồ", "Cầu Giấy", "Thanh Xuân",
                "Hoàng Mai", "Long Biên", "Nam Từ Liêm", "Bắc Từ Liêm", "Hà Đông", "Sơn Tây", "Ba Vì",
                "Chương Mỹ", "Đan Phượng", "Đông Anh", "Gia Lâm", "Hoài Đức", "Mê Linh", "Mỹ Đức",
                "Phú Xuyên", "Phúc Thọ", "Quốc Oai", "Sóc Sơn", "Thạch Thất", "Thanh Oai", "Thanh Trì",
                "Thường Tín", "Ứng Hòa"};
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, districts) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Vô hiệu hóa mục hint
            }
        };
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);
        spinnerDistrict.setSelection(0);

        // Thiết lập Spinner cho Loại phòng
        String[] roomTypes = {"Chọn loại phòng", "Phòng trọ", "Chung cư", "Nhà nguyên căn"};
        ArrayAdapter<String> roomTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, roomTypes) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Vô hiệu hóa mục hint
            }
        };
        roomTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomType.setAdapter(roomTypeAdapter);
        spinnerRoomType.setSelection(0);

        // Xử lý sự kiện chọn Thành phố
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCity = null;
                } else {
                    selectedCity = cities[position];
                    tvCityError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCity = null;
                tvCityError.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý sự kiện chọn Quận
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDistrict = null;
                } else {
                    selectedDistrict = districts[position];
                    tvDistrictError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = null;
                tvDistrictError.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý sự kiện chọn Loại phòng
        spinnerRoomType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedRoomType = null;
                } else {
                    selectedRoomType = roomTypes[position];
                    tvRoomTypeError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoomType = null;
                tvRoomTypeError.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý các nút giá
        View.OnClickListener priceClickListener = v -> {
            Button clickedButton = (Button) v;
            resetPriceButtons();
            clickedButton.setSelected(true);
            selectedPriceButton = clickedButton;
            selectedPriceRange = clickedButton.getText().toString();
            tvPriceError.setVisibility(View.GONE);
        };

        btnPriceAll.setOnClickListener(priceClickListener);
        btnPriceUnder2M.setOnClickListener(priceClickListener);
        btnPrice2Mto4M.setOnClickListener(priceClickListener);
        btnPrice4Mto6M.setOnClickListener(priceClickListener);
        btnPrice6Mto8M.setOnClickListener(priceClickListener);
        btnPriceAbove10M.setOnClickListener(priceClickListener);

        // Xử lý nút Áp dụng
        btnApply.setOnClickListener(v -> {
            boolean isValid = true;

            if (selectedCity == null) {
                tvCityError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                tvCityError.setVisibility(View.GONE);
            }

            if (selectedDistrict == null) {
                tvDistrictError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                tvDistrictError.setVisibility(View.GONE);
            }

            if (selectedRoomType == null) {
                tvRoomTypeError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                tvRoomTypeError.setVisibility(View.GONE);
            }

            if (selectedPriceRange == null) {
                tvPriceError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                tvPriceError.setVisibility(View.GONE);
            }

            if (isValid) {
                String address = edtAddress.getText().toString().trim();
                Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                intent.putExtra("city", selectedCity);
                intent.putExtra("district", selectedDistrict);
                intent.putExtra("address", address);
                intent.putExtra("roomType", selectedRoomType);
                intent.putExtra("priceRange", selectedPriceRange);
                Bundle bundle = getArguments();
                if (bundle != null) {
                    intent.putExtra("username", bundle.getString("username"));
                    intent.putExtra("role", bundle.getString("role"));
                }
                startActivity(intent);
            }
        });
    }

    // Đặt lại trạng thái các nút giá
    private void resetPriceButtons() {
        btnPriceAll.setSelected(false);
        btnPriceUnder2M.setSelected(false);
        btnPrice2Mto4M.setSelected(false);
        btnPrice4Mto6M.setSelected(false);
        btnPrice6Mto8M.setSelected(false);
        btnPriceAbove10M.setSelected(false);
    }
}