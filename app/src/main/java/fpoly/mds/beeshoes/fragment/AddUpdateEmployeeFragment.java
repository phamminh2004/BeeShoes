package fpoly.mds.beeshoes.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentAddUpdateEmployeeBinding;
import fpoly.mds.beeshoes.model.Employee;

public class AddUpdateEmployeeFragment extends Fragment {
    private final String REGEX_PHONE_NUMBER = "^[0-9\\-\\+]{9,15}$";
    private final String REGEX_DATE = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$";
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    FragmentAddUpdateEmployeeBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Bundle bundle;
    String id, name, birthday, sex, phone, address, role;
    ArrayAdapter<String> adapter;
    private Uri img_uri;
    private final ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                try {
                    Picasso.get().load(img_uri).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera).into(binding.cardPickerCamera);
                } catch (Exception e) {
                    Log.e("TAG", "onActivityResult: Không thể load ảnh " + e.getMessage());
                }
            }
        }
    });
    private final ActivityResultLauncher<Intent> galleryActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                img_uri = intent.getData();
                try {
                    Picasso.get().load(img_uri).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera).into(binding.cardPickerCamera);
                } catch (Exception e) {
                    Log.e("TAG", "onActivityResult: Không thể load ảnh " + e.getMessage());
                }
            }
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddUpdateEmployeeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        bundle = getArguments();
        binding.cardPickerCamera.setOnClickListener(v -> {
            showDialogPick();
        });
        String[] listSex = {"Nam", "Nữ", "Khác"};
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listSex);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spSex.setAdapter(adapter);
        binding.spSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sex = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (bundle != null) {
            id = bundle.getString("id");
            Log.e("id", id);
            getID(id, new EmployeeCallback() {
                @Override
                public void onEmployeeLoaded(Employee employee) {
                    binding.spSex.setSelection(adapter.getPosition(employee.getSex()));
                    binding.edtName.setText(employee.getName());
                    binding.edtAddress.setText(employee.getAddress());
                    binding.edtBirthday.setText(sdf.format(employee.getBirthday()));
                    binding.edtPhone.setText(employee.getPhone());
                    binding.edtRole.setText(employee.getRole());
                    Picasso.get().load(employee.getImg()).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera).into(binding.cardPickerCamera);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
        binding.btnSave.setOnClickListener(v -> {
            name = binding.edtName.getText().toString().trim();
            address = binding.edtAddress.getText().toString().trim();
            phone = binding.edtPhone.getText().toString().trim();
            role = binding.edtRole.getText().toString().trim();
            birthday = binding.edtBirthday.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(role) || TextUtils.isEmpty(birthday)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!phone.matches(REGEX_PHONE_NUMBER)) {
                Toast.makeText(getContext(), "Số điện thoại sai định dạng", Toast.LENGTH_SHORT).show();
            } else if (!birthday.matches(REGEX_DATE)) {
                Toast.makeText(getContext(), "Ngày sinh sai định dạng (dd-MM-yyyy)", Toast.LENGTH_SHORT).show();
            } else {
                binding.loadingProgressBar.setVisibility(View.VISIBLE);
                saveData();
            }
        });
        return binding.getRoot();
    }

    private void showDialogPick() {
        String[] options = {"Máy Ảnh", "Thư Viện"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    pickCameraFunction();
                } else if (i == 1) {
                    pickGalleryFunction();
                }
            }
        }).show();
    }

    private void pickCameraFunction() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Máy ảnh");
        img_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);
        cameraActivityResult.launch(intent);
    }

    private void pickGalleryFunction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResult.launch(intent);
    }

    private void saveData() {
        if (bundle == null) {
            if (img_uri == null) {
                Toast.makeText(getContext(), "Bạn chưa chọn ảnh", Toast.LENGTH_SHORT).show();
            } else {
                id = UUID.randomUUID().toString();
                uploadImageAndSaveData();
            }
        } else {
            if (img_uri != null) {
                uploadImageAndSaveData();
            } else {
                updateDataWithoutImage();
            }
        }
    }

    private void uploadImageAndSaveData() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("employee/" + id);
        imageRef.putFile(img_uri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String imgUrl = downloadUri.toString();
                        uploadFirestoreData(imgUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Lỗi khi tải ảnh lên Firebase Storage", e);
                });
    }

    private void updateDataWithoutImage() {
        HashMap<String, Object> employee = new HashMap<>();
        employee.put("id", id);
        employee.put("name", name);
        employee.put("birthday", birthday);
        employee.put("sex", sex);
        employee.put("phone", phone);
        employee.put("address", address);
        employee.put("role", role);
        db.collection("Employee").document(id).update(employee).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new EmployeeFragment()).commit();
            }
        });
    }

    private void uploadFirestoreData(String imgUrl) {
        try {
            HashMap<String, Object> hashMap = new Employee(id, imgUrl, name, sdf.parse(birthday), sex, phone, address, role).convertHashMap();
            db.collection("Employee").document(id).set(hashMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new EmployeeFragment()).commit();
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {
                            Log.e("add", "Task failed: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {

        }
    }

    private void getID(String id, AddUpdateEmployeeFragment.EmployeeCallback callback) {
        DocumentReference docRef = db.collection("Employee").document(id);
        docRef.get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        try {
                            Employee employee = new Employee(
                                    document.getString("id"),
                                    document.getString("img"),
                                    document.getString("name"),
                                    sdf.parse(document.getString("birthday")),
                                    document.getString("sex"),
                                    document.getString("phone"),
                                    document.getString("address"),
                                    document.getString("role"));
                            callback.onEmployeeLoaded(employee);
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


    private interface EmployeeCallback {
        void onEmployeeLoaded(Employee employee);

        void onFailure(Exception e);
    }
}