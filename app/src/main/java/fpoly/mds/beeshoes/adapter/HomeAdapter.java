package fpoly.mds.beeshoes.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.ItemProductBinding;
import fpoly.mds.beeshoes.fragment.HomeCustomerFragment;
import fpoly.mds.beeshoes.model.Cart;
import fpoly.mds.beeshoes.model.Shoe;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private final Context context;
    private final ArrayList<Shoe> list;
    private final functionInterface functionInterface;

    public HomeAdapter(Context context, ArrayList<Shoe> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface =functionInterface;
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
        holder.binding.tvPrice.setText(decimalFormat.format(item.getPrice()) + "VND");
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

    }

    public interface functionInterface {
        void click(String id);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
