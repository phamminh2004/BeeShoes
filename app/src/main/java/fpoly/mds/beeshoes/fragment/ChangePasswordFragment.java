package fpoly.mds.beeshoes.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fpoly.mds.beeshoes.databinding.FragmentChangePasswordBinding;

public class ChangePasswordFragment extends Fragment {
    FragmentChangePasswordBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        user  = auth.getCurrentUser();
        binding.btnChangePassword.setOnClickListener(v -> {
            validate();
        });
        return binding.getRoot();
    }

    public void validate() {
        String currentPassword = binding.edtCurrentPassword.getText().toString().trim();
        String newPassword = binding.edtNewPassword.getText().toString().trim();
        String rePassword = binding.edtRePassword.getText().toString().trim();
        if (TextUtils.isEmpty(currentPassword)) {
            binding.edtCurrentPassword.setError("Nhập mật khẩu hiện tại");
            binding.edtCurrentPassword.requestFocus();
        } else if (TextUtils.isEmpty(newPassword)) {
            binding.edtNewPassword.setError("Nhập mật khẩu mới");
            binding.edtNewPassword.requestFocus();
        }else if(newPassword.length()<6){
            binding.edtNewPassword.setError("Mật khẩu phải từ 6 kí tự");
            binding.edtNewPassword.requestFocus();
        } else if (TextUtils.isEmpty(rePassword)) {
            binding.edtRePassword.setError("Nhập lại mật khẩu");
            binding.edtRePassword.requestFocus();
        } else if (!rePassword.equals(newPassword)) {
            binding.edtRePassword.setError("Mật khẩu không trùng khớp");
            binding.edtRePassword.requestFocus();
        } else{
            binding.loadingProgressBar.setVisibility(View.VISIBLE);
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()){
                            Toast.makeText(getContext(), "Cập nhật mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            binding.edtCurrentPassword.setText("");
                            binding.edtNewPassword.setText("");
                            binding.edtRePassword.setText("");
                            binding.loadingProgressBar.setVisibility(View.GONE);
                        }else{
                            Toast.makeText(getContext(), "Cập nhật mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                            binding.loadingProgressBar.setVisibility(View.GONE);
                        }
                    });
                }else{
                    binding.edtCurrentPassword.setError("Mật khẩu hiện tại sai");
                    binding.edtCurrentPassword.requestFocus();
                }
            });
        }
    }

}