package com.example.a11059.mlearning.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;

import com.example.a11059.mlearning.R;
import com.example.a11059.mlearning.adapter.ResourceRvAdapter;
import com.example.a11059.mlearning.entity.Knowledge;
import com.example.a11059.mlearning.entity.Resource;
import com.example.a11059.mlearning.entity.Unit;
import com.example.a11059.mlearning.utils.UtilDatabase;
import com.example.a11059.mlearning.utils.UtilNetwork;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIEmptyView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.example.a11059.mlearning.activity.QuestionActivity.currentTrainMode;

public class ResourceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TIP_TYPE_SUCCESS = 1;

    private static final int TIP_TYPE_FAIL = 0;

    private static final int TIP_TYPE_INFO = 2;

    private static final long DEFAULT_TIP_DURATION = 1000;

    private QMUITopBar topBar;

    private QMUIEmptyView emptyView;

    private RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;

    private static int currentUnitId = 1;

    private static int currentKnowledgeId = 1;

    private List<Resource> resourceList = new ArrayList<>();

    public MyHandler handler = new MyHandler(this);

    public static class MyHandler extends Handler {

        private final WeakReference<ResourceActivity> mActivity;

        public MyHandler(ResourceActivity activity){
            mActivity = new WeakReference<ResourceActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ResourceActivity activity = mActivity.get();
            switch (msg.what){
                case UtilDatabase.ALL_RESOURCE_INFO:
                    if(UtilDatabase.resourceList.size() == 0){
                        activity.emptyView.show("未找到相关资料", "该单元还没有学习资料");
                    }else {
                        activity.resourceList = UtilDatabase.resourceList;
                        activity.emptyView.hide();
                        activity.resetAdapter();
                    }
                    break;
                case UtilDatabase.ERROR_RESOURCES:
                    activity.showEmptyViewTip();
                    break;
            }
        }
    }

    public static void actionStart(Context context, int unitId, int knowledgeId){
        Intent intent = new Intent(context, ResourceActivity.class);
        currentUnitId = unitId;
        currentKnowledgeId = knowledgeId;
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
        QMUIStatusBarHelper.translucent(this);
        initEmptyView();
        initTopBar();
        initRecyclerView();
        UtilDatabase.findAllResources(this, currentUnitId, currentKnowledgeId);
    }

    private void initEmptyView(){
        emptyView = (QMUIEmptyView) findViewById(R.id.emptyView);
        emptyView.show(true);
    }

    private void initTopBar(){
        topBar = (QMUITopBar) findViewById(R.id.resource_topbar);
        topBar.setTitle(getTopBarTitle());
        topBar.setSubTitle(getTopBarSubTitle());
        topBar.addLeftBackImageButton().setOnClickListener(this);
        topBar.addRightTextButton("筛选", R.drawable.submit_exam).setOnClickListener(this);
    }

    private String getTopBarTitle(){
        String title = "";
        for (Unit unit : UtilDatabase.unitsList){
            if(unit.getId().equals(currentUnitId)){
                title = unit.getName();
            }
        }
        return title;
    }

    private String getTopBarSubTitle(){
        String subTitle = "";
        int i = 0;
        for (Unit unit : UtilDatabase.unitsList){
            if(unit.getId().equals(currentUnitId)){
                for(Knowledge knowledge : UtilDatabase.unitKnowledgeList.get(i)){
                    if(knowledge.getId().equals(currentKnowledgeId)){
                        subTitle = knowledge.getName();
                        return subTitle;
                    }
                }
            }else {
                i++;
            }
        }
        return subTitle;
    }

    private void initRecyclerView(){
        recyclerView = (RecyclerView) findViewById(R.id.resource_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(ResourceActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void resetAdapter(){
        ResourceRvAdapter adapter = new ResourceRvAdapter(this, resourceList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.qmui_topbar_item_left_back:
                finish();
            case R.drawable.submit_exam:
                showResourceTypes();
                break;
        }
    }

    private void showResourceTypes(){
        new QMUIBottomSheet.BottomListSheetBuilder(this)
                .addItem("All")
                .addItem("PDF")
                .addItem("VIDEO")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        String type = "";
                        switch (position){
                            case 0:
                                UtilDatabase.findAllResources(ResourceActivity.this, currentUnitId, currentKnowledgeId);
                                break;
                            case 1:
                                type = "pdf";
                                break;
                            case 2:
                                type = "video";
                                break;
                        }
                        UtilDatabase.findResourceByType(ResourceActivity.this, currentUnitId, currentKnowledgeId, type);
                    }
                })
        .build()
        .show();
    }

    private void showEmptyViewTip(){
        if(!UtilNetwork.isNetworkAvailable()){
            emptyView.show("网络异常", "请确认网络畅通后重试");
        } else {
            emptyView.show("未获取到相关课程资源", "请重试~");
        }
    }
}
