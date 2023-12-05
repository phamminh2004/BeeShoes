package fpoly.mds.beeshoes.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.WorkAdapter;
import fpoly.mds.beeshoes.databinding.FragmentWorkBinding;
import fpoly.mds.beeshoes.model.Work;

public class WorkFragment extends Fragment implements WorkAdapter.functionInterface {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    FragmentWorkBinding binding;
    FirebaseFirestore db;
    WorkAdapter adapter;
    ArrayList<Work> list = new ArrayList<>();
    private WorkAdapter.functionInterface functionInterface;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvWork.setLayoutManager(manager);
        loadData();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        db.collection("User").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"manager".equals(role)) {
                        binding.btnAdd.setVisibility(View.GONE);
                    }
                });
        binding.btnAdd.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new AddUpdateWorkFragment()).addToBackStack(null).commit();
        });
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Work> templist = new ArrayList<>();
                try {
                    if (s.toString().trim() != "") {
                        for (Work work : list) {
                            if (String.valueOf(work.getName()).contains(String.valueOf(s))) {
                                templist.add(work);
                            }
                        }
                        adapter = new WorkAdapter(getContext(), templist, functionInterface);
                        binding.rvWork.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Lỗi tìm kiếm" + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }

    @Override
    public void update(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        AddUpdateWorkFragment updateWorkFragment = new AddUpdateWorkFragment();
        updateWorkFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, updateWorkFragment).addToBackStack(null).commit();
    }

    @Override
    public void delete(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có chắc muốn xóa không ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("Work").document(id).delete().addOnSuccessListener(command -> {
                    Toast.makeText(getContext(), "Xoá thành công", Toast.LENGTH_SHORT).show();
                    loadData();
                });
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private ArrayList<Work> getAllList() {
        ArrayList<Work> listAll = new ArrayList<>();
        db.collection("Work")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listAll.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                TemporalAccessor tempS = dtf.parse(document.getString("timeStart"));
                                LocalTime timeStart = LocalTime.from(tempS);
                                TemporalAccessor tempE = dtf.parse(document.getString("timeEnd"));
                                LocalTime timeEnd = LocalTime.from(tempE);
                                Work item = new Work(
                                        document.getString("id"),
                                        document.getString("name"),
                                        document.getLong("shift").intValue(),
                                        timeStart,
                                        timeEnd,
                                        document.getLong("status").intValue());
                                listAll.add(item);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
        return listAll;
    }

    private void loadData() {
        list = getAllList();
        adapter = new WorkAdapter(getContext(), list, functionInterface);
        binding.rvWork.setAdapter(adapter);
    }
    public void onResume() {
        super.onResume();
        loadData();
    }
}