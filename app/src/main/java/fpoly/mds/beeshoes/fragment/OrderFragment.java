package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.adapter.OrderAdapter;
import fpoly.mds.beeshoes.databinding.FragmentOrderBinding;
import fpoly.mds.beeshoes.model.Cart;

public class OrderFragment extends Fragment {
    private final String REGEX_PHONE_NUMBER = "^[0-9\\-\\+]{9,15}$";
    FragmentOrderBinding binding;
    FirebaseFirestore db;
    OrderAdapter adapter;
    ArrayList<Cart> list;
    int price;
    String nameCustomer, address, phone;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvProduct.setLayoutManager(manager);
        loadData();
        binding.tvSubmit.setOnClickListener(v -> {
        nameCustomer = binding.edtNameCustomer.getText().toString();
        address = binding.edtAddress.getText().toString();
        phone = binding.edtPhone.getText().toString();
            if (TextUtils.isEmpty(nameCustomer) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!phone.matches(REGEX_PHONE_NUMBER)) {
                Toast.makeText(getContext(), "Số điện thoại sai định dạng", Toast.LENGTH_SHORT).show();
            } else {

            }
        });
        return binding.getRoot();
    }

    private void getAllList(CartFragment.FirestoreCallback callback) {
        db.collection("Cart")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        price = 0;
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
        getAllList(new CartFragment.FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Cart> list) {
                adapter = new OrderAdapter(getContext(), list);
                binding.rvProduct.setAdapter(adapter);
                binding.tvPriceShoe.setText("đ" + decimalFormat.format(price));
                int totalPrice = price + 30000;
                binding.tvPriceTotal.setText("đ" + decimalFormat.format(totalPrice));
                binding.tvTotal.setText("đ" + decimalFormat.format(totalPrice));
            }
        });
    }
}