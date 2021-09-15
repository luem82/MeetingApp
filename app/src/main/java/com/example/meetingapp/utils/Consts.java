package com.example.meetingapp.utils;

import java.util.HashMap;

public class Consts {

    public static final String KEY_PREFERENCE_NAME = "MeetingAppReference";

    public static final String KEY_COLLECTION_MEMBER = "members";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_USER_NAME = "userName";
    public static final String REMOTE_MSG_USER_ID = "user_Id";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                REMOTE_MSG_AUTHORIZATION,
                "key=AAAAlhf4bl8:APA91bHSAaGVXJ-uc7sxxw0RfrtEUD1T-v35yp3aWFh-zV5E1yv_CtOz8DU8TI-RtdwU5VbQ3hrMP3ES3sXZR8XGmQj6-1x0MU9ECAZpgCl30Gb2uLzFafXEpzWsjiN-gdSKXUmbOtTS"
        );
        headers.put(
                REMOTE_MSG_CONTENT_TYPE,
                "application/json"
        );
        return headers;
    }
}
