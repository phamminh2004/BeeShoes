package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentProductInfoBinding;
import fpoly.mds.beeshoes.model.Cart;
import fpoly.mds.beeshoes.model.Shoe;

public class ProductInfoFragment extends Fragment {
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    FragmentProductInfoBinding binding;
    FirebaseFirestore db;
    Bundle bundle;
    String id, name, color;
    int price, quantity, size;
    ArrayList<Shoe> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductInfoBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        bundle = getArguments();
        quantity = 1;
        if (bundle != null) {
            id = bundle.getString("id");
            getID(id, new ShoeCallback() {
                @Override
                public void onShoeLoaded(Shoe shoe) {
                    String img = shoe.getImg();
                    try {
                        Picasso.get().load(img).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera)
                                .into(binding.ivImg);
                    } catch (Exception e) {
                        Log.e("PicassoError", "Error loading image: " + e.getMessage());
                    }
                    binding.tvName.setText(shoe.getName());
                    binding.tvPrice.setText(shoe.getPrice() + "");
                    binding.tvDescribe.setText(shoe.getDescribe());
                    binding.tvPrice.setText(decimalFormat.format((long) quantity * shoe.getPrice()) + "VND");
                    binding.tvColor.setText(shoe.getColor());
                    binding.tvSize.setText(shoe.getSize() + "");
                    binding.ivMinus.setOnClickListener(v -> {
                        if (quantity > 1) {
                            quantity--;
                            price = quantity * shoe.getPrice();
                            binding.tvQuantity.setText(String.valueOf(quantity));
                            binding.tvPrice.setText(decimalFormat.format(price) + "VND");
                        }
                    });
                    binding.ivPlus.setOnClickListener(v -> {
                        quantity++;
                        price = quantity * shoe.getPrice();
                        binding.tvQuantity.setText(String.valueOf(quantity));
                        binding.tvPrice.setText(decimalFormat.format(price) + "VND");
                    });
                    price = Integer.parseInt(binding.tvQuantity.getText().toString()) * shoe.getPrice();
                    binding.btnAdd.setOnClickListener(v -> {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = auth.getCurrentUser();
                        String userId = currentUser.getUid();
                        name = shoe.getName();
                        color = shoe.getColor();
                        size = shoe.getSize();
                        Cart cart = new Cart(id, userId, img, name, price, color, size, quantity);
                        HashMap<String, Object> hashMap = cart.convertHashMap();
                        db.collection("Cart").document(id).set(hashMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new HomeCustomerFragment()).commit();
                                });
                    });
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
        return binding.getRoot();
    }

    private void getID(String id, ShoeCallback callback) {
        DocumentReference docRef = db.collection("Shoes").document(id);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Shoe shoe = documentSnapshot.toObject(Shoe.class);
                            if (shoe != null) {
                                callback.onShoeLoaded(shoe);
                            } else {
                                Log.d("Firebase", "Không thể chuyển đổi thành đối tượng ShoeType");
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

    private interface ShoeCallback {
        void onShoeLoaded(Shoe shoe);

        void onFailure(Exception e);
    }
}