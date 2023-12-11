package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import fpoly.mds.beeshoes.databinding.FragmentAddAccountBinding;
import fpoly.mds.beeshoes.model.User;


public class AddAccountFragment extends Fragment {
    FragmentAddAccountBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String REGEX_EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddAccountBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        binding.btnReg.setOnClickListener(v -> {
            validate();
        });
        return binding.getRoot();
    }

    public void validate() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String rePass = binding.edtRePassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Nhập email");
            binding.edtEmail.requestFocus();
        } else if (!email.matches(REGEX_EMAIL)) {
            binding.edtEmail.setError("Email sai định dạng");
            binding.edtEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            binding.edtPassword.setError("Nhập mật khẩu");
            binding.edtPassword.requestFocus();
        } else if (password.length() < 6) {
            binding.edtPassword.setError("Mật khẩu phải từ 6 kí tự");
            binding.edtPassword.requestFocus();
        } else if (TextUtils.isEmpty(rePass)) {
            binding.edtRePassword.setError("Nhập lại mật khẩu");
            binding.edtRePassword.requestFocus();
        } else if (!rePass.equals(password)) {
            binding.edtRePassword.setError("Mật khẩu không trùng khớp");
            binding.edtRePassword.requestFocus();
        } else {
            binding.loadingProgressBar.setVisibility(View.VISIBLE);
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user1 = new User(email, "employee");
                    HashMap<String, Object> hashMap = user1.convertHashMap();
                    db.collection("User").document(auth.getCurrentUser().getUid()).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                        }
                    });
                    Toast.makeText(getContext(), "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    binding.edtEmail.setText("");
                    binding.edtPassword.setText("");
                    binding.edtRePassword.setText("");
                    binding.loadingProgressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show();
                    binding.loadingProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}