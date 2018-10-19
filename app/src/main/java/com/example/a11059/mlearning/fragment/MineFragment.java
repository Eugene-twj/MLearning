package com.example.a11059.mlearning.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.a11059.mlearning.R;
import com.example.a11059.mlearning.activity.LoginActivity;
import com.example.a11059.mlearning.activity.StudentMainActivity;
import com.example.a11059.mlearning.activity.TeacherMainActivity;
import com.example.a11059.mlearning.entity.Feedback;
import com.example.a11059.mlearning.entity.HistoryL;
import com.example.a11059.mlearning.entity.User;
import com.example.a11059.mlearning.utils.UtilDatabase;
import com.example.a11059.mlearning.utils.UtilFile;
import com.example.a11059.mlearning.utils.UtilNetwork;
import com.qmuiteam.qmui.widget.QMUICollapsingTopBarLayout;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by 11059 on 2018/7/21.
 */

public class MineFragment extends Fragment {

    public static final int MODIFY_PASSWD_SUCCESS = 1;

    public static final int MODIFY_PASSWD_FAIL = 0;

    private static final int TIP_TYPE_SUCCESS = 1;

    private static final int TIP_TYPE_FAIL = 0;

    private static final long DEFAULT_TIP_DURATION = 1000;

    public static final int MODIFY_NICKNAME = 2;

    public static final int MODIFY_PHONE = 3;

    public static final int MODIFY_ADDRESS = 4;

    public static final int RESET_PASSWORD_SUCCESS = 5;

    public static final int RESET_PASSWORD_FAIL = 6;

    private static final String REGEX_PHONE_NUMBER = "^1[0-9]{10}$";

    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    private User user;

    private View fragmentView;

    private EditText oldPasswd;

    private EditText newPasswd;

    private EditText confirmPasswd;

    private EditText userInfo;

    private QMUITipDialog tipDialog;

    private QMUIDialog modifyDialog;

    private StudentMainActivity parentActivityS;

    private TeacherMainActivity parentActivityT;

    private boolean modifyWaitingFlag = false;

    private QMUICommonListItemView userImage, nickName, phoneNumber, address;

    private QMUIRadiusImageView userLogo;

    private Uri cropImageUri;

    private String information = "";

    private static final int REQUEST_SELECT_FILE = 1;
    private String path = "";
    private static final int REQUEST_PERMISSION_STORAGE = 1;
    private static final int CROP_PICTURE = 2;

    public MyHandler handler = new MyHandler(this);

    public static class MyHandler extends Handler {

        private final WeakReference<MineFragment> mFragment;

