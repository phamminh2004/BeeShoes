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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentAddUpdateBillBinding;
import fpoly.mds.beeshoes.model.Bill;
import fpoly.mds.beeshoes.model.Customer;
import fpoly.mds.beeshoes.model.Shoe;

public class AddUpdateBillFragment extends Fragment {
    private final String REGEX_PHONE_NUMBER = "^[0-9\\-\\+]{9,15}$";
    private final String REGEX_INT = "^\\d+$";
    private final String REGEX_DATE = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$";
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    FragmentAddUpdateBillBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Bundle bundle;
    String id, strPrice, nameCustomer, phone, address, date;
    int status;
    Date currentDate;
    ArrayList<String> listNameShoe;
    ArrayAdapter<String> shoeSdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddUpdateBillBinding.inflate(inflater, container, false);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        bundle = getArguments();
        currentDate = new Date();
        listNameShoe = new ArrayList<>();
        loadSpinner();
        binding.edtDate.setText(sdf.format(currentDate));
        binding.spNameShoe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                nameShoe = (String) parent.getItemAtPosition(position);/

                CollectionReference collectionReference = db.collection("Shoes");

                Query query = collectionReference.whereEqualTo("name", "nameShoe");

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Shoe shoe = document.toObject(Shoe.class);
                                strPrice = shoe.getPrice() + "";
                                binding.edtPrice.setText(strPrice);
                            }
                        } else {
                            Log.e("Firestore", "Lỗi: " + task.getException());
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (bundle != null) {
            id = bundle.getString("id");
            getID(id, new BillCallback() {
                @Override
                public void onBillLoaded(Bill bill) {
                    binding.edtPrice.setText(bill.getPrice()+"");
                    binding.edtNameCustomer.setText(bill.getNameCustomer());
                    binding.edtAddress.setText(bill.getAddress());
                    binding.edtPhone.setText(bill.getPhone());
                    binding.edtDate.setText(sdf.format(bill.getDate()));
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }else{
            binding.edtDate.setFocusable(false);
            binding.edtDate.setClickable(false);
        }
        binding.btnSave.setOnClickListener(v -> {
            nameCustomer = binding.edtNameCustomer.getText().toString();
            phone = binding.edtPhone.getText().toString();
            address = binding.edtAddress.getText().toString();
            date = binding.edtDate.getText().toString();
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(date));
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Date ngayMai = cal.getTime();
                if (ngayMai.before(currentDate)) {
                    status = 1;
                } else {
                    status = 0;
                }
            } catch (Exception e) {

            }
            if ( TextUtils.isEmpty(address) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(nameCustomer) || TextUtils.isEmpty(date) || TextUtils.isEmpty(strPrice)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!phone.matches(REGEX_PHONE_NUMBER)) {
                Toast.makeText(getContext(), "Số điện thoại sai định dạng", Toast.LENGTH_SHORT).show();
            } else if (!date.matches(REGEX_DATE)) {
                Toast.makeText(getContext(), "Ngày sai định dạng (dd-MM-yyyy)", Toast.LENGTH_SHORT).show();
            } else if (!strPrice.matches(REGEX_INT)) {
                Toast.makeText(getContext(), "Giá phải là số tự nhiên", Toast.LENGTH_SHORT).show();
            } else {
                saveData();
            }
        });
        return binding.getRoot();
    }

    private void saveData() {
        if (bundle == null) {
            id = UUID.randomUUID().toString();
            uploadFirestoreData();
        } else {
            uploadFirestoreData();
        }
    }

    private void getID(String id, BillCallback callback) {
        DocumentReference docRef = db.collection("Bill").document(id);
        docRef.get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        try {
                            Bill bill = new Bill(
                                    document.getId(),
                                    document.getLong("price").intValue(),
                                    document.getString("nameCustomer"),
                                    document.getString("phone"),
                                    document.getString("address"),
                                    sdf.parse(document.getString("date")),
                                    document.getLong("status").intValue()
                            );
                            callback.onBillLoaded(bill);
                        } catch (Exception e) {

                        }

                    } else {
                        Log.d("Firebase", "Tài liệu không tồn tại cho id: " + id);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Lỗi khi lấy tài liệu nhân viên với id: " + id, e);
                    callback.onFailure(e);
                });
    }

    private void uploadFirestoreData() {
        try {
            Bill bill = new Bill(id, Integer.parseInt(strPrice), nameCustomer, phone, address, sdf.parse(date), status);
            HashMap<String, Object> hashMap = bill.convertHashMap();
            db.collection("Bill").document(id).set(hashMap).
                    addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new BillFragment()).commit();
                        }
                    });
            uploadCustomer();
        } catch (Exception e) {}
    }
    private void saveCustomerData(){
        Customer customer = new Customer(id,nameCustomer, address, phone);
        HashMap<String, Object> hashMap1 = customer.converHashMap();
        db.collection("Customer").document(id).set(hashMap1).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void uploadCustomer(){
        CollectionReference collectionReference = db.collection("Customer");

        Query query = collectionReference.whereEqualTo("name", nameCustomer);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Customer customer1 = document.toObject(Customer.class);
                            id = customer1.getId();
                        }
                    }
                }
            });
            saveCustomerData();
    }

    private void loadSpinner() {
        db.collection("Shoes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                String name = document.getString("name");
                                listNameShoe.add(name);
                            }
                            shoeSdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listNameShoe);
                            shoeSdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spNameShoe.setAdapter(shoeSdapter);
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
    }

    private interface BillCallback {
        void onBillLoaded(Bill bill);

        void onFailure(Exception e);
    }
}