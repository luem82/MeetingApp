package com.example.meetingapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.meetingapp.adapters.UserAdapter;
import com.example.meetingapp.databinding.ActivityMainBinding;
import com.example.meetingapp.models.User;
import com.example.meetingapp.utils.Consts;
import com.example.meetingapp.utils.MyBitmap;
import com.example.meetingapp.utils.PreferenceManager;
import com.example.meetingapp.utils.UserListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> userList;
    private UserAdapter userAdapter;
    private GridLayoutManager gridLayoutManager;
    private int RQC_BATTERY_OPTIMIZATIONS = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sendFCMToDatabase(task.getResult().getToken());
            }
        });

        loadUserInfo();
        setListeners();
        initRecyclerView();
        getUsers();
        checkForBatteryOptimizations();
    }

    private void initRecyclerView() {
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvUsers.setLayoutManager(gridLayoutManager);
        binding.rvUsers.setAdapter(userAdapter);
        binding.swipeRefresh.setOnRefreshListener(this::getUsers);
    }

    private void getUsers() {
        binding.swipeRefresh.setRefreshing(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Consts.KEY_COLLECTION_MEMBER).get()
                .addOnCompleteListener(task -> {
                    binding.swipeRefresh.setRefreshing(false);
                    String currentUserId = preferenceManager.getString(Consts.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            if (currentUserId.equals(snapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = snapshot.getString(Consts.KEY_NAME);
                            user.email = snapshot.getString(Consts.KEY_EMAIL);
                            user.photo = snapshot.getString(Consts.KEY_IMAGE);
                            user.token = snapshot.getString(Consts.KEY_FCM_TOKEN);
                            user.id = snapshot.getId();

                            userList.add(user);
                        }
                        if (userList.size() > 0) {
                            userAdapter.notifyDataSetChanged();
                        } else {
                            binding.tvError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.tvError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadUserInfo() {
        Bitmap bitmap = MyBitmap.getBitmapFromStringEndcode(preferenceManager.getString(Consts.KEY_IMAGE));
        binding.ivUserAvatar.setImageBitmap(bitmap);
        String name = preferenceManager.getString(Consts.KEY_NAME);
        binding.tvUserName.setText(name);
    }

    private void setListeners() {
        binding.frSignOut.setOnClickListener(v -> {
            signOut();
        });
    }

    private void sendFCMToDatabase(String token) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Consts.KEY_COLLECTION_MEMBER)
                .document(preferenceManager.getString(Consts.KEY_USER_ID));
        documentReference.update(Consts.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> {
                    showToast("Unable to send token: " + e.getMessage());
                });
    }

    private void signOut() {
        binding.ivSignOut.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        showToast("Signing out...");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection(Consts.KEY_COLLECTION_MEMBER)
                .document(preferenceManager.getString(Consts.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Consts.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Unable to sign out, try again");
                    binding.ivSignOut.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                });
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAudioClicked(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, user.name + " is not available for meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onVideoClicked(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, user.name + " is not available for meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }



    private ActivityResultLauncher<Intent> batteryPotimization =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            checkForBatteryOptimizations();
                        }
                    });

    private void checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt runing background services.");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        batteryPotimization.launch(intent);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        }
    }
}
