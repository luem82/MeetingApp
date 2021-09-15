package com.example.meetingapp.utils;

import com.example.meetingapp.models.User;

public interface UserListener {

    void onAudioClicked(User user);
    void onVideoClicked(User user);
}
