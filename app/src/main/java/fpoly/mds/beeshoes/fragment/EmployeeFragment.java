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

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.adapter.EmployeeAdapter;
import fpoly.mds.beeshoes.databinding.FragmentEmployeeBinding;
import fpoly.mds.beeshoes.model.Employee;

public class EmployeeFragment extends Fragment implements EmployeeAdapter.functionInterface {
    FragmentEmployeeBinding binding;
    FirebaseFirestore db;
    EmployeeAdapter adapter;
    ArrayList<Employee> list;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private EmployeeAdapter.functionInterface functionInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmployeeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        functionInterface = this;
        list = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(RecyclerView.VERTICAL);
        binding.rvEmployee.setLayoutManager(manager);
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
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new AddUpdateEmployeeFragment()).addToBackStack(null).commit();
        });
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Employee> templist = new ArrayList<>();
                try {
                    if (s.toString().trim() != "") {
                        for (Employee employee : list) {
                            if (String.valueOf(employee.getName()).contains(String.valueOf(s))) {
                                templist.add(employee);
                            }
                        }
                        adapter = new EmployeeAdapter(getContext(), templist, functionInterface);
                        binding.rvEmployee.setAdapter(adapter);
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
        AddUpdateEmployeeFragment updateEmployeeFragment = new AddUpdateEmployeeFragment();
        updateEmployeeFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, updateEmployeeFragment).addToBackStack(null).commit();
    }

    @Override
    public void delete(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có chắc muốn xóa không ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("Employee").document(id).delete().addOnSuccessListener(command -> {
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

    private ArrayList<Employee> getAllList() {
        ArrayList<Employee> listAll = new ArrayList<>();
        db.collection("Employee")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listAll.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                try {
                                    Employee item = new Employee(
                                            document.getString("id"),
                                            document.getString("img"),
                                            document.getString("name"),
                                            sdf.parse(document.getString("birthday")),
                                            document.getString("sex"),
                                            document.getString("phone"),
                                            document.getString("address"),
                                            document.getString("role"));
                                    listAll.add(item);
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {

                                }

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
        adapter = new EmployeeAdapter(getContext(), list, functionInterface);
        binding.rvEmployee.setAdapter(adapter);
    }
    public void onResume() {
        super.onResume();
        loadData();
    }
}