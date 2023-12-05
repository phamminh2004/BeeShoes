package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentRevenueStatisticsBinding;

public class RevenueStatisticsFragment extends Fragment {

    FragmentRevenueStatisticsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRevenueStatisticsBinding.inflate(inflater, container, false);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Lấy ngày hiện tại để xác định tháng hiện tại
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        FirebaseFirestore.getInstance().collection("Bill").whereEqualTo("status",1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BarEntry> entries = new ArrayList<>();
                            List<String> labels = new ArrayList<>();

                            for (int i = 0; i < 12; i++) {
                                // Thiết lập tháng để truy vấn
                                String month = String.format("%02d/%04d", i + 1, calendar.get(Calendar.YEAR));

                                // Tìm tất cả các hóa đơn trong tháng
                                long revenue = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    try {
                                        Date date = dateFormat.parse(document.getString("date"));

                                        // Sử dụng Calendar để trích xuất tháng và năm
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(date);

                                        int month1 = calendar.get(Calendar.MONTH) + 1; // Tháng bắt đầu từ 0
                                        int year = calendar.get(Calendar.YEAR);

                                        String date1 = month1 + "/" + year;
                                        if (date1 != null && date1.startsWith(month)) {
                                            revenue += document.getLong("price");
                                        }
                                    } catch (Exception e) {
                                    }
                                }

                                // Thêm giá trị vào danh sách BarEntry
                                entries.add(new BarEntry(i, revenue));

                                // Lấy tháng để làm nhãn trục x
                                labels.add(month);
                            }

                            // Tạo và hiển thị biểu đồ
                            showBarChart(entries, labels);
                        } else {
                            Log.e("FirestoreError", "Lỗi khi truy vấn dữ liệu từ Firestore", task.getException());
                        }
                    }
                });
        return binding.getRoot();
    }

    private void showBarChart(List<BarEntry> entries, List<String> labels) {
        BarDataSet dataSet = new BarDataSet(entries, "Doanh Thu");

        // Thiết lập màu sắc của thanh trong biểu đồ
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.red));

        BarData barData = new BarData(dataSet);

        // Thiết lập nhãn trục x
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        // Tắt mô tả và chú thích
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);

        // Hiển thị giá trị trên thanh
        barData.setBarWidth(0.9f);
        barData.setValueTextSize(10f);

        // Hiển thị biểu đồ
        binding.barChart.setData(barData);
        binding.barChart.setFitBars(true);
        binding.barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
