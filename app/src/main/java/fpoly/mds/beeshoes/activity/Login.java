package fpoly.mds.beeshoes.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import fpoly.mds.beeshoes.databinding.ActivityLoginBinding;
import fpoly.mds.beeshoes.model.User;

public class Login extends AppCompatActivity {
    ActivityLoginBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        SharedPreferences spf = getSharedPreferences("USER_FILE", MODE_PRIVATE);
        binding.edtEmail.setText(spf.getString("EMAIL", ""));
        binding.edtPassword.setText(spf.getString("PASSWORD", ""));
        binding.chkCheckPass.setChecked(spf.getBoolean("REMEMBER", false));
        binding.btnLogin.setOnClickListener(v -> {
            validate();
        });
        binding.tvPasswordRecovery.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, PasswordRecovery.class));
        });
    }

    public void validate() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Nhập tài khoản");
            binding.edtEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            binding.edtPassword.setError("Nhập mật khẩu");
            binding.edtPassword.requestFocus();
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            rememberUser(email, password, binding.chkCheckPass.isChecked());
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            FirebaseUser currentUser = auth.getCurrentUser();
                            if (currentUser != null) {
                                String userId = currentUser.getUid();
                                database.collection("User").document(userId)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String role = documentSnapshot.getString("role");
                                                if ("manager".equals(role)) {
                                                    intent.putExtra("role", "manager");
                                                    startActivity(intent);
                                                } else if ("employee".equals(role)) {
                                                    intent.putExtra("role", "employee");
                                                    startActivity(intent);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Error", "onFailure" + e);
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Tài khoản hoặc mật khẩu sai", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void rememberUser(String email, String pass, boolean status) {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_FILE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!status) {
            editor.clear();
        } else {
            editor.putString("EMAIL", email);
            editor.putString("PASSWORD", pass);
            editor.putBoolean("REMEMBER", status);
        }
        editor.commit();
    }
}