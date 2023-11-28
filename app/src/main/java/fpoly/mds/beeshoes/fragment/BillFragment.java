package fpoly.mds.beeshoes.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.BillAdapter;
import fpoly.mds.beeshoes.adapter.WorkAdapter;
import fpoly.mds.beeshoes.databinding.FragmentBillBinding;
import fpoly.mds.beeshoes.model.Bill;
import fpoly.mds.beeshoes.model.Work;

public class BillFragment extends Fragment implements BillAdapter.functionInterface {
    FragmentBillBinding binding;
    FirebaseFirestore db;
    BillAdapter adapter;
    ArrayList<Bill> list = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private BillAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBillBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvBill.setLayoutManager(manager);
        loadData();
        binding.btnAdd.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new AddUpdateBillFragment()).addToBackStack(null).commit();
        });
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
//                            if (String.valueOf(bill.getNameShoe()).contains(String.valueOf(s))) {
//                                templist.add(bill);
//                            }
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

    @Override
    public void update(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        AddUpdateBillFragment updateBillFragment = new AddUpdateBillFragment();
        updateBillFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, updateBillFragment).addToBackStack(null).commit();
    }

    @Override
    public void delete(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có chắc muốn xóa không ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("Bill").document(id).delete().addOnSuccessListener(command -> {
                    Toast.makeText(getContext(), "Xoá thành công", Toast.LENGTH_SHORT).show();
                    loadData();
                });
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        builder.show();
    }

    private ArrayList<Bill> getAllList() {
        ArrayList<Bill> listAll = new ArrayList<>();
        db.collection("Bill")
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
                                    Bill item = new Bill(
                                            document.getId(),
                                            document.getLong("price").intValue(),
                                            document.getString("nameCustomer"),
                                            document.getString("phone"),
                                            document.getString("address"),
                                            sdf.parse(document.getString("date")),
                                            document.getLong("status").intValue()
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
        adapter = new BillAdapter(getContext(), list, functionInterface);
        binding.rvBill.setAdapter(adapter);
    }
}