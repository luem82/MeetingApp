package com.example.meetingapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetingapp.R;
import com.example.meetingapp.databinding.ItemUserBinding;
import com.example.meetingapp.models.User;
import com.example.meetingapp.utils.MyBitmap;
import com.example.meetingapp.utils.UserListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> {

    private List<User> userList;
    private UserListener userListener;

    public UserAdapter(List<User> userList, UserListener userListener) {
        this.userList = userList;
        this.userListener = userListener;
    }

    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemUserBinding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UserVH(itemUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserVH holder, int position) {
        holder.setData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserVH extends RecyclerView.ViewHolder {

        ItemUserBinding binding;

        public UserVH(@NonNull ItemUserBinding itemUserBinding) {
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }

        public void setData(User user) {
            binding.tvUserName.setText(user.name);
            binding.ivUserAvatar.setImageBitmap(MyBitmap.getBitmapFromStringEndcode(user.photo));

            binding.getRoot().setOnClickListener(v -> {
                openPopupMenu(v, user);
            });
        }
    }

    @SuppressLint("RestrictedApi")
    private void openPopupMenu(View view, User user) {
        Context context = view.getContext();
        MenuBuilder menuBuilder = new MenuBuilder(context);
        MenuInflater inflater = new MenuInflater(context);
        inflater.inflate(R.menu.menu_call_options, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, view, true, R.attr.actionOverflowMenuStyle);
        optionsMenu.setForceShowIcon(true);

        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull @NotNull MenuBuilder menu, @NonNull @NotNull MenuItem item) {
                if (item.getItemId() == R.id.audio_call) {
                    userListener.onAudioClicked(user);
                    return true;
                } else if (item.getItemId() == R.id.video_call) {
                    userListener.onVideoClicked(user);
                    return true;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(@NonNull @NotNull MenuBuilder menu) {
            }
        });

        optionsMenu.show();
    }
}
