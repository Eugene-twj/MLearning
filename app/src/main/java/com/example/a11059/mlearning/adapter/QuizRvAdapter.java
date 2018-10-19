package com.example.a11059.mlearning.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.a11059.mlearning.R;
import com.example.a11059.mlearning.activity.StudentMainActivity;
import com.example.a11059.mlearning.entity.Problem;
import com.example.a11059.mlearning.entity.User;
import com.example.a11059.mlearning.fragment.QuizFragment;
import com.like.LikeButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * Created by 11059 on 2018/8/16.
 */

public class QuizRvAdapter extends RecyclerView.Adapter<QuizRvAdapter.ViewHolder>{

    private QuizFragment quizFragment;

    private User currentUser;

    private Context mContext;

    private List<Problem> mProblemList;


    public QuizRvAdapter(List<Problem> problemList){
        mProblemList = problemList;
        currentUser = BmobUser.getCurrentUser(User.class);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView quizContent;
        TextView quizReply;
        TextView createdAt;
        TextView replyTime;

        public ViewHolder(View view){
            super(view);
            quizContent = (TextView) view.findViewById(R.id.quiz_content);
            quizReply = (TextView) view.findViewById(R.id.quiz_reply);
            createdAt = (TextView) view.findViewById(R.id.create_time);
            replyTime = (TextView) view.findViewById(R.id.reply_time);
        }
    }

    @NonNull
    @Override
    public QuizRvAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.quiz_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int lPosition = holder.getLayoutPosition();
        holder.quizContent.setText((lPosition + 1) + ". " + mProblemList.get(lPosition).getProblem());
        holder.createdAt.setText(mProblemList.get(lPosition).getCreatedAt());
        if(mProblemList.get(lPosition).getReply() == null){
            holder.quizReply.setText("暂未回复");
            holder.replyTime.setVisibility(View.GONE);
        }else {
            holder.replyTime.setVisibility(View.VISIBLE);
            holder.quizReply.setText(mProblemList.get(lPosition).getReply());
            holder.replyTime.setText(mProblemList.get(lPosition).getUpdatedAt());
        }
    }

    @Override
    public int getItemCount() {
        return mProblemList == null ? 0 : mProblemList.size();
    }

}
