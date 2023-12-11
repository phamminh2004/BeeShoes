package fpoly.mds.beeshoes.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.ItemCartBinding;
import fpoly.mds.beeshoes.model.Cart;
import fpoly.mds.beeshoes.model.Shoe;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Cart> list;
    private final functionInterface functionInterface;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    FirebaseFirestore db;
    int price;
    int quantity;

    public CartAdapter(Context context, ArrayList<Cart> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface = functionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cart cart = list.get(position);
        String id = cart.getId();
        holder.binding.tvName.setText(cart.getName());
        holder.binding.tvPrice.setText("đ" + decimalFormat.format(cart.getPrice()));
        holder.binding.tvColor.setText(cart.getColor());
        holder.binding.tvSize.setText("Size: " + cart.getSize());
        holder.binding.tvQuantity.setText(cart.getQuantity() + "");
        String img = cart.getImg();
        try {
            Picasso.get().load(img).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera)
                    .into(holder.binding.ivImg);
        } catch (Exception e) {
            Log.e("PicassoError", "Error loading image: " + e.getMessage());
        }

        holder.binding.ivMinus.setOnClickListener(v -> {
            quantity = cart.getQuantity();
            if (quantity > 1) {
                getName(cart.getName(), new ShoeCallback() {
                    @Override
                    public void onShoeLoaded(Shoe shoe) {
                        quantity--;
                        price = shoe.getPrice() * quantity;
                        holder.binding.tvQuantity.setText(quantity + "");
                        holder.binding.tvPrice.setText("đ"+decimalFormat.format(price));
                        functionInterface.updateData();
                        updateFirebase(id);
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }
        });
        holder.binding.ivPlus.setOnClickListener(v -> {
            quantity = cart.getQuantity();
            getName(cart.getName(), new ShoeCallback() {
                @Override
                public void onShoeLoaded(Shoe shoe) {
                    quantity = cart.getQuantity();
                    quantity++;
                    price = shoe.getPrice() * quantity;
                    holder.binding.tvQuantity.setText(quantity + "");
                    holder.binding.tvPrice.setText("đ"+decimalFormat.format(price));
                    functionInterface.updateData();
                    updateFirebase(id);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        });
        holder.binding.btnDelete.setOnClickListener(v -> {
            db = FirebaseFirestore.getInstance();
            list.remove(position);
            notifyDataSetChanged();
            db.collection("Cart").document(cart.getId()).delete().addOnSuccessListener(command -> {
                Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
            });
            functionInterface.updateData();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void updateFirebase(String id) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("quantity", quantity);
        hashMap.put("price", price);
        db.collection("Cart").document(id).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
    }


    private void getName(String name, ShoeCallback callback) {
        db = FirebaseFirestore.getInstance();
        Query query = db.collection("Shoes").whereEqualTo("name", name);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        if (documentSnapshot.exists()) {
                            Shoe shoe = documentSnapshot.toObject(Shoe.class);
                            if (shoe != null) {
                                callback.onShoeLoaded(shoe);
                            }
                        } else {
                            // Tài liệu không tồn tại
                        }
                    } else {
                        // Không có tài liệu nào thỏa mãn điều kiện
                    }
                } else {
                    Log.e("FirestoreError", "Error getting documents", task.getException());
                }
            }
        });
    }

    private interface ShoeCallback {
        void onShoeLoaded(Shoe shoe);

        void onFailure(Exception e);
    }

    public interface functionInterface {
        void updateData();
//        void delete(String id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;

        public ViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
