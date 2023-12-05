package fpoly.mds.beeshoes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.ItemBillBinding;
import fpoly.mds.beeshoes.model.Bill;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Bill> list;
    private final functionInterface functionInterface;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    int status;

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
        holder.binding.tvPrice.setText("Giá: đ" + decimalFormat.format(item.getPrice()));
        holder.binding.tvNameCustomer.setText("Họ tên: " + item.getNameCustomer());
        holder.binding.tvAddress.setText("Địa chỉ: " + item.getAddress());
        holder.binding.tvPhone.setText("SĐT: " + item.getPhone());
        holder.binding.tvDate.setText("Ngày đặt hàng: " + sdf.format(item.getDate()));
        try {
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(item.getDate());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date datePlus1 = cal.getTime();
            if (datePlus1.before(currentDate)) {
                status = 1;
                holder.binding.tvStatus.setText("Đã thanh toán");
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));

                updateFirebase(item.getId());
            } else {
                holder.binding.tvStatus.setText("Chưa thanh toán");
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        } catch (Exception e) {

        }
        holder.itemView.setOnClickListener(v -> {
            functionInterface.click(item.getId());
        });
    }

    private void updateFirebase(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        db.collection("Bill").document(id).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
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
        ItemBillBinding binding;

        public ViewHolder(@NonNull ItemBillBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
