package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.HomeCustomerAdapter;
import fpoly.mds.beeshoes.databinding.FragmentHomeBinding;
import fpoly.mds.beeshoes.model.Shoe;

public class HomeFragment extends Fragment  implements HomeCustomerAdapter.functionInterface{
    FragmentHomeBinding binding;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    String userId, role;
    FirebaseFirestore db;
    int revenue;
    ArrayList<Shoe> list = new ArrayList<>();
    HomeCustomerAdapter adapter;
    private HomeCustomerAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        userId = currentUser.getUid();
        functionInterface = this;
        loadRole();
        loadRevenue();
        loadRV();

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

    private void loadRole() {
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
    private void loadRV(){
        GridLayoutManager manager = new GridLayoutManager(getContext(),2);
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvShoe.setLayoutManager(manager);
        list = getAllList();
        adapter = new HomeCustomerAdapter(getContext(), list, functionInterface);
        binding.rvShoe.setAdapter(adapter);
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
                    revenue = 0;
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
    private ArrayList<Shoe> getAllList() {
        ArrayList<Shoe> listAll = new ArrayList<>();
        db.collection("Shoes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                        listAll.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                Shoe item = new Shoe(
                                        document.getString("id"),
                                        document.getString("img"),
                                        document.getString("name"),
                                        document.getString("shoeType"),
                                        document.getLong("price").intValue(),
                                        document.getString("color"),
                                        document.getLong("size").intValue(),
                                        document.getString("describe"));
                                listAll.add(item);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
        return listAll;
    }
    @Override
    public void click(String id) {

    }
}