        private MyHandler(MineFragment fragment){
            mFragment = new WeakReference<MineFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MineFragment fragment = mFragment.get();
            switch (msg.what){
                case MODIFY_PASSWD_SUCCESS:
                    fragment.modifySuccessAction(MODIFY_PASSWD_SUCCESS, "");
                    break;
                case MODIFY_PASSWD_FAIL:
                    fragment.modifyFailAction(MODIFY_PASSWD_FAIL);
                    break;
                case RESET_PASSWORD_SUCCESS:
                    fragment.tipDialog.dismiss();
                    fragment.resetPasswordSuccess();
                    break;
                case RESET_PASSWORD_FAIL:
                    fragment.tipDialog.dismiss();
                    fragment.resetPasswordFail();
                    break;
                case UtilDatabase.UPLOAD_USERLOGO_SUCCESS:
                    fragment.tipDialog.dismiss();
                    fragment.showTip(TIP_TYPE_SUCCESS, "上传成功", DEFAULT_TIP_DURATION);
                    fragment.userLogo.setImageURI(fragment.cropImageUri);
                    break;
                case UtilDatabase.UPLOAD_USERLOGO_FAIL:
                    fragment.tipDialog.dismiss();
                    fragment.showTip(TIP_TYPE_FAIL, "上传失败，请重试", DEFAULT_TIP_DURATION);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //parentActivity = (StudentMainActivity) getActivity();
        user = BmobUser.getCurrentUser(User.class);
        if(user == null){ //验证当前登录人是否存在
            parentActivityS = (StudentMainActivity) getActivity();
            LoginActivity.actionStart(getActivity(), false);
            parentActivityS.finish();
        }else {
            if(user.getIdentity().equals("student")){
                parentActivityS = (StudentMainActivity) getActivity();
            }else {
                parentActivityT = (TeacherMainActivity) getActivity();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_mine, container, false);
        initTopBar(fragmentView);
        initGroupList(fragmentView);
        return fragmentView;
    }

    private void initTopBar(View view){
        QMUICollapsingTopBarLayout collapsingTopBar = (QMUICollapsingTopBarLayout) view.findViewById(R.id.mine_collapsing_topbar);
        collapsingTopBar.setTitle(user.getName());
        userLogo = (QMUIRadiusImageView) view.findViewById(R.id.userImage);
        if(user.getImage() != null){
            String url = user.getImage().getUrl();
            Glide.with(MineFragment.this).load(url).into(userLogo);
        }else{

        }
    }

    private void initGroupList(View view){
        QMUIGroupListView groupList = (QMUIGroupListView) view.findViewById(R.id.mine_group_list);

        //section 1
        QMUICommonListItemView studentIdView = groupList.createItemView("学号");
        QMUICommonListItemView teacherIdView = groupList.createItemView("工号");
        studentIdView.setDetailText(user.getUsername());
        teacherIdView.setDetailText(user.getUsername());
        QMUICommonListItemView studentNameView = groupList.createItemView("姓名");
        studentNameView.setDetailText(user.getName());
        if (user.getIdentity().equals("student")){
            QMUIGroupListView.newSection(getContext())
                    .setTitle("基本信息")
                    .addItemView(studentIdView, null)
                    .addItemView(studentNameView, null)
                    .addTo(groupList);
        }else {
            QMUIGroupListView.newSection(getContext())
                    .setTitle("基本信息")
                    .addItemView(teacherIdView, null)
                    .addItemView(studentNameView, null)
                    .addTo(groupList);
        }


        //section 2
        userImage = groupList.createItemView("上传头像");
        userImage.setDetailText("点击上传");

        nickName = groupList.createItemView("昵称");
        nickName.setDetailText(user.getNickname());

        phoneNumber = groupList.createItemView("电话");
        phoneNumber.setDetailText(user.getMobilePhoneNumber());

        address = groupList.createItemView("邮箱");
        address.setDetailText(user.getEmail());

        QMUIGroupListView.newSection(getContext())
                .setTitle("个人资料")
                .addItemView(userImage, uploadUserImageListener())
                .addItemView(nickName, modifyUserInfoListener(MODIFY_NICKNAME))
                .addItemView(phoneNumber, modifyUserInfoListener(MODIFY_PHONE))
                .addItemView(address, modifyUserInfoListener(MODIFY_ADDRESS))
                .addTo(groupList);

        //section 3
        QMUICommonListItemView modifyPasswd = groupList.createItemView("修改密码");
        ImageView modifyIcon = new ImageView(getContext());
        modifyIcon.setImageResource(R.drawable.ic_modify_passwd);
        modifyPasswd.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        modifyPasswd.addAccessoryCustomView(modifyIcon);

        QMUICommonListItemView resetPasswd = groupList.createItemView("重置密码");
        ImageView resetIcon = new ImageView(getContext());
        resetIcon.setImageResource(R.drawable.ic_modify_passwd);
        resetPasswd.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        resetPasswd.addAccessoryCustomView(resetIcon);

        QMUIGroupListView.newSection(getContext())
                .setTitle("密码服务")
                .addItemView(modifyPasswd, getModifyPasswdListener())
                .addItemView(resetPasswd, resetPasswdListener())
                .addTo(groupList);

        //section 4
        QMUICommonListItemView clearHistory = groupList.createItemView("清除本地答题记录");
        ImageView clearIcon = new ImageView(getContext());
        clearIcon.setImageResource(R.drawable.ic_exit_login);
        clearHistory.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        clearHistory.addAccessoryCustomView(clearIcon);

        QMUICommonListItemView exitLogin = groupList.createItemView("退出登录");
        ImageView exitIcon = new ImageView(getContext());
        exitIcon.setImageResource(R.drawable.ic_exit_login);
        exitLogin.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        exitLogin.addAccessoryCustomView(exitIcon);
        if (user.getIdentity().equals("student")){
            QMUIGroupListView.newSection(getContext())
                    .setTitle("其他")
                    .addItemView(clearHistory, clearHistoryListener())
                    .addItemView(exitLogin, getExitLoginListener())
                    .addTo(groupList);
        }else {
            QMUIGroupListView.newSection(getContext())
                    .setTitle("其他")
                    .addItemView(exitLogin, getExitLoginListener())
                    .addTo(groupList);
        }

    }

    private View.OnClickListener uploadUserImageListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageAction();
            }
        };
    }

