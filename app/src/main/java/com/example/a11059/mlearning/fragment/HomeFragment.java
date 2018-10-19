package com.example.a11059.mlearning.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.bumptech.glide.util.Util;
import com.example.a11059.mlearning.R;
import com.example.a11059.mlearning.activity.TeacherMainActivity;
import com.example.a11059.mlearning.adapter.QuizRvAdapter;
import com.example.a11059.mlearning.entity.Problem;
import com.example.a11059.mlearning.entity.User;
import com.example.a11059.mlearning.utils.UtilDatabase;
import com.qmuiteam.qmui.widget.QMUIEmptyView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private View fragmentView;

    private TeacherMainActivity parentActivity;

    private QMUIEmptyView emptyView;

    private RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;

    private List<Problem> problemList = new ArrayList<>();

    public MyHandler handler = new MyHandler(this);

    public static class MyHandler extends Handler {

        private final WeakReference<HomeFragment> mFragment;

        private MyHandler(HomeFragment fragment){
            mFragment = new WeakReference<HomeFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            HomeFragment fragment = mFragment.get();
            switch (msg.what){
                case UtilDatabase.PROBLEM_INFO:
                    fragment.problemList = UtilDatabase.problemList;
                    if (fragment.problemList.size() == 0){
                        fragment.emptyView.show("未获取到留言", "没有未回复的留言");
                    }else {
                        fragment.emptyView.hide();
                        fragment.resetData();
                    }
                    break;
                case UtilDatabase.ERROR_PROBLEM:
                    fragment.emptyView.show("未获取到留言", "没有未回复的留言");
                    break;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (TeacherMainActivity) getActivity();
        User user = BmobUser.getCurrentUser(User.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, container, false);
        initTopBar(fragmentView);
        initEmptyView(fragmentView);
        initGroupList(fragmentView);
        initRecyclerView(fragmentView);
        return fragmentView;
    }

    private void initTopBar(View view) {
        QMUITopBar mTopBar = view.findViewById(R.id.home_topbar);
        mTopBar.setTitle("主页");
    }

    private void initEmptyView(View view){
        emptyView = (QMUIEmptyView) view.findViewById(R.id.emptyView);
        emptyView.hide();
    }

    private void initGroupList(View view){
        QMUIGroupListView groupList = (QMUIGroupListView) view.findViewById(R.id.info_group_list);

        //section1
        QMUICommonListItemView studentInfoView = groupList.createItemView("浏览学生信息");
        studentInfoView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        QMUICommonListItemView resourceView = groupList.createItemView("浏览课程资源");
        resourceView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        QMUICommonListItemView questionView = groupList.createItemView("浏览题目信息");
        questionView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        QMUIGroupListView.newSection(getContext())
                .setTitle("信息浏览")
                .addItemView(studentInfoView, studentInfoViewListener())
                .addItemView(resourceView, resourceViewListener())
                .addItemView(questionView, questionViewListener())
                .addTo(groupList);

        //section2
        QMUICommonListItemView problemsView = groupList.createItemView("查看问题并解答");
        problemsView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
        problemsView.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    emptyView.show(true);
                    UtilDatabase.findProblemInfo(HomeFragment.this);
                }else {
                    emptyView.show(false);
                }

            }
        });
        QMUIGroupListView.newSection(getContext())
                .setTitle("问答")
                .addItemView(problemsView, null)
                .addTo(groupList);
    }

    private View.OnClickListener studentInfoViewListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilDatabase.findStudentsInfo(HomeFragment.this);
            }
        };
    }

    private View.OnClickListener resourceViewListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilDatabase.findResourceInfo(HomeFragment.this);
            }
        };
    }

    private View.OnClickListener questionViewListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilDatabase.findQuestionInfo(HomeFragment.this);
            }
        };
    }

    private void initRecyclerView(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.problem_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void resetData(){
        QuizRvAdapter adapter = new QuizRvAdapter(problemList);
        recyclerView.setAdapter(adapter);
    }




    @Override
    public void onClick(View view) {

    }
}
