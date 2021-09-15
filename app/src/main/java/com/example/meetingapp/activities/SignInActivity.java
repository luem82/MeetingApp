package com.example.meetingapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.meetingapp.R;
import com.example.meetingapp.databinding.ActivitySignInBinding;
import com.example.meetingapp.utils.Consts;
import com.example.meetingapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.tvCreate.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });

        binding.btnSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
    }

    public void signIn() {
        loading(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Consts.KEY_COLLECTION_MEMBER)
                .whereEqualTo(Consts.KEY_EMAIL, binding.edtMail.getText().toString())
                .whereEqualTo(Consts.KEY_PASSWORD, binding.edtPass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Consts.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Consts.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Consts.KEY_NAME, documentSnapshot.getString(Consts.KEY_NAME));
                        preferenceManager.putString(Consts.KEY_IMAGE, documentSnapshot.getString(Consts.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Signin failue");
                    }
                });
    }

    public void loading(boolean isLoading) {
        if (isLoading) {
            binding.btnSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isValidSignInDetails() {
        if (binding.edtMail.getText().toString().isEmpty()) {
            showToast("Nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtMail.getText().toString()).matches()) {
            showToast("Email không đúng định dạng");
            return false;
        } else if (binding.edtPass.getText().toString().trim().isEmpty()) {
            showToast("Nhập mật khẩu");
            return false;
        } else {
            return true;
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}