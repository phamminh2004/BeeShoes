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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.OrderAdapter;
import fpoly.mds.beeshoes.databinding.FragmentOrderBinding;
import fpoly.mds.beeshoes.model.Bill;
import fpoly.mds.beeshoes.model.Cart;
import fpoly.mds.beeshoes.model.Customer;


public class OrderFragment extends Fragment {
    private final String REGEX_PHONE_NUMBER = "^[0-9\\-\\+]{9,15}$";
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    FragmentOrderBinding binding;
    FirebaseFirestore db;
    OrderAdapter adapter;
    ArrayList<Cart> list;
    int price, totalPrice;
    String nameCustomer, address, phone, id, userId;
    Date currentDate;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        currentDate = new Date();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        userId = currentUser.getUid();
        id = UUID.randomUUID().toString();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvProduct.setLayoutManager(manager);
        binding.tvDate.setText("Nhận hàng vào " + sdf.format(new Date()));
        loadData();
        binding.tvSubmit.setOnClickListener(v -> {
            nameCustomer = binding.edtNameCustomer.getText().toString();
            address = binding.edtAddress.getText().toString();
            phone = binding.edtPhone.getText().toString();
            totalPrice = price + 30000;
            if (TextUtils.isEmpty(nameCustomer) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!phone.matches(REGEX_PHONE_NUMBER)) {
                Toast.makeText(getContext(), "Số điện thoại sai định dạng", Toast.LENGTH_SHORT).show();
            } else {
                saveData();
            }
        });
        return binding.getRoot();
    }

    private void saveData() {
        try {
            Bill bill = new Bill(id, userId, nameCustomer, address, phone, totalPrice, currentDate, 0);
            HashMap<String, Object> hashMap = bill.convertHashMap();
            db.collection("Bill").document(id).set(hashMap).
                    addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new BillFragment()).addToBackStack(null).commit();
                        }
                    });
            uploadBillInfo();
            upLoadCustomer();
            deleteDataCart();
        } catch (Exception e) {
            Log.e("loi firebase", e.getMessage());
        }
    }

    private void upLoadCustomer() {
        Customer customer = new Customer(userId, nameCustomer, phone, address);
        HashMap<String, Object> hashMap = customer.converHashMap();
        db.collection("Customer").document(userId).set(hashMap).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new BillFragment()).addToBackStack(null).commit();
                    }
                });
    }

    private void deleteDataCart() {
        CollectionReference collectionReference = db.collection("Cart");
        collectionReference.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().delete();
                            }
                            Log.d("TAG", "Xóa dữ liệu thành công.");
                        } else {
                            Log.w("TAG", "Lỗi khi truy vấn dữ liệu", task.getException());
                        }
                    }
                });
    }


    private void getAllList(FirestoreCallback callback) {
        ArrayList<Cart> list = new ArrayList<>();
        CollectionReference collectionReference = db.collection("Cart");
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
                });
    }

    private void uploadBillInfo() {
        ArrayList<Cart> list = new ArrayList<>();
        CollectionReference collectionReference = db.collection("Cart");
        collectionReference.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();
                        price = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String uid = UUID.randomUUID().toString();
                                Cart cart = document.toObject(Cart.class);
                                cart.setId(id);
                                HashMap<String, Object> hashMap = cart.convertHashMap();
                                db.collection("BillInfo").document(uid).set(hashMap).
                                        addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Log.w("TAG", "Lỗi khi truy vấn dữ liệu", task.getException());
                        }
                    }
                });
    }

    private void loadData() {
        getAllList(new FirestoreCallback() {
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

    public interface FirestoreCallback {
        void onCallback(ArrayList<Cart> list);
    }
}