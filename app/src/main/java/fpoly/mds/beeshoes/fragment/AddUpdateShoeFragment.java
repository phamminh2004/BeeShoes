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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentAddUpdateShoeBinding;
import fpoly.mds.beeshoes.model.Shoe;


public class AddUpdateShoeFragment extends Fragment {
    private final String REGEX_INT = "^\\d+$";
    FragmentAddUpdateShoeBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Bundle bundle;
    String id, name, color, strPrice, strSize, shoeType, describe;
    ArrayList<String> listName;
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
        binding = FragmentAddUpdateShoeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        bundle = getArguments();
        listName = new ArrayList<>();
        loadSpinner();
        binding.cardPickerCamera.setOnClickListener(v -> {
            showDialogPick();
        });
        binding.spShoeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shoeType = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (bundle != null) {
            id = bundle.getString("id");
            getID(id, new ShoeCallback() {
                @Override
                public void onShoeLoaded(Shoe shoe) {
                    binding.edtName.setText(shoe.getName());
                    binding.spShoeType.setSelection(adapter.getPosition(shoe.getShoeType()));
                    binding.edtPrice.setText(shoe.getPrice() + "");
                    binding.edtSize.setText(shoe.getSize() + "");
                    binding.edtColor.setText(shoe.getColor());
                    binding.edtDescribe.setText(shoe.getDescribe());
                    Picasso.get().load(shoe.getImg()).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera).into(binding.cardPickerCamera);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
        binding.btnSave.setOnClickListener(v -> {
            name = binding.edtName.getText().toString().trim();
            color = binding.edtColor.getText().toString().trim();
            strPrice = binding.edtPrice.getText().toString().trim();
            strSize = binding.edtSize.getText().toString().trim();
            describe = binding.edtDescribe.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(color) || TextUtils.isEmpty(strPrice) || TextUtils.isEmpty(strSize)||TextUtils.isEmpty(describe)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!strPrice.matches(REGEX_INT) || !strSize.matches(REGEX_INT)) {
                Toast.makeText(getContext(), "Giá và size là số tự nhiên", Toast.LENGTH_SHORT).show();
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
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("shoes/" + id);
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
        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("shoeType", shoeType);
        updateData.put("price", Integer.parseInt(strPrice));
        updateData.put("color", color);
        updateData.put("size", Integer.parseInt(strSize));
        updateData.put("describe", describe);
        db.collection("Shoes").document(id).update(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoesFragment()).commit();
            }
        });
    }

    private void uploadFirestoreData(String imgUrl) {
        HashMap<String, Object> hashMap = new Shoe(id, imgUrl, name, shoeType, Integer.parseInt(strPrice), color, Integer.parseInt(strSize), describe).convertHashMap();
        db.collection("Shoes").document(id).set(hashMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoesFragment()).commit();
                });
    }

    private void getID(String id, AddUpdateShoeFragment.ShoeCallback callback) {
        DocumentReference docRef = db.collection("Shoes").document(id);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Shoe shoe = documentSnapshot.toObject(Shoe.class);
                            if (shoe != null) {
                                callback.onShoeLoaded(shoe);
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

    private void loadSpinner() {
        db.collection("ShoeType")
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
                                Log.e("name",listName.toString());
                            }
                            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listName);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spShoeType.setAdapter(adapter);
                        } else {
                            Log.d("TAG", "Error getting documents: " + task.getException());
                        }
                    }
                });
    }

    private interface ShoeCallback {
        void onShoeLoaded(Shoe shoe);

        void onFailure(Exception e);
    }
}


