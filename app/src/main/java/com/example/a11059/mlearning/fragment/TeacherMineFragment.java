package com.example.a11059.mlearning.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a11059.mlearning.R;
import com.example.a11059.mlearning.activity.TeacherMainActivity;
import com.example.a11059.mlearning.entity.User;

import cn.bmob.v3.BmobUser;

public class TeacherMineFragment extends Fragment {

    private View fragmentView;

    private TeacherMainActivity parentActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (TeacherMainActivity) getActivity();
        User user = BmobUser.getCurrentUser(User.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_teacher, container, false);
        return fragmentView;
    }
}
