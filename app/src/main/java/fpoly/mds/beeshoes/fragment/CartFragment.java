package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.adapter.CartAdapter;
import fpoly.mds.beeshoes.databinding.FragmentCartBinding;
import fpoly.mds.beeshoes.model.Cart;
import fpoly.mds.beeshoes.model.Shoe;

public class CartFragment extends Fragment implements CartAdapter.functionInterface {
    FragmentCartBinding binding;
    FirebaseFirestore db;
    CartAdapter adapter;
    int price;
    ArrayList<Cart> list;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private CartAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        functionInterface = this;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rvProduct.setLayoutManager(manager);
        loadData();
        return binding.getRoot();
    }

    private void getAllList(FirestoreCallback callback) {
        ArrayList<Cart> list = new ArrayList<>();
        list.clear();
        db.collection("Cart")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                Cart item = new Cart(
                                        document.getString("id"),
                                        document.getString("img"),
                                        document.getString("name"),
                                        document.getLong("price").intValue(),
                                        document.getString("color"),
                                        document.getLong("size").intValue(),
                                        document.getLong("quantity").intValue());
                                price += item.getPrice();
                                list.add(item);
                            }
                            callback.onCallback(list);
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
    }

    private void loadData() {
        getAllList(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Cart> list) {
                adapter = new CartAdapter(getContext(), list, functionInterface);
                binding.rvProduct.setAdapter(adapter);
                binding.tvPrice.setText(decimalFormat.format(price) + "VND");
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateData() {
        price = 0;
        Log.e("size", list.size() + "");
        getAllList(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Cart> list) {
                adapter = new CartAdapter(getContext(), list, functionInterface);
                binding.rvProduct.setAdapter(adapter);
                binding.tvPrice.setText(decimalFormat.format(price) + "VND");
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Cart> list);
    }

    private interface ShoeCallback {
        void onShoeLoaded(Shoe shoe);

        void onFailure(Exception e);
    }
}