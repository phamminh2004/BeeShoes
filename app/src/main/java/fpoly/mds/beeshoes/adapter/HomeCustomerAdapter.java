package fpoly.mds.beeshoes.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.ItemProductBinding;
import fpoly.mds.beeshoes.model.Cart;
import fpoly.mds.beeshoes.model.Shoe;

public class HomeCustomerAdapter extends RecyclerView.Adapter<HomeCustomerAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Shoe> list;
    private final functionInterface functionInterface;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public HomeCustomerAdapter(Context context, ArrayList<Shoe> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface = functionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shoe item = list.get(position);
        holder.binding.tvName.setText(item.getName());
        holder.binding.tvPrice.setText("đ" + decimalFormat.format(item.getPrice()));
        String img = item.getImg();
        try {
            Picasso.get().load(img).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera)
                    .into(holder.binding.ivImg);
        } catch (Exception e) {
            Log.e("PicassoError", "Error loading image: " + e.getMessage());
        }
        holder.itemView.setOnClickListener(v -> {
            functionInterface.click(item.getId());
        });
        holder.binding.btnAdd.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String id = item.getId();
            String name = item.getName();
            String color = item.getColor();
            int quantity = 1;
            int size = item.getSize();
            int price = item.getPrice();
            Cart cart = new Cart(id, userId, img, name, price, color, size, quantity);
            HashMap<String, Object> hashMap = cart.convertHashMap();
            db.collection("Cart").document(id).set(hashMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Thành công", Toast.LENGTH_SHORT).show();
                    });
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        FirebaseFirestore.getInstance().collection("User").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (!documentSnapshot.getString("role").equals("customer"))
                            holder.binding.btnAdd.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "onFailure" + e);
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface functionInterface {
        void click(String id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
