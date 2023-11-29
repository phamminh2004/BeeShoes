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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.HomeCustomerAdapter;
import fpoly.mds.beeshoes.databinding.FragmentHomeCustomerBinding;
import fpoly.mds.beeshoes.model.Shoe;

public class HomeCustomerFragment extends Fragment implements HomeCustomerAdapter.functionInterface {
    FragmentHomeCustomerBinding binding;
    FirebaseFirestore db;
    HomeCustomerAdapter adapter;
    ArrayList<Shoe> list = new ArrayList<>();
    private HomeCustomerAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeCustomerBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rvShoe.setLayoutManager(manager);
        loadData();
        binding.btnCart.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new CartFragment()).addToBackStack(null).commit();
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
                        adapter = new HomeCustomerAdapter(getContext(), templist, functionInterface);
                        binding.rvShoe.setAdapter(adapter);
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
        adapter = new HomeCustomerAdapter(getContext(), list, functionInterface);
        binding.rvShoe.setAdapter(adapter);
    }

    @Override
    public void click(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        ProductInfoFragment productInfoFragment = new ProductInfoFragment();
        productInfoFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, productInfoFragment).addToBackStack(null).commit();
    }
}