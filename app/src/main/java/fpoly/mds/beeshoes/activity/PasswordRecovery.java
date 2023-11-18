package fpoly.mds.beeshoes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import fpoly.mds.beeshoes.R;
import fpoly.mds.beeshoes.databinding.ActivityLoginBinding;
import fpoly.mds.beeshoes.databinding.ActivityPasswordRecoveryBinding;

public class PasswordRecovery extends AppCompatActivity {
    ActivityPasswordRecoveryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordRecoveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnRecovery.setOnClickListener(v->{
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String email = binding.edtEmail.getText().toString();
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(PasswordRecovery.this, "Email đã được gửi.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        });
    }
}