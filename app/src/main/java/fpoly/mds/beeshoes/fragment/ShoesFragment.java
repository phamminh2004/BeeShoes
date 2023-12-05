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

import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.ShoesAdapter;
import fpoly.mds.beeshoes.databinding.FragmentShoesBinding;
import fpoly.mds.beeshoes.model.Shoe;

public class ShoesFragment extends Fragment implements ShoesAdapter.functionInterface {
    FragmentShoesBinding binding;
    FirebaseFirestore db;
    ShoesAdapter adapter;
    ArrayList<Shoe> list = new ArrayList<>();
    private ShoesAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShoesBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvShoes.setLayoutManager(manager);
        loadData();
        binding.btnAdd.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new AddUpdateShoeFragment()).addToBackStack(null).commit();
        });
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Shoe> templist = new ArrayList<>();
                try {
                    if (s.toString().trim() != "") {
                        for (Shoe shoe : list) {
                            if (String.valueOf(shoe.getName()).contains(String.valueOf(s))) {
                                templist.add(shoe);
                            }
                        }
                        adapter = new ShoesAdapter(getContext(), templist, functionInterface);
                        binding.rvShoes.setAdapter(adapter);
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
        AddUpdateShoeFragment updateShoeFragment = new AddUpdateShoeFragment();
        updateShoeFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, updateShoeFragment).addToBackStack(null).commit();
    }

    @Override
    public void delete(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có chắc muốn xóa không ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("Shoes").document(id).delete().addOnSuccessListener(command -> {
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

    private ArrayList<Shoe> getAllList() {
        ArrayList<Shoe> listAll = new ArrayList<>();
        db.collection("Shoes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
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

    private void loadData() {
        list = getAllList();
        adapter = new ShoesAdapter(getContext(), list, functionInterface);
        binding.rvShoes.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}