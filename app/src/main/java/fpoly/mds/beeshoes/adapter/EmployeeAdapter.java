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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.DialogFunctionBinding;
import fpoly.mds.beeshoes.databinding.ItemEmployeeBinding;
import fpoly.mds.beeshoes.fragment.BillFragment;
import fpoly.mds.beeshoes.model.Bill;
import fpoly.mds.beeshoes.model.Employee;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Employee> list;
    private final functionInterface functionInterface;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public EmployeeAdapter(Context context, ArrayList<Employee> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface = functionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEmployeeBinding binding = ItemEmployeeBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        FirebaseFirestore.getInstance().collection("User").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"manager".equals(role)) {
                        holder.binding.btnFuncion.setVisibility(View.GONE);
                    }
                });
        Employee item = list.get(position);
        holder.binding.tvName.setText(item.getName());
        holder.binding.tvBirthday.setText("Ngày sinh: " + sdf.format(item.getBirthday()));
        holder.binding.tvPhone.setText("SĐT: " + item.getPhone());
        holder.binding.tvAddress.setText("Địa chỉ: " + item.getAddress());
        holder.binding.tvRole.setText("Chức vụ: " + item.getRole());
        holder.binding.tvSex.setText("Giới tính: " + item.getSex());
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

    private void getList(BillFragment.FirestoreCallback callback) {
        ArrayList<Bill> list = new ArrayList<>();

    }

    public interface functionInterface {
        void update(String id);

        void delete(String id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemEmployeeBinding binding;

        public ViewHolder(@NonNull ItemEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
