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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentAddUpdateShoeTypeBinding;
import fpoly.mds.beeshoes.model.ShoeType;

public class AddUpdateShoeTypeFragment extends Fragment {
    FragmentAddUpdateShoeTypeBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    String id, name;
    Bundle bundle;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddUpdateShoeTypeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        bundle = getArguments();
        binding.cardPickerCamera.setOnClickListener(v -> {
            showDialogPick();
        });
        if (bundle != null) {
            id = bundle.getString("id");
            getID(id, new ShoeTypeCallback() {
                @Override
                public void onShoeTypeLoaded(ShoeType shoeType) {
                    binding.edtName.setText(shoeType.getName());
                    Picasso.get().load(shoeType.getImg()).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera).into(binding.cardPickerCamera);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
        binding.btnSave.setOnClickListener(v -> {
            name = binding.edtName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                binding.edtName.setError("Nhập loại giày");
                binding.edtName.requestFocus();
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
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("shoeType/" + id);
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
        db.collection("ShoeType").document(id).update(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoeTypeFragment()).commit();
            }
        });
    }

    private void uploadFirestoreData(String imgUrl) {
        HashMap<String, Object> hashMap = new ShoeType(id, imgUrl, name).convertHashMap();
        db.collection("ShoeType").document(id).set(hashMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Thành công", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoeTypeFragment()).commit();
                });
    }

    private void getID(String id, AddUpdateShoeTypeFragment.ShoeTypeCallback callback) {
        DocumentReference docRef = db.collection("ShoeType").document(id);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ShoeType shoeType = documentSnapshot.toObject(ShoeType.class);
                            if (shoeType != null) {
                                callback.onShoeTypeLoaded(shoeType);
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

    private interface ShoeTypeCallback {
        void onShoeTypeLoaded(ShoeType shoeType);

        void onFailure(Exception e);
    }
}