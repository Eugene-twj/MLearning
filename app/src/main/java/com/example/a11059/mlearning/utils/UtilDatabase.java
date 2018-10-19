package com.example.a11059.mlearning.utils;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.example.a11059.mlearning.activity.InitialActivity;
import com.example.a11059.mlearning.activity.LoginActivity;
import com.example.a11059.mlearning.activity.QuestionActivity;
import com.example.a11059.mlearning.activity.ResourceActivity;
import com.example.a11059.mlearning.activity.StudentMainActivity;
import com.example.a11059.mlearning.activity.StudentStatisticActivity;
import com.example.a11059.mlearning.entity.Class;
import com.example.a11059.mlearning.entity.Course;
import com.example.a11059.mlearning.entity.Examination;
import com.example.a11059.mlearning.entity.Feedback;
import com.example.a11059.mlearning.entity.Knowledge;
import com.example.a11059.mlearning.entity.Problem;
import com.example.a11059.mlearning.entity.Question;
import com.example.a11059.mlearning.entity.Resource;
import com.example.a11059.mlearning.entity.Statistic;
import com.example.a11059.mlearning.entity.Unit;
import com.example.a11059.mlearning.entity.User;
import com.example.a11059.mlearning.fragment.HomeFragment;
import com.example.a11059.mlearning.fragment.LearnFragment;
import com.example.a11059.mlearning.fragment.MineFragment;
import com.example.a11059.mlearning.fragment.QuizFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * Created by 11059 on 2018/7/16.
 */

public class UtilDatabase {

    public static final int ERROR = 0;
    public static final int CLASS_INFO = 1;
    public static final int COURSE_INFO = 2;
    public static final int QUESTION_NUM_INFO = 3;
    public static final int QUESTION_INFO = 4;
    public static final int UNIT_INFO = 5;
    public static final int ERRO_UNIT = 6;
    public static final int EXAM_INFO = 7;
    public static final int ERROR_EXAM = 8;
    public static final int COURSE_NAME = 9;
    public static final int RESET_PASSWORD_SUCCESS = 10;
    public static final int RESET_PASSWORD_FAIL = 11;
    public static final int UPLOAD_USERLOGO_SUCCESS = 12;
    public static final int UPLOAD_USERLOGO_FAIL = 13;
    public static final int UNITS_FIND = 14;
    public static final int ERROR_UNIT_LOADED = 15;
    public static final int GET_SCHEDULE = 16;
    public static final int GET_PROGRAM = 17;
    public static final int GET_EXPERIMENT = 18;
    public static final int GET_TIME = 19;
    public static final int PROBLEM_INFO = 20;
    public static final int ERROR_PROBLEM = 21;
    public static final int ADD_PROBLEM_SUCCESS = 22;
    public static final int ADD_FAIL = 23;
    public static final int STATISTIC_UNIT = 24;
    public static final int STATISTIC_CLASS = 25;
    public static final int STATISTIC_ERROR = 26;
    public static final int INITIAL_SUCCESS = 27;
    public static final int INITIAL_ERROR = 28;
    public static final int ALL_RESOURCE_INFO = 29;
    public static final int ERROR_RESOURCES = 30;

    public static int questionNum = 0;

    public static List<Class> classList = new ArrayList<>();
    public static List<Course> courseList = new ArrayList<>();
    public static List<Question> questionList = new ArrayList<>();
    public static List<Unit> unitList = new ArrayList<>();
    public static List<Examination> examList = new ArrayList<>();
    public static List<Unit> unitsList = new ArrayList<>();
    public static List<List<Knowledge>> unitKnowledgeList = new ArrayList<>();
    public static List<Problem> problemList = new ArrayList<>();
    public static List<Statistic> statisticList = new ArrayList<>();
    public static List<Resource> resourceList = new ArrayList<>();

    public static String courseName = "";
    public static String schedule = "";
    public static String filePath = "";
    public static int k = 0;

    public static BmobFile bmobFile;

    static User user = BmobUser.getCurrentUser(User.class);

