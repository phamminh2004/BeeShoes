package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
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
import fpoly.mds.beeshoes.adapter.HomeAdapter;
import fpoly.mds.beeshoes.databinding.FragmentHomeCustomerBinding;
import fpoly.mds.beeshoes.model.Shoe;

public class HomeCustomerFragment extends Fragment implements HomeAdapter.functionInterface {
    FragmentHomeCustomerBinding binding;
    FirebaseFirestore db;
    HomeAdapter adapter;
    ArrayList<Shoe> list = new ArrayList<>();
    private HomeAdapter.functionInterface functionInterface;

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
        binding.rvShoe.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ProductInfoFragment()).addToBackStack(null).commit();
        });
        binding.btnCart.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new CartFragment()).addToBackStack(null).commit();
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
        adapter = new HomeAdapter(getContext(), list, functionInterface);
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