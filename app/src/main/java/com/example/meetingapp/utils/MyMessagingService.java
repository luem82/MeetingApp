package com.example.meetingapp.utils;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.meetingapp.activities.IncomingActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class MyMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull @NotNull String token) {
        super.onNewToken(token);
//        Log.d("fcm", "Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Log.d("fcm", "RemoteMessage : " + remoteMessage.getNotification().getBody());

        String type = remoteMessage.getData().get(Consts.REMOTE_MSG_TYPE);
        if (type != null) {
            if (type.equals(Consts.REMOTE_MSG_INVITATION)) {
                Intent intent = new Intent(this, IncomingActivity.class);
                intent.putExtra(
                        Consts.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Consts.REMOTE_MSG_MEETING_TYPE)
                );
                intent.putExtra(
                        Consts.REMOTE_MSG_USER_NAME,
                        remoteMessage.getData().get(Consts.REMOTE_MSG_USER_NAME)
                );
                intent.putExtra(
                        Consts.REMOTE_MSG_USER_ID,
                        remoteMessage.getData().get(Consts.REMOTE_MSG_USER_ID)
                );
                intent.putExtra(
                        Consts.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Consts.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        Consts.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Consts.REMOTE_MSG_MEETING_ROOM)
                );

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals(Consts.REMOTE_MSG_INVITATION_RESPONSE)) {
                Intent intent = new Intent(Consts.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Consts.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Consts.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
