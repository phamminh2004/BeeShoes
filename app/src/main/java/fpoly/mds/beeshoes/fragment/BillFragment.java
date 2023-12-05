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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.BillAdapter;
import fpoly.mds.beeshoes.adapter.EmployeeAdapter;
import fpoly.mds.beeshoes.databinding.FragmentBillBinding;
import fpoly.mds.beeshoes.model.Bill;
import fpoly.mds.beeshoes.model.Employee;

public class BillFragment extends Fragment implements BillAdapter.functionInterface {

    FragmentBillBinding binding;
    FirebaseFirestore db;
    BillAdapter adapter;
    ArrayList<Bill> list;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    String userId, role;
    private BillAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBillBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        list = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvBill.setLayoutManager(manager);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        userId = currentUser.getUid();
        loadData();
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Bill> templist = new ArrayList<>();
                try {
                    if (s.toString().trim() != "") {
                        for (Bill bill : list) {
                            if (String.valueOf(bill.getId()).contains(String.valueOf(s))) {
                                templist.add(bill);
                            }
                        }
                        adapter = new BillAdapter(getContext(), templist, functionInterface);
                        binding.rvBill.setAdapter(adapter);
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

    private void getList(FirestoreCallback callback) {
        ArrayList<Bill> list = new ArrayList<>();
        CollectionReference collectionReference = db.collection("Bill");
        collectionReference.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Bill bill = new Bill(
                                            document.getString("id"),
                                            document.getString("userId"),
                                            document.getString("nameCustomer"),
                                            document.getString("address"),
                                            document.getString("phone"),
                                            document.getLong("price").intValue(),
                                            sdf.parse(document.getString("date")),
                                            document.getLong("status").intValue()
                                    );
                                    list.add(bill);
                                } catch (Exception e) {

                                }
                            }
                            callback.onCallback(list);
                        } else {
                            Log.w("TAG", "Lỗi khi truy vấn dữ liệu", task.getException());
                        }
                    }
                });
    }

    private void getAllList(FirestoreCallback callback) {
        ArrayList<Bill> list = new ArrayList<>();
        db.collection("Bill")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Bill bill = new Bill(
                                            document.getString("id"),
                                            document.getString("userId"),
                                            document.getString("nameCustomer"),
                                            document.getString("address"),
                                            document.getString("phone"),
                                            document.getLong("price").intValue(),
                                            sdf.parse(document.getString("date")),
                                            document.getLong("status").intValue()
                                    );
                                    list.add(bill);
                                } catch (Exception e) {

                                }
                            }
                            callback.onCallback(list);
                        } else {
                            Log.w("TAG", "Lỗi khi truy vấn dữ liệu", task.getException());
                        }
                    }
                });
    }

    private void loadData() {
        db.collection("User").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        role = documentSnapshot.getString("role");
                        if (role.equals("manager")||role.equals("employee")) {
                            getAllList(new FirestoreCallback() {
                                @Override
                                public void onCallback(ArrayList<Bill> list) {
                                    adapter = new BillAdapter(getContext(), list, functionInterface);
                                    binding.rvBill.setAdapter(adapter);
                                }
                            });
                        } else {
                            getList(new FirestoreCallback() {
                                @Override
                                public void onCallback(ArrayList<Bill> list) {
                                    adapter = new BillAdapter(getContext(), list, functionInterface);
                                    binding.rvBill.setAdapter(adapter);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "onFailure" + e);
                });

    }

    @Override
    public void click(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        BillInfoFragment billInfoFragment = new BillInfoFragment();
        billInfoFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, billInfoFragment).addToBackStack(null).commit();
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Bill> list);
    }
}