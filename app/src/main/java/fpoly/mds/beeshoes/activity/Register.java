package fpoly.mds.beeshoes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import fpoly.mds.beeshoes.databinding.ActivityRegisterBinding;
import fpoly.mds.beeshoes.model.User;

public class Register extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;
    String REGEX_EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        binding.btnRegister.setOnClickListener(v -> {
            validate();
        });
        binding.tvLogin.setOnClickListener(v -> {
            onBackPressed();
        });
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
                    User user1 = new User(email, "customer");
                    HashMap<String, Object> hashMap = user1.convertHashMap();
                    database.collection("User").document(auth.getCurrentUser().getUid()).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                        }
                    });
                    startActivity(new Intent(Register.this, Login.class));
                    Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    binding.loadingProgressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
                    binding.loadingProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