    public static void findClassInfo(final InitialActivity activity){
        BmobQuery<Class> query = new BmobQuery<>();
        query.findObjects(new FindListener<Class>() {
            @Override
            public void done(List<Class> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    classList = list;
                    message.what = CLASS_INFO;
                }else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findCourseInfo(final InitialActivity activity){
        BmobQuery<Course> query = new BmobQuery<>();
        query.findObjects(new FindListener<Course>() {
            @Override
            public void done(List<Course> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    courseList = list;
                    message.what = COURSE_INFO;
                }else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void initFeedBack(final InitialActivity activity, int courseId, final String classId, final String nickName){
        BmobQuery<Unit> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", courseId);
        query.findObjects(new FindListener<Unit>() {
            @Override
            public void done(List<Unit> list, BmobException e) {
                if (e == null) {
                    List<BmobObject> feedList = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++){
                        Feedback feedback = new Feedback();
                        feedback.setUsername(user.getUsername());
                        feedback.setNickname(nickName);
                        feedback.setUnitId(list.get(i).getId());
                        feedback.setClassId(classId);
                        feedback.setTotalNum(0);
                        feedback.setRightNum(0);
                        feedList.add(feedback);
                    }
                    new BmobBatch().insertBatch(feedList).doBatch(new QueryListListener<BatchResult>() {
                        @Override
                        public void done(List<BatchResult> list, BmobException e) {
                            Message message = new Message();
                            if (e == null){
                                for (int j = 0; j < list.size(); j++){
                                    BatchResult result = list.get(j);
                                    BmobException ex =result.getError();
                                    if(ex==null){
                                        k++;
                                    }else{

                                    }
                                }
                                if(k == list.size()){
                                    message.what = INITIAL_SUCCESS;
                                }
                            }else {
                                message.what = INITIAL_ERROR;
                            }
                            activity.handler.sendMessage(message);
                        }
                    });
                } else {

                }
            }
        });
    }

    public static void findCourseNameById(final LearnFragment fragment, int id){
        BmobQuery<Course> query = new BmobQuery<>();
        query.addWhereEqualTo("id", id);
        query.findObjects(new FindListener<Course>() {
            @Override
            public void done(List<Course> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    courseName = list.get(0).getName();
                    message.what = COURSE_NAME;
                }else{
                    message.what = ERROR;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findQuestionNum(final QuestionActivity activity){
        BmobQuery<Question> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.count(Question.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                Message message = new Message();
                if(e == null){
                    questionNum = integer;
                    message.what = QUESTION_NUM_INFO;
                }
                else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findQuestionSequence(final QuestionActivity activity, int skip, int num){
        questionList.clear();
        BmobQuery<Question> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.setLimit(num).setSkip(skip).findObjects(new FindListener<Question>() {
            @Override
            public void done(List<Question> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    questionList = list;
                    if(questionList.size() > 0){
                        message.what = QUESTION_INFO;
                    } else {
                        message.what = ERROR;
                    }
                }
                else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findUnit(final LearnFragment fragment){
        BmobQuery<Unit> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.findObjects(new FindListener<Unit>() {
            @Override
            public void done(List<Unit> list, BmobException e) {
                Message message = new Message();
                if (e == null) {
                    unitList = list;
                    message.what = UNIT_INFO;
                } else {
                    message.what = ERRO_UNIT;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findQuestionByUnitId(final QuestionActivity activity, int unitId){
        questionList.clear();
        BmobQuery<Question> query = new BmobQuery<>();
        //query.addWhereEqualTo("courseId", user.getCourseId());
        query.addWhereEqualTo("unitId", unitId);
        query.findObjects(new FindListener<Question>() {
            @Override
            public void done(List<Question> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    questionList = list;
                    if(questionList.size() > 0){
                        message.what = QUESTION_INFO;
                    } else {
                        message.what = ERROR;
                    }
                }
                else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findExam(final LearnFragment fragment){
        BmobQuery<Examination> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.order("difficulty").findObjects(new FindListener<Examination>() {
            @Override
            public void done(List<Examination> list, BmobException e) {
                Message message = new Message();
                if (e == null) {
                    examList = list;
                    message.what = EXAM_INFO;
                } else {
                    message.what = ERROR_EXAM;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findQuestionById(final QuestionActivity activity, String[] questionId){
        Integer[] id = new Integer[questionId.length];
        for(int i = 0; i < questionId.length; i++){
            id[i] = Integer.parseInt(questionId[i]);
        }
        questionList.clear();
        BmobQuery<Question> query = new BmobQuery<>();
        //query.addWhereEqualTo("courseId", user.getCourseId());
        query.addWhereContainedIn("id", Arrays.asList(id));
        query.findObjects(new FindListener<Question>() {
            @Override
            public void done(List<Question> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    questionList = list;
                    if(questionList.size() > 0){
                        message.what = QUESTION_INFO;
                    } else {
                        message.what = ERROR;
                    }
                }
                else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findQuestionByRandom(final QuestionActivity activity, final int[] randNum, int max){
        for(int j = 0; j < randNum.length; j++){
            Log.d("rand", randNum[j] + ",");
        }
        questionList.clear();
        BmobQuery<Question> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.setLimit(max).findObjects(new FindListener<Question>() {
            @Override
            public void done(List<Question> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    if(list.size() > 0){
                        for(int i = 0; i < randNum.length; i++){
                            questionList.add(list.get(randNum[i]));
                        }
                        message.what = QUESTION_INFO;
                    } else {
                        message.what = ERROR;
                    }
                }
                else{
                    message.what = ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });

    }

    public static void modifyPasswd(final MineFragment fragment, String oldPasswd, String newPasswd){
        BmobUser.updateCurrentUserPassword(oldPasswd, newPasswd, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Message message = new Message();
                if(e == null){
                    message.what = MineFragment.MODIFY_PASSWD_SUCCESS;
                } else {
                    message.what = MineFragment.MODIFY_PASSWD_FAIL;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void resetPassword(final MineFragment fragment){
        User user = BmobUser.getCurrentUser(User.class);
        String email = user.getEmail();
        BmobUser.resetPasswordByEmail(email, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Message message = new Message();
                if(e == null){
                    message.what = MineFragment.RESET_PASSWORD_SUCCESS;
                } else {
                    message.what = MineFragment.RESET_PASSWORD_FAIL;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void resetPasswordBeforeLogin(final LoginActivity activity, String email){
        BmobUser.resetPasswordByEmail(email, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Message message = new Message();
                if(e == null){
                    message.what = RESET_PASSWORD_SUCCESS;
                } else {
                    message.what = RESET_PASSWORD_FAIL;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void uploadUserLogo(final MineFragment fragment, String path){
        final User user = BmobUser.getCurrentUser(User.class);
        final BmobFile bmobFile = new BmobFile(new File(path));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                final Message message = new Message();
                if(e == null){
                    User newUser = new User();
                    newUser.setImage(bmobFile);
                    newUser.update(user.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e == null){
                                message.what = UPLOAD_USERLOGO_SUCCESS;
                            }else{
                                message.what = UPLOAD_USERLOGO_FAIL;
                            }
                            fragment.handler.sendMessage(message);
                        }
                    });
                    return;
                }else{
                    message.what = UPLOAD_USERLOGO_FAIL;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findUnits(final LearnFragment fragment){
        User user = BmobUser.getCurrentUser(User.class);
        BmobQuery<Unit> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.findObjects(new FindListener<Unit>() {
            @Override
            public void done(List<Unit> list, BmobException e) {
                Message message = new Message();
                if (e == null) {
                    unitsList = list;
                    findKnowledge(fragment);
                } else {
                    message.what = ERROR_UNIT_LOADED;
                    fragment.handler.sendMessage(message);
                }

            }
        });
    }

    public static void findKnowledge(final LearnFragment fragment){
        User user = BmobUser.getCurrentUser(User.class);
        BmobQuery<Knowledge> query = new BmobQuery<>();
        query.addWhereEqualTo("courseId", user.getCourseId());
        query.findObjects(new FindListener<Knowledge>() {
            @Override
            public void done(List<Knowledge> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    //knowledgeList = list;
                    unitKnowledgeList.clear();//必须清除，否则无法正确显示
                    for(Unit unit : unitsList){
                        List<Knowledge> moduleChapter = new ArrayList<>();
                        for (Knowledge knowledge : list){
                            if (unit.getId().equals(knowledge.getUnitId())){
                                moduleChapter.add(knowledge);
                            }
                        }
                        unitKnowledgeList.add(moduleChapter);
                    }
                    message.what = UNITS_FIND;
                }
                else{
                    message.what = ERROR_UNIT_LOADED;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findSchedule(final LearnFragment fragment){
        User user = BmobUser.getCurrentUser(User.class);
        String classId = user.getClassId();
        BmobQuery<Class> query = new BmobQuery<>();
        query.addWhereEqualTo("id", classId);
        query.findObjects(new FindListener<Class>() {
            @Override
            public void done(List<Class> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    schedule = list.get(0).getSchedule();
                    message.what = GET_SCHEDULE;
                }else{
                    message.what = ERROR;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findCourseInfo(final LearnFragment fragment, final String type){
        User user = BmobUser.getCurrentUser(User.class);
        int courseId = user.getCourseId();
        BmobQuery<Course> query = new BmobQuery<>();
        query.addWhereEqualTo("id", courseId);
        query.findObjects(new FindListener<Course>() {
            @Override
            public void done(List<Course> list, BmobException e) {
                final Message message = new Message();
                if(e == null){
                    switch (type){
                        case "program":
                            bmobFile = list.get(0).getProgram();
                            message.what = GET_PROGRAM;
                            break;
                        case "experiment":
                            bmobFile = list.get(0).getExperiment();
                            message.what = GET_EXPERIMENT;
                            break;
                        case "time":
                            bmobFile = list.get(0).getTime();
                            message.what = GET_TIME;
                            break;
                    }
                }else{
                    message.what = ERROR;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void findAllProblem(final QuizFragment fragment){
        User user = BmobUser.getCurrentUser(User.class);
        String studentId = user.getUsername();
        BmobQuery<Problem> query = new BmobQuery<>();
        query.addWhereEqualTo("studentId", studentId);
        query.order("-createdAt").findObjects(new FindListener<Problem>() {
            @Override
            public void done(List<Problem> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    problemList = list;
                    message.what = PROBLEM_INFO;
                }else{
                    message.what = ERROR_PROBLEM;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }

    public static void addProblem(final QuizFragment fragment, final String p){
        User user = BmobUser.getCurrentUser(User.class);
        final String studentId = user.getUsername();
        BmobQuery<Problem> query = new BmobQuery<>();
        query.addWhereEqualTo("studentId", studentId);
        query.count(Problem.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null){
                    int id = integer + 1;
                    Problem problem = new Problem();
                    problem.setStudentId(studentId);
                    problem.setId(id);
                    problem.setProblem(p);
                    problem.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Message message = new Message();
                            if(e == null){
                                message.what = ADD_PROBLEM_SUCCESS;
                            }else {
                                message.what = ADD_FAIL;
                            }
                            fragment.handler.sendMessage(message);
                        }
                    });
                }else{

                }
            }
        });
    }

    public static void findStatisticUnit(final StudentStatisticActivity activity){
        statisticList.clear();
        User user = BmobUser.getCurrentUser(User.class);
        String studentId = user.getUsername();
        BmobQuery<Feedback> query = new BmobQuery<>();
        query.addWhereEqualTo("username", studentId);
        query.order("unitId");
        query.findObjects(new FindListener<Feedback>() {
            @Override
            public void done(List<Feedback> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    int length = list.size();
                    for(int i = 0; i < length; i++){
                        Statistic statistic = new Statistic();
                        statistic.setUnitId(list.get(i).getUnitId());
                        statistic.setTotalNum(list.get(i).getTotalNum());
                        statistic.setRightNum(list.get(i).getRightNum());
                        int accuracy = 0;
                        if(list.get(i).getTotalNum() != 0){
                            accuracy = 100 * list.get(i).getRightNum() / list.get(i).getTotalNum();
                        }
                        statistic.setAccuracy(accuracy);
                        statisticList.add(statistic);
                    }
                    message.what = STATISTIC_UNIT;
                }else {
                    message.what = STATISTIC_ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findStatisticClass(final StudentStatisticActivity activity){
        statisticList.clear();
        User user = BmobUser.getCurrentUser(User.class);
        String classId = user.getClassId();
        BmobQuery<Feedback> query = new BmobQuery<>();
        query.addWhereEqualTo("classId", classId);
        query.sum(new String[]{"totalNum", "rightNum"});
        query.groupby(new String[] {"username", "nickname"});
        query.findStatistics(Feedback.class, new QueryListener<JSONArray>() {
            @Override
            public void done(JSONArray jsonArray, BmobException e) {
                Message message = new Message();
                if(e == null){
                    int length = jsonArray.length();
                    List<Statistic> mStatisticList = new ArrayList<>();
                    try{
                        for (int i = 0; i < length; i++){
                            JSONObject obj = jsonArray.getJSONObject(i);
                            Statistic statistic = new Statistic();
                            statistic.setNickname(obj.getString("nickname"));
                            statistic.setTotalNum(obj.getInt("_sumTotalNum"));
                            statistic.setRightNum(obj.getInt("_sumRightNum"));
                            int accuracy = 0;
                            if(obj.getInt("_sumTotalNum") != 0){
                                accuracy = 100 * obj.getInt("_sumRightNum") / obj.getInt("_sumTotalNum");
                            }
                            statistic.setAccuracy(accuracy);
                            mStatisticList.add(statistic);
                        }
                        Collections.sort(mStatisticList, new Comparator<Statistic>() {
                            @Override
                            public int compare(Statistic o1, Statistic o2) {
                                return o1.getAccuracy().compareTo(o2.getAccuracy());
                            }
                        });
                        if(mStatisticList.size() <= 5){
                            for(int i = mStatisticList.size() - 1 ; i >= 0; i--){
                                statisticList.add(mStatisticList.get(i));
                            }
                        }else {
                            for(int i = mStatisticList.size() - 1; i >= (mStatisticList.size()-5); i--){
                                statisticList.add(mStatisticList.get(i));
                            }
                        }

                        message.what = STATISTIC_CLASS;
                    }catch (JSONException e1){
                        message.what = STATISTIC_ERROR;
                    }
                }else {
                    message.what = STATISTIC_ERROR;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findAllResources(final ResourceActivity activity, int unitId, int knowledgeId) {
        User user = BmobUser.getCurrentUser(User.class);
        int courseId = user.getCourseId();
        BmobQuery<Resource> q1 = new BmobQuery<>();
        q1.addWhereEqualTo("courseId", courseId);
        BmobQuery<Resource> q2 = new BmobQuery<>();
        q2.addWhereEqualTo("unitId", unitId);
        BmobQuery<Resource> q3 = new BmobQuery<>();
        q3.addWhereEqualTo("knowledgeId", knowledgeId);

        List<BmobQuery<Resource>> queryList = new ArrayList<>();
        queryList.add(q1);
        queryList.add(q2);
        queryList.add(q3);

        BmobQuery<Resource> query = new BmobQuery<>();
        query.and(queryList);
        query.findObjects(new FindListener<Resource>() {
            @Override
            public void done(List<Resource> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    resourceList = list;
                    message.what = ALL_RESOURCE_INFO;
                }else {
                    message.what = ERROR_RESOURCES;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findResourceByType(final ResourceActivity activity, int unitId, int knowledgeId, String type){
        User user = BmobUser.getCurrentUser(User.class);
        int courseId = user.getCourseId();
        BmobQuery<Resource> q1 = new BmobQuery<>();
        q1.addWhereEqualTo("courseId", courseId);
        BmobQuery<Resource> q2 = new BmobQuery<>();
        q2.addWhereEqualTo("unitId", unitId);
        BmobQuery<Resource> q3 = new BmobQuery<>();
        q3.addWhereEqualTo("knowledgeId", knowledgeId);
        BmobQuery<Resource> q4 = new BmobQuery<>();
        q4.addWhereEqualTo("type", type);

        List<BmobQuery<Resource>> queryList = new ArrayList<>();
        queryList.add(q1);
        queryList.add(q2);
        queryList.add(q3);
        queryList.add(q4);

        BmobQuery<Resource> query = new BmobQuery<>();
        query.and(queryList);
        query.findObjects(new FindListener<Resource>() {
            @Override
            public void done(List<Resource> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    resourceList = list;
                    message.what = ALL_RESOURCE_INFO;
                }else {
                    message.what = ERROR_RESOURCES;
                }
                activity.handler.sendMessage(message);
            }
        });
    }

    public static void findStudentsInfo(final HomeFragment fragment){

    }

    public static void findResourceInfo(final HomeFragment fragment){

    }

    public static void findQuestionInfo(final HomeFragment fragment){

    }

    public static void findProblemInfo(final HomeFragment fragment){
        BmobQuery<Problem> query = new BmobQuery<>();
        query.order("-createdAt").findObjects(new FindListener<Problem>() {
            @Override
            public void done(List<Problem> list, BmobException e) {
                Message message = new Message();
                if(e == null){
                    problemList = list;
                    message.what = PROBLEM_INFO;
                }else{
                    message.what = ERROR_PROBLEM;
                }
                fragment.handler.sendMessage(message);
            }
        });
    }
}
