package com.example.meetingapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.example.meetingapp.R;
import com.example.meetingapp.databinding.ActivityIncomingBinding;
import com.example.meetingapp.models.User;
import com.example.meetingapp.network.ApiClient;
import com.example.meetingapp.network.ApiService;
import com.example.meetingapp.utils.Consts;
import com.example.meetingapp.utils.MyBitmap;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingActivity extends AppCompatActivity {

    private ActivityIncomingBinding binding;
    private String userId, userName, meetingType;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        meetingType = getIntent().getStringExtra(Consts.REMOTE_MSG_MEETING_TYPE);
        userId = getIntent().getStringExtra(Consts.REMOTE_MSG_USER_ID);
        userName = getIntent().getStringExtra(Consts.REMOTE_MSG_USER_NAME);

        if (meetingType != null && userName != null && userId != null) {
            if (meetingType.equals("audio")) {
                binding.ivInCallType.setImageResource(R.drawable.ic_round_call_24);
                binding.tvInTypeCall.setText(userName + " calling audio");
            } else {
                binding.ivInCallType.setImageResource(R.drawable.ic_round_videocam_24);
                binding.tvInTypeCall.setText(userName + " calling video");
            }
            binding.tvInUserName.setText(userName);
            FirebaseFirestore.getInstance().collection(Consts.KEY_COLLECTION_MEMBER)
                    .document(userId).addSnapshotListener((value, error) -> {
                String endcodeImage = value.getString(Consts.KEY_IMAGE);
                binding.ivInAvatar.setImageBitmap(MyBitmap.getBitmapFromStringEndcode(endcodeImage));
            });

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            ringtone.play();
        }

        setListeners();

    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject data = new JSONObject();
            JSONObject body = new JSONObject();

            data.put(Consts.REMOTE_MSG_TYPE, Consts.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Consts.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Consts.REMOTE_MSG_DATA, data);
            body.put(Consts.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
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
                            if (type.equals(Consts.REMOTE_MSG_INVITATION_ACCEPTED)) {
                                try {
                                    URL serverURL = new URL("https://meet.jit.si");
                                    JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                                    builder.setServerURL(serverURL);
                                    builder.setWelcomePageEnabled(false);
                                    builder.setRoom(getIntent().getStringExtra(Consts.REMOTE_MSG_MEETING_ROOM));
                                    if (meetingType.equals("audio")) {
                                        builder.setVideoMuted(true);
                                    }
                                    JitsiMeetActivity.launch(IncomingActivity.this, builder.build());
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(IncomingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                Toast.makeText(IncomingActivity.this, "Rejected", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(IncomingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(IncomingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setListeners() {
        binding.ivInRejected.setOnClickListener(v -> {
            ringtone.stop();
            onBackPressed();
        });

        binding.ivInAccepted.setOnClickListener(v -> {
            ringtone.stop();
            sendInvitationResponse(
                    Consts.REMOTE_MSG_INVITATION_ACCEPTED,
                    getIntent().getStringExtra(Consts.REMOTE_MSG_INVITER_TOKEN)
            );
        });

        binding.ivInRejected.setOnClickListener(v -> {
            ringtone.stop();
            sendInvitationResponse(
                    Consts.REMOTE_MSG_INVITATION_REJECTED,
                    getIntent().getStringExtra(Consts.REMOTE_MSG_INVITER_TOKEN)
            );
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Consts.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Consts.REMOTE_MSG_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Consts.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        ringtone.stop();
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(invitationResponseReceiver);
    }
}