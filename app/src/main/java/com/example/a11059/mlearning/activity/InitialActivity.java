package com.example.a11059.mlearning.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a11059.mlearning.R;
import com.example.a11059.mlearning.entity.Class;
import com.example.a11059.mlearning.entity.Course;
import com.example.a11059.mlearning.entity.User;
import com.example.a11059.mlearning.utils.UtilDatabase;
import com.example.a11059.mlearning.utils.UtilNetwork;
import com.example.a11059.mlearning.utils.UtilString;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.example.a11059.mlearning.application.MyApplication.getContext;

public class InitialActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TIP_TYPE_SUCCESS = 1;

    private static final int TIP_TYPE_FAIL = 0;

    private static final long DEFAULT_TIP_DURATION = 1000;

    private static final String IDENTITY_TEACHER = "teacher";

    private static final String IDENTITY_STUDENT = "student";

    private QMUITipDialog tipDialog;

    private QMUILinearLayout initial_window;

    private EditText nickname, password, cPassword, mobileNumber, email;
    private TextView classInfo, courseInfo, c;

    private String userNickName, userPwd, userMobilPhone, userAddress, userClassId, userCourseId;

    private List<Class> classList = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();

    private User user = new User();

    public static void actionStart(Context context){
        Intent intent = new Intent(context, InitialActivity.class);
        context.startActivity(intent);
    }

    public MyHandler handler = new MyHandler(this);
    public static class MyHandler extends Handler{
        private WeakReference<InitialActivity> mactivity;

        public MyHandler(InitialActivity activity){
            mactivity = new WeakReference<InitialActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            InitialActivity activity = mactivity.get();
            switch (msg.what){
                case UtilDatabase.ERROR:
                    activity.showErrorTip();
                    break;
                case UtilDatabase.CLASS_INFO:
                    activity.classList = UtilDatabase.classList;
                    if(activity.classList.size() == 0){
                        activity.showErrorTip();
                        return;
                    }
                    activity.chooseClassInfo(activity.classList);
                    break;
                case UtilDatabase.COURSE_INFO:
                    activity.courseList = UtilDatabase.courseList;
                    if(activity.courseList.size() == 0){
                        activity.showErrorTip();
                        return;
                    }
                    activity.chooseCourseInfo(activity.courseList);
                    break;
                case UtilDatabase.INITIAL_SUCCESS:
                    activity.initialUserInfo(activity.userNickName, activity.userPwd, activity.userMobilPhone,activity.userAddress, activity.userClassId, activity.userCourseId);
                    break;
                case UtilDatabase.INITIAL_ERROR:
                    activity.showSubmitFailTip();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        QMUIStatusBarHelper.translucent(this);
        user = BmobUser.getCurrentUser(User.class);
        initWindow();
    }

    private void initWindow(){
        initial_window = (QMUILinearLayout) findViewById(R.id.initial_window);
        float mShadowAlpha = 0.45f;
        int mShadowElevation = QMUIDisplayHelper.dp2px(InitialActivity.this, 14);
        int mRadius = QMUIDisplayHelper.dp2px(InitialActivity.this, 5);
        initial_window.setRadiusAndShadow(mRadius, mShadowElevation, mShadowAlpha);
        initEdittext();
        initTextView();
        QMUIRoundButton login = (QMUIRoundButton) findViewById(R.id.btn_initial);
        login.setOnClickListener(this);
    }

    private void initEdittext(){
        nickname = (EditText) findViewById(R.id.nickname);
        password = (EditText) findViewById(R.id.password);
        cPassword = (EditText) findViewById(R.id.confirm_password);
        mobileNumber = (EditText) findViewById(R.id.mobilPhoneNumber);
        email = (EditText) findViewById(R.id.email);
        ImageView clrNickname = (ImageView) findViewById(R.id.clr_nickname);
        ImageView clrPassword = (ImageView) findViewById(R.id.clr_password);
        ImageView clrCPassword = (ImageView) findViewById(R.id.clr_confirm_password);
        ImageView clrMobilNumber = (ImageView) findViewById(R.id.clr_mobilPhoneNumber);
        ImageView clrEmail = (ImageView) findViewById(R.id.clr_email);
        clrNickname.setOnClickListener(this);
        clrPassword.setOnClickListener(this);
        clrCPassword.setOnClickListener(this);
        clrMobilNumber.setOnClickListener(this);
        clrEmail.setOnClickListener(this);
        initClrEditText(nickname, clrNickname);
        initClrEditText(password, clrPassword);
        initClrEditText(cPassword, clrCPassword);
        initClrEditText(mobileNumber, clrMobilNumber);
        initClrEditText(email, clrEmail);
    }

    private void initTextView(){
        c = (TextView)findViewById(R.id.c_c);
        classInfo = (TextView) findViewById(R.id.classInfo);
        courseInfo = (TextView) findViewById(R.id.courseInfo);
        if(user.getIdentity().equals(IDENTITY_TEACHER) ){
            c.setVisibility(View.GONE);
            classInfo.setVisibility(View.GONE);
            courseInfo.setVisibility(View.GONE);
        }else {
            classInfo.setOnClickListener(this);
            courseInfo.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clr_nickname:
                clrNicknameClickAction();
                break;
            case R.id.clr_password:
                clrPasswordClickAction();
                break;
            case R.id.clr_confirm_password:
                clrCPasswordClickAction();
                break;
            case R.id.clr_mobilPhoneNumber:
                clrMobilPhoneNumberClickAction();
                break;
            case R.id.clr_email:
                clrEmailClickAction();
                break;
            case R.id.classInfo:
                UtilDatabase.findClassInfo(this);

                break;
            case R.id.courseInfo:
                UtilDatabase.findCourseInfo(this);
                break;
            case R.id.btn_initial:
                btnInitialClickAction();
                break;
        }
    }

    private void clrNicknameClickAction(){
        nickname.setText("");
    }

    private void clrPasswordClickAction(){
        password.setText("");
    }

    private void clrCPasswordClickAction(){
        cPassword.setText("");
    }

    private void clrMobilPhoneNumberClickAction(){mobileNumber.setText("");}

    private void clrEmailClickAction(){email.setText("");}

    private void chooseClassInfo(List<Class> classes){
        final String[] clInfo = new String[classes.size()];
        for(int i = 0; i < classes.size(); i++){
            clInfo[i] = classes.get(i).getId() + " " + classes.get(i).getName();
        }
        new QMUIDialog.MenuDialogBuilder(InitialActivity.this)
                .addItems(clInfo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        classInfo.setText(clInfo[which]);
                    }
                })
                .create()
                .show();
    }

    private void chooseCourseInfo(List<Course> courses){
        final String coInfo[] = new String[courses.size()];
        for(int i = 0; i < courses.size(); i++){
            coInfo[i] = courses.get(i).getId() + " " + courses.get(i).getName();
        }
        new QMUIDialog.MenuDialogBuilder(InitialActivity.this)
                .addItems(coInfo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        courseInfo.setText(coInfo[which]);
                    }
                })
                .create()
                .show();
    }

    private void initClrEditText(final EditText e, final ImageView i){
        i.setVisibility(View.GONE);
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    i.setVisibility(View.VISIBLE);
                }
                else{
                    i.setVisibility(View.GONE);
                }
            }
        });
    }

    private void btnInitialClickAction() {
        if (!UtilNetwork.isNetworkAvailable()) {
            showTip(TIP_TYPE_FAIL, "网络不可用", DEFAULT_TIP_DURATION);
            return;
        }
        final String nickName = nickname.getText().toString();
        String pwd = password.getText().toString();
        //String cPwd = cPassword.getText().toString();
        String mobilPhone = mobileNumber.getText().toString();
        String address = email.getText().toString();
        if (!UtilString.isNotBlankString(nickName)) {
            showTip(TIP_TYPE_FAIL, "学号不能为空", DEFAULT_TIP_DURATION);
            return;
        }
        if (!isPasswdCheck(pwd)) {
            showTip(TIP_TYPE_FAIL, "密码不可少于6位", DEFAULT_TIP_DURATION);
            return;
        }
        if (!isConfirmPasswdCheck()) {
            showTip(TIP_TYPE_FAIL, "两次密码输入不一致", DEFAULT_TIP_DURATION);
            return;
        }
        if (!UtilString.isNotBlankString(mobilPhone)) {
            showTip(TIP_TYPE_FAIL, "手机号码不能为空", DEFAULT_TIP_DURATION);
            return;
        }
        if(!isPhoneCheck(mobilPhone)){
            showTip(TIP_TYPE_FAIL, "手机号码为11位", DEFAULT_TIP_DURATION);
        }
        if (!UtilString.isNotBlankString(address)) {
            showTip(TIP_TYPE_FAIL, "邮箱地址不能为空", DEFAULT_TIP_DURATION);
            return;
        }
        if(user.getIdentity().equals(IDENTITY_STUDENT)){
            final String classId = classInfo.getText().toString().split(" ")[0];
            final String courseId = courseInfo.getText().toString().split(" ")[0];
            if (!UtilString.isNotBlankString(classId)) {
                showTip(TIP_TYPE_FAIL, "请选择班级", DEFAULT_TIP_DURATION);
                return;
            }
            if (!UtilString.isNotBlankString(courseId)) {
                showTip(TIP_TYPE_FAIL, "请选择课程", DEFAULT_TIP_DURATION);
                return;
            }
            showLoadingTip();
            userNickName = nickName;
            userPwd = pwd;
            userMobilPhone = mobilPhone;
            userAddress = address;
            userClassId = classId;
            userCourseId = courseId;
            UtilDatabase.initFeedBack(InitialActivity.this, Integer.parseInt(courseId), classId, nickName);
        }else {
            showLoadingTip();
            userNickName = nickName;
            userPwd = pwd;
            userMobilPhone = mobilPhone;
            userAddress = address;
            initialTeacherInfo(userNickName, userPwd, userMobilPhone, userAddress);
        }



    }

    private void initialUserInfo(String nickName, String pwd, String mobilPhone, String address, String classId, String courseId){
        User user = BmobUser.getCurrentUser(User.class);
        user.setNickname(nickName);
        user.setPassword(pwd);
        user.setMobilePhoneNumber(mobilPhone);
        user.setEmail(address);
        user.setClassId(classId);
        user.setCourseId(Integer.parseInt(courseId));
        user.setFirstTime(1);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    showSubmitSuccDialog();
                }
                else{
                    showSubmitFailTip();
                }
            }
        });
    }

    private void initialTeacherInfo(String nickName, String pwd, String mobilPhone, String address){
        User user = BmobUser.getCurrentUser(User.class);
        user.setNickname(nickName);
        user.setPassword(pwd);
        user.setMobilePhoneNumber(mobilPhone);
        user.setEmail(address);
        user.setFirstTime(1);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    showSubmitSuccDialog();
                }
                else{
                    showSubmitFailTip();
                }
            }
        });
    }

    //验证密码位数
    private boolean isPasswdCheck(String passwd){
        return passwd.length() >= 6;
    }

    //验证两次新密码是否一致
    private boolean isConfirmPasswdCheck(){
        return password.getText().toString().equals(cPassword.getText().toString());
    }

    //验证手机号码的位数
    private boolean isPhoneCheck(String phoneNumber){return phoneNumber.length() == 11;}

    private void showSubmitSuccDialog(){
        tipDialog.dismiss();
        showTip(TIP_TYPE_SUCCESS, "信息初始化成功", DEFAULT_TIP_DURATION);
        User bmobUser = BmobUser.getCurrentUser(User.class);

        String identity = bmobUser.getIdentity();
        if(identity.equals(IDENTITY_STUDENT)){
            startStudentMainActivity();
        } else if(identity.equals(IDENTITY_TEACHER)){
            startTeacherMainActivity();
        }
    }

    private void startStudentMainActivity(){
        StudentMainActivity.actionStart(InitialActivity.this);
        finish();
    }

    private void startTeacherMainActivity(){
        TeacherMainActivity.actionStart(InitialActivity.this);
        finish();
    }

    private void showSubmitFailTip(){
        tipDialog.dismiss();
        showTip(TIP_TYPE_FAIL, "信息初始化失败，请重试", DEFAULT_TIP_DURATION);
    }

    private void showLoadingTip(){
        tipDialog = new QMUITipDialog.Builder(InitialActivity.this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("信息初始化中...")
                .create(false);
        tipDialog.show();
    }

    private void showErrorTip(){
        if(!UtilNetwork.isNetworkAvailable()){
            showTip(TIP_TYPE_FAIL, "网络异常，请确认网络畅通后重试。", DEFAULT_TIP_DURATION);
        } else {
            showTip(TIP_TYPE_FAIL, "未获取到相关信息，请重试。", DEFAULT_TIP_DURATION);
        }
    }

    private void showTip(int type, String tipWord, long duration){
        QMUITipDialog.Builder tipBuilder = new QMUITipDialog.Builder(InitialActivity.this);
        if(type == TIP_TYPE_SUCCESS){
            tipBuilder = tipBuilder.setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS);
        } else if(type == TIP_TYPE_FAIL){
            tipBuilder = tipBuilder.setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL);
        }
        tipBuilder = tipBuilder.setTipWord(tipWord);
        tipDialog = tipBuilder.create();
        tipDialog.show();
        if(type == TIP_TYPE_FAIL){
            initial_window.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tipDialog.dismiss();
                }
            }, duration);
        }
    }

}
