package fpoly.mds.beeshoes.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.DialogFunctionBinding;
import fpoly.mds.beeshoes.databinding.ItemBillBinding;
import fpoly.mds.beeshoes.model.Bill;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Bill> list;
    private final functionInterface functionInterface;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public BillAdapter(Context context, ArrayList<Bill> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface = functionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBillBinding binding = ItemBillBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill item = list.get(position);
        holder.binding.tvId.setText("ID: " + item.getId());
        holder.binding.tvPrice.setText("Giá: " + decimalFormat.format(item.getPrice()) + "VND");
        holder.binding.tvNameCustomer.setText("Tên khách hàng: " + item.getNameCustomer());
        holder.binding.tvAddress.setText("Địa chỉ: " + item.getAddress());
        holder.binding.tvPhone.setText("SĐT: " + item.getPhone());
        holder.binding.tvDate.setText("Ngày đặt: " + sdf.format(item.getDate()));

        if (item.getStatus() == 0) {
            holder.binding.tvStatus.setText("Chưa thanh toán");
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.binding.tvStatus.setText("Đã thanh toán");
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
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
        ItemBillBinding binding;

        public ViewHolder(@NonNull ItemBillBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
