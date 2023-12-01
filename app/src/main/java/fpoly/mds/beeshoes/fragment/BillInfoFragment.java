package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.BillInfoAdapter;
import fpoly.mds.beeshoes.databinding.FragmentBillInfoBinding;
import fpoly.mds.beeshoes.model.Bill;
import fpoly.mds.beeshoes.model.Cart;

public class BillInfoFragment extends Fragment {
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    FragmentBillInfoBinding binding;
    FirebaseFirestore db;
    Bundle bundle;
    String id;
    BillInfoAdapter adapter;
    ArrayList<Cart> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBillInfoBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        bundle = getArguments();
        if (bundle != null) {
            id = bundle.getString("id");
            getID(id, new BillCallback() {
                @Override
                public void onBillLoaded(Bill bill) {
                    binding.tvId.setText("ID: " + bill.getId());
                    binding.tvNameCustomer.setText("Họ tên: " + bill.getNameCustomer());
                    binding.tvPrice.setText("Giá: đ" + decimalFormat.format(bill.getPrice()));
                    binding.tvPhone.setText("SĐT: " + bill.getPhone());
                    binding.tvAddress.setText("Địa chỉ: " + bill.getAddress());
                    binding.tvDate.setText("Ngày đặt hàng: " + sdf.format(bill.getDate()));
                    if (bill.getStatus() == 0) {
                        binding.tvStatus.setText("Chưa thanh toán");
                        binding.tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    } else {
                        binding.tvStatus.setText("Đã thanh toán");
                        binding.tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                    }
                    LinearLayoutManager manager = new LinearLayoutManager(getContext());
                    manager.setOrientation(LinearLayoutManager.VERTICAL);
                    binding.rvShoes.setLayoutManager(manager);
                    loadData();
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
        return binding.getRoot();
    }

    private void getID(String id, BillCallback callback) {
        DocumentReference docRef = db.collection("Bill").document(id);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            try {
                                Bill bill = new Bill(
                                        documentSnapshot.getString("id"),
                                        documentSnapshot.getString("userId"),
                                        documentSnapshot.getString("nameCustomer"),
                                        documentSnapshot.getString("address"),
                                        documentSnapshot.getString("phone"),
                                        documentSnapshot.getLong("price").intValue(),
                                        sdf.parse(documentSnapshot.getString("date")),
                                        documentSnapshot.getLong("status").intValue()
                                );
                                callback.onBillLoaded(bill);
                            } catch (Exception e) {

                            }
                        } else {
                            Log.d("Firebase", "Tài liệu không tồn tại");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    private void getAllList(FirestoreCallback callback) {
        ArrayList<Cart> list = new ArrayList<>();
        CollectionReference collectionReference = db.collection("BillInfo");

        collectionReference.whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Cart cart = document.toObject(Cart.class);
                                list.add(cart);
                            }
                            callback.onCallback(list);

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
                adapter = new BillInfoAdapter(getContext(), list);
                binding.rvShoes.setAdapter(adapter);
            }
        });
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Cart> list);
    }

    private interface BillCallback {
        void onBillLoaded(Bill bill);

        void onFailure(Exception e);
    }

}