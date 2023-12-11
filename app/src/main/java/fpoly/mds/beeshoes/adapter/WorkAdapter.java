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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.DialogFunctionBinding;
import fpoly.mds.beeshoes.databinding.ItemWorkBinding;
import fpoly.mds.beeshoes.model.Work;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Work> list;
    private final functionInterface functionInterface;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    int status;

    public WorkAdapter(Context context, ArrayList<Work> list, functionInterface functionInterface) {
        this.context = context;
        this.list = list;
        this.functionInterface = functionInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkBinding binding = ItemWorkBinding.inflate(LayoutInflater.from(context), parent, false);
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
        Work work = list.get(position);
        holder.binding.tvName.setText("Nhân viên: " + work.getName());
        holder.binding.tvShift.setText("Ca: " + work.getShift());
        holder.binding.tvTimeStart.setText("Từ: " + dtf.format(work.getTimeStart()));
        holder.binding.tvTimeEnd.setText("Đến: " + dtf.format(work.getTimeEnd()));

        TemporalAccessor tempS = dtf.parse(work.getTimeStart().toString());
        LocalTime timeStart = LocalTime.from(tempS);
        TemporalAccessor tempE = dtf.parse(work.getTimeEnd().toString());
        LocalTime timeEnd = LocalTime.from(tempE);
        LocalTime localTimeNow = LocalTime.now();
        if (timeStart.isBefore(localTimeNow) && timeEnd.isAfter(localTimeNow)) {
            status = 1;
            holder.binding.tvStatus.setText("Trong ca");
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
            updateStatus(work.getId());
        } else {
            status = 0;
            holder.binding.tvStatus.setText("Ngoài ca");
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
            updateStatus(work.getId());
        }
        holder.binding.btnFuncion.setOnClickListener(v -> {
            openDialogChucNang(work.getId());
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

    private void updateStatus(String id) {
        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("status", status);
        FirebaseFirestore.getInstance().collection("Work").document(id).update(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
    }

    public interface functionInterface {
        void update(String id);

        void delete(String id);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemWorkBinding binding;

        public ViewHolder(@NonNull ItemWorkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
