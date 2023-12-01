package fpoly.mds.beeshoes.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import fpoly.mds.beeshoes.databinding.ActivityPasswordRecoveryBinding;

public class PasswordRecovery extends AppCompatActivity {
    ActivityPasswordRecoveryBinding binding;
    String email;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordRecoveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        binding.btnRecovery.setOnClickListener(v -> {
            email = binding.edtEmail.getText().toString();
            if (TextUtils.isEmpty(email)) {
                binding.edtEmail.setError("Vui lòng nhập email.");
                binding.edtEmail.requestFocus();
            } else {
                recovery();
            }
        });
    }

    private void recovery() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection("User");
        reference.whereEqualTo("email", email)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PasswordRecovery.this, "Email đã được gửi.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    Toast.makeText(PasswordRecovery.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
    }
}