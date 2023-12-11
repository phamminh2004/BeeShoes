package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentAddUpdateWorkBinding;
import fpoly.mds.beeshoes.model.Work;

public class AddUpdateWorkFragment extends Fragment {
    final String REGEX_TIME = "^([01]\\d|2[0-3]):([0-5]\\d)$";
    private final String REGEX_INT = "^\\d+$";
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    FragmentAddUpdateWorkBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Bundle bundle;
    String id, name, shift, timeStart, timeEnd;
    int status;
    ArrayList<String> listName;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddUpdateWorkBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        bundle = getArguments();
        listName = new ArrayList<>();
        loadSpinner();
        binding.spName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                name = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (bundle != null) {
            id = bundle.getString("id");
            getID(id, new WorkCallback() {
                @Override
                public void onWorkLoaded(Work work) {
                    binding.edtTimeStart.setText(dtf.format(work.getTimeStart()));
                    binding.edtTimeEnd.setText(dtf.format(work.getTimeEnd()));
                    binding.edtShift.setText(work.getShift() + "");
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

        }
        binding.btnSave.setOnClickListener(v -> {
            timeStart = binding.edtTimeStart.getText().toString();
            timeEnd = binding.edtTimeEnd.getText().toString();
            shift = binding.edtShift.getText().toString();
            if (TextUtils.isEmpty(timeEnd) || TextUtils.isEmpty(timeStart) || TextUtils.isEmpty(shift)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!timeStart.matches(REGEX_TIME) || !timeStart.matches(REGEX_TIME)) {
                Toast.makeText(getContext(), "Sai định dạng thời gian (HH:mm)", Toast.LENGTH_SHORT).show();
            } else if (!shift.matches(REGEX_INT)) {
                Toast.makeText(getContext(), "Ca là số tự nhiên", Toast.LENGTH_SHORT).show();
            } else {
                binding.loadingProgressBar.setVisibility(View.VISIBLE);
                saveData();
            }
        });
        return binding.getRoot();
    }

    private void getID(String id, AddUpdateWorkFragment.WorkCallback callback) {
        DocumentReference docRef = db.collection("Work").document(id);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            TemporalAccessor tempS = dtf.parse(documentSnapshot.getString("timeStart"));
                            LocalTime timeStart = LocalTime.from(tempS);
                            TemporalAccessor tempE = dtf.parse(documentSnapshot.getString("timeEnd"));
                            LocalTime timeEnd = LocalTime.from(tempE);
                            Work item = new Work(
                                    documentSnapshot.getString("id"),
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getLong("shift").intValue(),
                                    timeStart,
                                    timeEnd,
                                    documentSnapshot.getLong("status").intValue());
                            if (item != null) {
                                callback.onWorkLoaded(item);
                            } else {
                                Log.d("Firebase", "Không thể chuyển đổi thành đối tượng ShoeType");
                            }
                        } else {
                            Log.d("Firebase", "Tài liệu không tồn tại");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    private void saveData() {
        if (bundle == null) {
            id = UUID.randomUUID().toString();
            uploadFirestoreData();
        } else {
            uploadFirestoreData();
        }
    }

    private void uploadFirestoreData() {
        int shift = Integer.parseInt(binding.edtShift.getText().toString());
        TemporalAccessor tempS = dtf.parse(binding.edtTimeStart.getText().toString());
        LocalTime timeStart = LocalTime.from(tempS);
        TemporalAccessor tempE = dtf.parse(binding.edtTimeEnd.getText().toString());
        LocalTime timeEnd = LocalTime.from(tempE);
        LocalTime localTimeNow = LocalTime.now();
        if (timeStart.isAfter(timeEnd)) {
            Toast.makeText(getContext(), "Thời gian bắt đầu, kết thúc sai định dạng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeStart.isBefore(localTimeNow) && timeEnd.isAfter(localTimeNow)) {
            status = 1;
        } else {
            status = 0;
        }
        Work work = new Work(id, name, shift, timeStart, timeEnd, status);
        HashMap<String, Object> updateData = work.convertHashMap();
        db.collection("Work").document(id).set(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new WorkFragment()).commit();
            }
        });
    }

    private void loadSpinner() {
        db.collection("Employee")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                String name = document.getString("name");
                                listName.add(name);
                            }
                            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listName);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spName.setAdapter(adapter);
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
    }

    private interface WorkCallback {
        void onWorkLoaded(Work work);

        void onFailure(Exception e);
    }
}