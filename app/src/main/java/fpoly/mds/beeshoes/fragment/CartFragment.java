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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
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
        binding.btnOrder.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new OrderFragment()).addToBackStack(null).commit();
        });
        return binding.getRoot();
    }

    private void getAllList(FirestoreCallback callback) {
        ArrayList<Cart> list = new ArrayList<>();
        CollectionReference collectionReference = db.collection("Cart");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        collectionReference.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        price = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Cart cart = document.toObject(Cart.class);
                                price += cart.getPrice();
                                list.add(cart);
                                Log.d("TAG", document.getId() + " => " + document.getData());
                            }
                            callback.onCallback(list);
                        } else {
                            Log.w("TAG", "Lỗi khi truy vấn dữ liệu", task.getException());
                        }
                    }
                }).addOnFailureListener(command -> {
                    price = 0;
                });
    }

    private void loadData() {
        getAllList(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Cart> list) {
                adapter = new CartAdapter(getContext(), list, functionInterface);
                binding.rvProduct.setAdapter(adapter);
                binding.tvPrice.setText("đ" + decimalFormat.format(price));
            }
        });
    }

    @Override
    public void updateData() {
        getAllList(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Cart> list) {
                adapter = new CartAdapter(getContext(), list, functionInterface);
                binding.rvProduct.setAdapter(adapter);
                binding.tvPrice.setText("đ" + decimalFormat.format(price));
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