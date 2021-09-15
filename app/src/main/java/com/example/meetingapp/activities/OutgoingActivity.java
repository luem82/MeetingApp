package com.example.meetingapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.meetingapp.R;
import com.example.meetingapp.databinding.ActivityOutgoingBinding;
import com.example.meetingapp.models.User;
import com.example.meetingapp.network.ApiClient;
import com.example.meetingapp.network.ApiService;
import com.example.meetingapp.utils.Consts;
import com.example.meetingapp.utils.MyBitmap;
import com.example.meetingapp.utils.PreferenceManager;
import com.google.firebase.iid.FirebaseInstanceId;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingActivity extends AppCompatActivity {

    private ActivityOutgoingBinding binding;
    private User user;
    private String meetingType;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private Animation alphaAnimation;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.animation_alpha);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);

        user = (User) getIntent().getSerializableExtra("user");
        meetingType = getIntent().getStringExtra("type");
        if (user != null && meetingType != null) {
            loadUserDetails();
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult().getToken();
                if (user != null && meetingType != null) {
                    loadUserDetails();
                    initiateMeeting(meetingType, user.token);
                }
            }
        });

        setListeners();


    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject data = new JSONObject();
            JSONObject body = new JSONObject();

            data.put(Consts.REMOTE_MSG_TYPE, Consts.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Consts.REMOTE_MSG_INVITATION_RESPONSE, Consts.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Consts.REMOTE_MSG_DATA, data);
            body.put(Consts.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Consts.REMOTE_MSG_INVITATION_CANCELLED);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void initiateMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject data = new JSONObject();
            JSONObject body = new JSONObject();

            data.put(Consts.REMOTE_MSG_TYPE, Consts.REMOTE_MSG_INVITATION);
            data.put(Consts.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Consts.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            data.put(Consts.REMOTE_MSG_USER_NAME, preferenceManager.getString(Consts.KEY_NAME));
            data.put(Consts.REMOTE_MSG_USER_ID, preferenceManager.getString(Consts.KEY_USER_ID));
            meetingRoom = preferenceManager.getString(Consts.KEY_USER_ID) + "_" +
                    UUID.randomUUID().toString().substring(0, 5);
            data.put(Consts.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Consts.REMOTE_MSG_DATA, data);
            body.put(Consts.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendRemoteMessage(body.toString(), Consts.REMOTE_MSG_INVITATION);
        } catch (Exception ex) {
            Toast.makeText(OutgoingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class)
                .sendRemoteMessage(Consts.getRemoteMessageHeaders(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Consts.REMOTE_MSG_INVITATION)) {
                                Toast.makeText(OutgoingActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
                            } else if (type.equals(Consts.REMOTE_MSG_INVITATION_CANCELLED)) {
                                Toast.makeText(OutgoingActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(OutgoingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(OutgoingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setListeners() {
        binding.ivOutRejected.setOnClickListener(v -> {
            if (user != null) {
                cancelInvitation(user.token);
            }
        });
    }

    private void loadUserDetails() {
        if (meetingType.equals("audio")) {
            binding.ivOutCallType.setImageResource(R.drawable.ic_round_call_24);
            binding.tvOutTypeCall.setText("Audio calling " + user.name);
        } else {
            binding.ivOutCallType.setImageResource(R.drawable.ic_round_videocam_24);
            binding.tvOutTypeCall.setText("Video calling " + user.name);
        }
        binding.tvOutUserName.setText(user.name);
        binding.ivOutAvatar.setImageBitmap(MyBitmap.getBitmapFromStringEndcode(user.photo));
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Consts.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Consts.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")) {
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutgoingActivity.this, builder.build());
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals(Consts.REMOTE_MSG_INVITATION_REJECTED)) {
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        binding.ivOutAvatar.startAnimation(alphaAnimation);
        ringtone.play();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Consts.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.ivOutAvatar.clearAnimation();
        ringtone.stop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(invitationResponseReceiver);
    }
}