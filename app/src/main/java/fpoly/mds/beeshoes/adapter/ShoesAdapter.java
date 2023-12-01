package fpoly.mds.beeshoes.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.DialogFunctionBinding;
import fpoly.mds.beeshoes.databinding.ItemShoesBinding;
import fpoly.mds.beeshoes.model.Shoe;

public class ShoesAdapter extends RecyclerView.Adapter<ShoesAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Shoe> list;
    private final functionInterface functionInterface;
    DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public ShoesAdapter(Context context, ArrayList<Shoe> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface = functionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShoesBinding binding = ItemShoesBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shoe item = list.get(position);
        holder.binding.tvName.setText(item.getName());
        holder.binding.tvPrice.setText("Giá: đ" + decimalFormat.format(item.getPrice()));
        holder.binding.tvShoeType.setText("Hãng: " + item.getShoeType());
        holder.binding.tvColor.setText("Màu sắc: " + item.getColor());
        holder.binding.tvSize.setText("Size: " + item.getSize());
        String img = item.getImg();
        try {
            Picasso.get().load(img).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera)
                    .into(holder.binding.ivImg);
        } catch (Exception e) {
            Log.e("PicassoError", "Error loading image: " + e.getMessage());
        }
        holder.binding.btnFuncion.setOnClickListener(v -> {
            openDialogChucNang(item.getId());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void openDialogChucNang(String id) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        DialogFunctionBinding binding = DialogFunctionBinding.inflate(inflater);
        View view = binding.getRoot();
        Dialog dialog = new Dialog(context);
        dialog.setContentView(view);
        dialog.show();
        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                functionInterface.update(id);
                dialog.dismiss();
            }
        });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                functionInterface.delete(id);
                dialog.dismiss();
            }
        });
    }

    public interface functionInterface {
        void update(String id);

        void delete(String id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemShoesBinding binding;

        public ViewHolder(@NonNull ItemShoesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
