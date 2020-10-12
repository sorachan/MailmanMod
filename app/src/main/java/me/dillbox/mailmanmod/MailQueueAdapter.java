package me.dillbox.mailmanmod;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MailQueueAdapter extends RecyclerView.Adapter<MailQueueAdapter.MailViewHolder> {
    class MailViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout heldMailView;

        private MailViewHolder(View parentView) {
            super(parentView);
            heldMailView = parentView.findViewById(R.id.message_view);
        }
    }

    private final LayoutInflater mInflater;
    private List<HeldMail> mMailQueue;
    private Context mContext;
    private boolean mAbsDate;
    private boolean mShowId;

    MailQueueAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mAbsDate = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("show_abs_date", false);
        mShowId = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("show_msg_id", false);
    }

    @Override
    public MailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.message_view, parent, false);
        return new MailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MailViewHolder holder, int position) {
        if (mMailQueue != null) {
            HeldMail current = mMailQueue.get(position);
            ((TextView) holder.heldMailView.findViewById(R.id.sender_line)).setText(current.getName());
            ((TextView) holder.heldMailView.findViewById(R.id.email_line)).setText(current.getEmail());
            ((TextView) holder.heldMailView.findViewById(R.id.subject_line)).setText(current.getSubject());
            if (mShowId) {
                ((TextView) holder.heldMailView.findViewById(R.id.msg_id)).setText("#" + current.getId());
            } else {
                ((TextView) holder.heldMailView.findViewById(R.id.msg_id)).setVisibility(View.GONE);
            }
            ((TextView) holder.heldMailView.findViewById(R.id.arrived)).setText(current.getDateString(mAbsDate));

            CheckBox accept = (CheckBox) holder.heldMailView.findViewById(R.id.accept);
            accept.setChecked(current.getAccept());
            accept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    current.setAccept(b);
                }
            });

            ConstraintLayout message_info = (ConstraintLayout) holder.heldMailView.findViewById(R.id.message_main);
            message_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MailViewActivity.class);
                    intent.putExtra("sender", current.getName());
                    intent.putExtra("email", current.getEmail());
                    intent.putExtra("subject", current.getSubject());
                    intent.putExtra("received", current.getDateObject().toString());
                    intent.putExtra("headers", current.getHeaders());
                    intent.putExtra("message", current.getMessage());
                    mContext.startActivity(intent);
                }
            });
        }
    }

    void setMailQueue(List<HeldMail> mailQueue) {
        mMailQueue = mailQueue;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mMailQueue != null) return mMailQueue.size();
        return 0;
    }

    public List<Integer> findAcceptMsgId(boolean accept) {
        List<Integer> result = new ArrayList<>();
        for (HeldMail mail : mMailQueue) {
            if (mail.getAccept() == accept) {
                result.add(mail.getId());
            }
        }
        return result;
    }
}
