package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import fpoly.mds.beeshoes.adapter.CustomerAdapter;
import fpoly.mds.beeshoes.databinding.FragmentCustomerBinding;
import fpoly.mds.beeshoes.model.Customer;

public class CustomerFragment extends Fragment {
    FragmentCustomerBinding binding;
    ArrayList<Customer> list = new ArrayList<>();
    CustomerAdapter adapter;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomerBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvCustomer.setLayoutManager(manager);
        loadData();
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Customer> templist = new ArrayList<>();
                try {
                    if (s.toString().trim() != "") {
                        for (Customer customer : list) {
                            if (String.valueOf(customer.getName()).contains(String.valueOf(s))) {
                                templist.add(customer);
                            }
                        }
                        adapter = new CustomerAdapter(getContext(), templist);
                        binding.rvCustomer.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Lỗi tìm kiếm" + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private ArrayList<Customer> getAllList() {
        ArrayList<Customer> listAll = new ArrayList<>();
        db.collection("Customer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listAll.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                try {
                                    Customer item = new Customer(
                                            document.getId(),
                                            document.getString("name"),
                                            document.getString("phone"),
                                            document.getString("address")
                                    );
                                    listAll.add(item);
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {

                                }
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
        return listAll;
    }

    private void loadData() {
        list = getAllList();
        adapter = new CustomerAdapter(getContext(), list);
        binding.rvCustomer.setAdapter(adapter);
    }
    public void onResume() {
        super.onResume();
        loadData();
    }
}