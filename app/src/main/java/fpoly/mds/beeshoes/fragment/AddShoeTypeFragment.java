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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.UUID;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.FragmentAddShoeTypeBinding;
import fpoly.mds.beeshoes.model.ShoeType;

public class AddShoeTypeFragment extends Fragment {
    FragmentAddShoeTypeBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri img_uri;
    String id;
    private final ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                try {
                    Picasso.get().load(img_uri).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera).into(binding.cardPickerCamera);
                } catch (Exception e) {
                    Log.d("TAG", "onActivityResult: Không thể load ảnh " + e.getMessage());
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
                    Log.d("TAG", "onActivityResult: Không thể load ảnh " + e.getMessage());
                }
            }
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddShoeTypeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        binding.cardPickerCamera.setOnClickListener(v -> {
            showDialogPick();
        });
        binding.btnSave.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.edtName.getText().toString().trim())) {
                binding.edtName.setError("Nhập loại giày");
                binding.edtName.requestFocus();
            } else if (img_uri == null) {
                Toast.makeText(getContext(), "Bạn chưa chọn ảnh", Toast.LENGTH_SHORT).show();
            } else {
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
                    pickCameraFuntion();
                } else if (i == 1) {
                    pickGalleryFuntion();
                }
            }
        }).show();
    }

    private void pickCameraFuntion() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Máy ảnh");
        img_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);
        cameraActivityResult.launch(intent);
    }

    private void pickGalleryFuntion() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResult.launch(intent);
    }

    private void saveData() {
        id = UUID.randomUUID().toString();
        storageReference = FirebaseStorage.getInstance().getReference("shoeType/" + id);

        storageReference.putFile(img_uri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String name = binding.edtName.getText().toString();
                    String imageUrl = uri.toString();

                    ShoeType obj = new ShoeType(id, imageUrl, name);
                    HashMap<String, Object> hashMap = obj.convertHashMap();

                    db.collection("ShoeType").document(id).set(hashMap)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, new ShoeTypeFragment()).commit();
                            });
                }))
                .addOnFailureListener(e -> {
                    // Lỗi
                });
    }

}