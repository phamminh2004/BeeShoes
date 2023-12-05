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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.ShoeTypeAdapter;
import fpoly.mds.beeshoes.adapter.ShoesAdapter;
import fpoly.mds.beeshoes.databinding.FragmentShoeTypeBinding;
import fpoly.mds.beeshoes.model.Shoe;
import fpoly.mds.beeshoes.model.ShoeType;

public class ShoeTypeFragment extends Fragment implements ShoeTypeAdapter.functionInterface {

    FragmentShoeTypeBinding binding;
    FirebaseFirestore db;
    ShoeTypeAdapter adapter;
    ArrayList<ShoeType> list = new ArrayList<>();
    private ShoeTypeAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShoeTypeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvShoeType.setLayoutManager(manager);
        loadData();
        binding.btnAdd.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new AddUpdateShoeTypeFragment()).addToBackStack(null).commit();
        });
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<ShoeType> templist = new ArrayList<>();
                try {
                    if (s.toString().trim() != "") {
                        for (ShoeType shoeType : list) {
                            if (String.valueOf(shoeType.getName()).contains(String.valueOf(s))) {
                                templist.add(shoeType);
                            }
                        }
                        adapter = new ShoeTypeAdapter(getContext(), templist, functionInterface);
                        binding.rvShoeType.setAdapter(adapter);
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

    private ArrayList<ShoeType> getAllList() {
        ArrayList<ShoeType> listAll = new ArrayList<>();
        db.collection("ShoeType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listAll.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                ShoeType item = new ShoeType(
                                        document.getString("id"),
                                        document.getString("img"),
                                        document.getString("name"));
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
    public void update(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        AddUpdateShoeTypeFragment updateShoeTypeFragment = new AddUpdateShoeTypeFragment();
        updateShoeTypeFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, updateShoeTypeFragment).addToBackStack(null).commit();
    }

    @Override
    public void delete(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có chắc muốn xóa không ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("ShoeType").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Xoá thành công", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
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

    private void loadData() {
        list.clear();
        list = getAllList();
        adapter = new ShoeTypeAdapter(getContext(), list, functionInterface);
        binding.rvShoeType.setAdapter(adapter);
    }
    public void onResume() {
        super.onResume();
        loadData();
    }
}