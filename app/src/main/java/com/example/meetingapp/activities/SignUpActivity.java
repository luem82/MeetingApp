package com.example.meetingapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meetingapp.databinding.ActivitySignUpBinding;
import com.example.meetingapp.utils.Consts;
import com.example.meetingapp.utils.MyBitmap;
import com.example.meetingapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String endcodeImage;
    private PreferenceManager preferenceManager;

    private ActivityResultLauncher<Intent> pickPhoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            Uri uriPhoto = result.getData().getData();
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uriPhoto);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                binding.ivProfile.setImageBitmap(bitmap);
                                binding.tvAddPhoto.setVisibility(View.GONE);
                                endcodeImage = MyBitmap.getEndcodeFromBitmap(bitmap);
                            } catch (FileNotFoundException exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.tvSignIn.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.btnSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });

        binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickPhoto.launch(intent);
        });
    }

    public void signUp() {
        loading(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put(Consts.KEY_NAME, binding.edtName.getText().toString());
        user.put(Consts.KEY_EMAIL, binding.edtMail.getText().toString());
        user.put(Consts.KEY_PASSWORD, binding.edtPass.getText().toString());
        user.put(Consts.KEY_IMAGE, endcodeImage);
        firestore.collection(Consts.KEY_COLLECTION_MEMBER)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Consts.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Consts.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Consts.KEY_NAME, binding.edtName.getText().toString());
                    preferenceManager.putString(Consts.KEY_IMAGE, endcodeImage);

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    showToast("Create new account successfully");
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast(e.getMessage());
                });
    }


    public void loading(boolean isLoading) {
        if (isLoading) {
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isValidSignUpDetails() {
        if (endcodeImage == null) {
            showToast("Chọn ảnh cho tài khoản");
            return false;
        } else if (binding.edtName.getText().toString().trim().isEmpty()) {
            showToast("Nhập tên người dùng");
            return false;
        } else if (binding.edtMail.getText().toString().trim().isEmpty()) {
            showToast("Nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtMail.getText().toString()).matches()) {
            showToast("Email không đúng định dạng");
            return false;
        } else if (binding.edtPass.getText().toString().trim().isEmpty()) {
            showToast("Nhập mật khẩu");
            return false;
        } else if (binding.edtConfirmPass.getText().toString().trim().isEmpty()) {
            showToast("Xác nhận mật khẩu");
            return false;
        } else if (!binding.edtConfirmPass.getText().toString().equals(binding.edtPass.getText().toString())) {
            showToast("Mật khẩu không trùng khớp");
            return false;
        } else {
            return true;
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}