    private View.OnClickListener modifyUserInfoListener(final int type){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMofifyUserInfoDialog(type);
            }
        };
    }

    private View.OnClickListener getModifyPasswdListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyPasswdDialog();
            }
        };
    }

    private View.OnClickListener resetPasswdListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QMUIDialog.MessageDialogBuilder(getContext())
                        .setTitle("提示")
                        .setMessage("确认使用邮箱重置密码？")
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                                showLoadingTip("密码重置中");
                                UtilDatabase.resetPassword(MineFragment.this);
                            }
                        })
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        };
    }

    private View.OnClickListener clearHistoryListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QMUIDialog.MessageDialogBuilder(getContext())
                        .setTitle("提示")
                        .setMessage("确认删除所有本地记录？")
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                                showLoadingTip("正在清除，请稍后");
                                DataSupport.deleteAll(HistoryL.class, "username = ?", user.getUsername());
                                fragmentView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tipDialog.dismiss();
                                        showTip(TIP_TYPE_SUCCESS, "清除完成", DEFAULT_TIP_DURATION);
                                    }
                                }, 2000);
                            }
                        })
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        };
    }

    private View.OnClickListener getExitLoginListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitConfirmDialog();
            }
        };
    }

    private void showMofifyUserInfoDialog(final int type){
        if(!UtilNetwork.isNetworkAvailable()){
            showTip(TIP_TYPE_FAIL, "网络不可用", DEFAULT_TIP_DURATION);
            return;
        }
        String title = "";
        switch(type){
            case MODIFY_NICKNAME:
                title = "修改昵称";
                break;
            case MODIFY_PHONE:
                title = "修改手机号码";
                break;
            case MODIFY_ADDRESS:
                title = "修改邮箱";
                break;
        }
        modifyDialog = new QMUIDialog.CustomDialogBuilder(getContext())
                .setLayout(R.layout.dialog_modify_userinfo)
                .setTitle(title)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        confirmTextFormat(type);
                        modifyDialog.dismiss();
                    }
                })
                .create();
        userInfo = (EditText) modifyDialog.findViewById(R.id.user_info);
        switch(type){
            case MODIFY_NICKNAME:
                userInfo.setHint("请输入昵称");
                break;
            case MODIFY_PHONE:
                userInfo.setHint("请输入手机号");
                break;
            case MODIFY_ADDRESS:
                userInfo.setHint("请输入邮箱");
                break;
        }
        modifyDialog.show();
    }

    private void showModifyPasswdDialog(){
        if(!UtilNetwork.isNetworkAvailable()){
            showTip(TIP_TYPE_FAIL, "网络不可用", DEFAULT_TIP_DURATION);
            return;
        }
        modifyDialog = new QMUIDialog.CustomDialogBuilder(getContext())
                .setLayout(R.layout.dialog_modify_passwd)
                .setTitle("修改密码")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        if(!modifyWaitingFlag){ //防止多次响应点击事件
                            modifyWaitingFlag = true;
                            confirmModifyPasswdAction();
                        }
                    }
                })
                .create();
        oldPasswd = (EditText) modifyDialog.findViewById(R.id.old_passwd);
        newPasswd = (EditText) modifyDialog.findViewById(R.id.new_passwd);
        confirmPasswd = (EditText) modifyDialog.findViewById(R.id.confirm_passwd);
        modifyDialog.show();
    }

    private void confirmTextFormat(int type){
        if(!UtilNetwork.isNetworkAvailable()){
            modifyWaitingFlag = false;
            showTip(TIP_TYPE_FAIL, "网络不可用", DEFAULT_TIP_DURATION);
            return;
        }
        String info = "";
        info = userInfo.getText().toString();
        if(info.equals("")){
            showTip(TIP_TYPE_FAIL, "输入信息不能为空", DEFAULT_TIP_DURATION);
            return;
        }else {

            switch (type){
                case MODIFY_PHONE:
                    if(!Pattern.matches(REGEX_PHONE_NUMBER, info)){
                        showTip(TIP_TYPE_FAIL, "手机号码有误，请重新输入", DEFAULT_TIP_DURATION);
                        return;
                    }
                    break;
                case MODIFY_ADDRESS:
                    if(!Pattern.matches(REGEX_EMAIL, info)){
                        showTip(TIP_TYPE_FAIL, "邮箱有误，请重新输入", DEFAULT_TIP_DURATION);
                        return;
                    }
                    break;
            }

        }
        showLoadingTip("正在修改，请稍后");
        switch (type){
            case MODIFY_NICKNAME:
                modifyUserNickName(type, info);
                break;
            case MODIFY_PHONE:
                modifyUserPhone(type, info);
                break;
            case MODIFY_ADDRESS:
                modifyUserAddress(type, info);
                break;
        }
    }

    private void confirmModifyPasswdAction(){
        if(!UtilNetwork.isNetworkAvailable()){
            modifyWaitingFlag = false;
            showTip(TIP_TYPE_FAIL, "网络不可用", DEFAULT_TIP_DURATION);
            return;
        }
        String oldP = oldPasswd.getText().toString();
        String newP = newPasswd.getText().toString();
        if(!isPasswdCheck(oldP)){
            modifyWaitingFlag = false;
            showTip(TIP_TYPE_FAIL, "原密码格式有误", DEFAULT_TIP_DURATION);
            return;
        }
        if(!isPasswdCheck(newP)){
            modifyWaitingFlag = false;
            showTip(TIP_TYPE_FAIL, "新密码格式有误", DEFAULT_TIP_DURATION);
            return;
        }
        if(!isNotSamePasswdCheck()){
            modifyWaitingFlag = false;
            showTip(TIP_TYPE_FAIL, "新密码与原密码相同", DEFAULT_TIP_DURATION);
            return;
        }
        if(!isConfirmPasswdCheck()){
            modifyWaitingFlag = false;
            showTip(TIP_TYPE_FAIL, "两次密码输入不一致", DEFAULT_TIP_DURATION);
            return;
        }
        //密码初次验证完毕，进行修改操作
        UtilDatabase.modifyPasswd(this, oldP, newP);
    }

    //验证密码位数
    private boolean isPasswdCheck(String passwd){
        return passwd.length() >= 6;
    }

    //验证新旧密码是否相同
    private boolean isNotSamePasswdCheck(){
        return !oldPasswd.getText().toString().equals(newPasswd.getText().toString());
    }

    //验证两次新密码是否一致
    private boolean isConfirmPasswdCheck(){
        return newPasswd.getText().toString().equals(confirmPasswd.getText().toString());
    }

    private void modifyUserNickName(final int type, final String info){
        User newUser = new User();
        newUser.setNickname(info);
        newUser.update(user.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    modifyFeedback(type, info);
                } else {
                    modifyFailAction(type);
                }
            }
        });
    }

    private void modifyUserPhone(final int type, final String info){
        BmobUser newUser = new BmobUser();
        newUser.setMobilePhoneNumber(info);
        newUser.update(user.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    modifySuccessAction(type, info);
                } else {
                    modifyFailAction(type);
                }
            }
        });
    }

    private void modifyUserAddress(final int type, final String info){
        BmobUser newUser = new BmobUser();
        newUser.setEmail(info);
        newUser.update(user.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    modifySuccessAction(type, info);
                } else {
                    modifyFailAction(type);
                }
            }
        });
    }


    private void modifySuccessAction(int type, String info){
        if(modifyDialog != null){
            modifyDialog.dismiss();
        }
        switch (type){
            case MODIFY_NICKNAME:
                tipDialog.dismiss();
                nickName.setDetailText(info);
                showTip(TIP_TYPE_SUCCESS, "昵称修改成功", DEFAULT_TIP_DURATION);
                break;
            case MODIFY_PHONE:
                phoneNumber.setDetailText(info);
                tipDialog.dismiss();
                showTip(TIP_TYPE_SUCCESS, "手机号码修改成功", DEFAULT_TIP_DURATION);
                break;
            case MODIFY_ADDRESS:
                address.setDetailText(info);
                tipDialog.dismiss();
                showTip(TIP_TYPE_SUCCESS, "邮箱修改成功", DEFAULT_TIP_DURATION);
                break;
            case MODIFY_PASSWD_SUCCESS:
                showTip(TIP_TYPE_SUCCESS, "密码修改成功", DEFAULT_TIP_DURATION);
                LoginActivity.actionStart(getActivity(), true);
                if(user.getIdentity().equals("student")){
                    parentActivityS.finish();
                }else {
                    parentActivityT.finish();
                }

                break;
        }
    }

    private void modifyFailAction(int type){
        modifyWaitingFlag = false;
        switch (type){
            case MODIFY_NICKNAME:
                tipDialog.dismiss();
                showTip(TIP_TYPE_FAIL, "昵称修改失败，请重试", DEFAULT_TIP_DURATION);
                break;
            case MODIFY_PHONE:
                tipDialog.dismiss();
                showTip(TIP_TYPE_FAIL, "手机号码修改失败，请重试", DEFAULT_TIP_DURATION);
                break;
            case MODIFY_ADDRESS:
                tipDialog.dismiss();
                showTip(TIP_TYPE_FAIL, "邮箱修改失败，请重试", DEFAULT_TIP_DURATION);
                break;
            case MODIFY_PASSWD_FAIL:
                showTip(TIP_TYPE_FAIL, "原密码有误，请重试", DEFAULT_TIP_DURATION);
                break;
        }
    }

    private void showTip(int type, String tipWord, long duration){
        QMUITipDialog.Builder tipBuilder = null;
        if(user.getIdentity().equals("student")){
            tipBuilder = new QMUITipDialog.Builder(parentActivityS);
        }else {
            tipBuilder = new QMUITipDialog.Builder(parentActivityT);
        }

        if(type == TIP_TYPE_SUCCESS){
            tipBuilder = tipBuilder.setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS);
        } else if(type == TIP_TYPE_FAIL){
            tipBuilder = tipBuilder.setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL);
        }
        tipBuilder = tipBuilder.setTipWord(tipWord);
        tipDialog = tipBuilder.create();
        tipDialog.show();
        fragmentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                tipDialog.dismiss();
            }
        }, duration);
    }

    private void showExitConfirmDialog(){
        if (user.getIdentity().equals("student")){
            QMUIDialog.CheckBoxMessageDialogBuilder Sbuilder = null;
            Sbuilder = new QMUIDialog.CheckBoxMessageDialogBuilder(parentActivityS);
            Sbuilder.setTitle("是否退出当前账号？")
                    .setMessage("清除此账号本地练习记录")
                    .setChecked(false);
            final QMUIDialog.CheckBoxMessageDialogBuilder finalBuilder = Sbuilder;
            Sbuilder.addAction("取消", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    dialog.dismiss();
                }
            })
                    .addAction(0, "退出", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            if(finalBuilder.isChecked()){
                                //清除本地练习记录
                                DataSupport.deleteAll(HistoryL.class, "username = ?", user.getUsername());
                            }
                            BmobUser.logOut();//退出当前用户
                            LoginActivity.actionStart(getActivity(), false);
//                            if(user.getIdentity().equals("student")){
                                parentActivityS.finish();
//                            }else {
//                                parentActivityT.finish();
//                            }
                        }
                    });
            Sbuilder.create().show();
        }else {
            QMUIDialog.CheckBoxMessageDialogBuilder Tbuilder = null;
            Tbuilder = new QMUIDialog.CheckBoxMessageDialogBuilder(parentActivityT);
            Tbuilder.setTitle("是否退出当前账号？")
                    .setMessage("清除此账号缓存数据")
                    .setChecked(false);
            final QMUIDialog.CheckBoxMessageDialogBuilder finalBuilder = Tbuilder;
            Tbuilder.addAction("取消", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    dialog.dismiss();
                }
            })
                    .addAction(0, "退出", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            if(finalBuilder.isChecked()){
                                //清除本地练习记录
                                DataSupport.deleteAll(HistoryL.class, "username = ?", user.getUsername());
                            }
                            BmobUser.logOut();//退出当前用户
                            LoginActivity.actionStart(getActivity(), false);
//                            if(user.getIdentity().equals("student")){
//                            parentActivityS.finish();
//                            }else {
                                parentActivityT.finish();
//                            }
                        }
                    });
            Tbuilder.create().show();
        }

    }

    private void showLoadingTip(String tip){
        tipDialog = new QMUITipDialog.Builder(getContext())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(tip)
                .create(false);
        tipDialog.show();
    }

    private void resetPasswordSuccess(){
        showTip(TIP_TYPE_SUCCESS, "邮件已发送至您的邮箱，请进入邮箱重置您的密码。", DEFAULT_TIP_DURATION);
    }

    private void resetPasswordFail(){
        showTip(TIP_TYPE_FAIL, "使用邮箱重置密码失败，请重试。", DEFAULT_TIP_DURATION);
    }

    private void selectImageAction(){
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        } else {
            switch (requestCode) {
                case REQUEST_SELECT_FILE:
                    resizeImage(data.getData());
                    break;
                case CROP_PICTURE:
                    uploadUserImage();
                    break;
            }
        }

    }

    public void resizeImage(Uri uri) {
        String u = uri.toString().split("/")[uri.toString().split("/").length - 1];
        File CropPhoto=new File(getContext().getExternalCacheDir(),"crop_"+u+".jpg");
        try{
            if(CropPhoto.exists()){
                CropPhoto.delete();
            }
            CropPhoto.createNewFile();
        }catch(IOException e){
            e.printStackTrace();
        }
        cropImageUri=Uri.fromFile(CropPhoto);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 10000);
        intent.putExtra("outputY", 10000);
        intent.putExtra("circleCrop", "true");
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, CROP_PICTURE);
    }

    private void uploadUserImage(){
        //先检测权限，有则继续，无则申请
        if(user.getIdentity().equals("student")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(parentActivityS, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(parentActivityS, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_PERMISSION_STORAGE);
                if(ActivityCompat.shouldShowRequestPermissionRationale(parentActivityS, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    showTip(TIP_TYPE_FAIL, "读写存储权限被拒绝", DEFAULT_TIP_DURATION);
                }
            }
            else{
                realUploadUserImage();
            }
        }else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(parentActivityT, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(parentActivityT, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_PERMISSION_STORAGE);
                if(ActivityCompat.shouldShowRequestPermissionRationale(parentActivityT, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    showTip(TIP_TYPE_FAIL, "读写存储权限被拒绝", DEFAULT_TIP_DURATION);
                }
            }
            else{
                realUploadUserImage();
            }
        }

    }

    private void realUploadUserImage(){
        String path = UtilFile.getRealFilePath(cropImageUri);
        showLoadingTip("上传中");
        UtilDatabase.uploadUserLogo(MineFragment.this, path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    realUploadUserImage();
                } else{
                    showTip(TIP_TYPE_FAIL, "读写存储权限被拒绝", DEFAULT_TIP_DURATION);
                }
                break;
            default:
                break;
        }
    }

    private void modifyFeedback(final int type, final String info){
        String username = user.getUsername();
        BmobQuery<Feedback> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.findObjects(new FindListener<Feedback>() {
            @Override
            public void done(List<Feedback> list, BmobException e) {
                if(e == null){
                    List<BmobObject> feedbacks = new ArrayList<>();
                    for(int i = 0; i < list.size(); i++){
                        Feedback feedback = new Feedback();
                        feedback.setObjectId(list.get(i).getObjectId());
                        feedback.setNickname(info);
                        feedbacks.add(feedback);
                    }
                    new BmobBatch().updateBatch(feedbacks).doBatch(new QueryListListener<BatchResult>() {
                        @Override
                        public void done(List<BatchResult> list, BmobException e) {
                            if(e == null){
                                int k = 0;
                                for(int i = 0; i < list.size(); i++){
                                    BatchResult result = list.get(i);
                                    BmobException ex =result.getError();
                                    if(ex == null){
                                        k++;
                                    }else {

                                    }
                                }
                                if(k == list.size()){
                                    nickName.setDetailText(user.getNickname());
                                    modifySuccessAction(type, info);
                                }else {
                                    modifyFeedback(type, info);
                                }
                            }else {
                                modifyFeedback(type, info);
                            }
                        }
                    });
                }else {
                    modifyFeedback(type, info);
                }
            }
        });
    }
}
