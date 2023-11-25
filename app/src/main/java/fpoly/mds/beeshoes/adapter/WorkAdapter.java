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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.DialogFunctionBinding;
import fpoly.mds.beeshoes.databinding.ItemWorkBinding;
import fpoly.mds.beeshoes.model.Work;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Work> list;
    private final functionInterface functionInterface;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

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
        Work work = list.get(position);
        holder.binding.tvName.setText(work.getName());
        holder.binding.tvShift.setText("Ca: " + work.getShift());
        holder.binding.tvTimeStart.setText("Từ: " + dtf.format(work.getTimeStart()));
        holder.binding.tvTimeEnd.setText("Đến: " + dtf.format(work.getTimeEnd()));
        if (work.getStatus() == 0) {
            holder.binding.tvStatus.setText("Ngoài ca");
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.binding.tvStatus.setText("Đang làm");
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
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
