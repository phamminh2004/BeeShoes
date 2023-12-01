package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    String userId, role;
    FirebaseFirestore db;
    int revenue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userId = currentUser.getUid();
        loadData();
        loadRevenue();
        binding.tvShoeType.setOnClickListener(v -> {
            getActivity().setTitle("Loại giày");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoeTypeFragment()).commit();
        });
        binding.tvShoes.setOnClickListener(v -> {
            getActivity().setTitle("Giày");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoesFragment()).commit();
        });
        binding.tvEmployee.setOnClickListener(v -> {
            getActivity().setTitle("Nhân viên");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new EmployeeFragment()).commit();
        });
        binding.tvWork.setOnClickListener(v -> {
            getActivity().setTitle("Công việc");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new WorkFragment()).commit();
        });
        binding.tvBill.setOnClickListener(v -> {
            getActivity().setTitle("Hóa đơn");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new BillFragment()).commit();
        });
        binding.tvStatistic.setOnClickListener(v -> {
            getActivity().setTitle("Thống kê doanh thu");
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new RevenueStatisticsFragment()).commit();
        });
        return binding.getRoot();
    }

    private void loadData() {
        db.collection("User").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        role = documentSnapshot.getString("role");
                        binding.tvRole.setText("Hello " + role);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "onFailure" + e);
                });
    }

    private void loadRevenue() {
        db.collection("Bill").whereEqualTo("status",1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    int quantity = querySnapshot.size();
                    binding.tvQuantity.setText(quantity + "");
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        revenue += documentSnapshot.getLong("price").intValue();
                    }
                    binding.tvRevenue.setText("đ"+decimalFormat.format(revenue));
                } else {
                    Log.e("FirebaseError", "Lỗi khi truy vấn dữ liệu từ Firebase", task.getException());
                }
            }
        });
    }
}