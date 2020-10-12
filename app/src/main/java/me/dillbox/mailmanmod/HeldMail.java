package me.dillbox.mailmanmod;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "held_mail", primaryKeys = {"id", "list"})
public class HeldMail {
    @NonNull
    @ColumnInfo(name = "id")
    private int mId;

    @NonNull
    @ColumnInfo(name = "list")
    private String mList;

    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name = "email")
    private String mEmail;

    @ColumnInfo(name = "subject")
    private String mSubject;

    @ColumnInfo(name = "date")
    private String mDate;

    @ColumnInfo(name = "headers")
    private String mHeaders;

    @ColumnInfo(name = "message")
    private String mMessage;

    @Ignore
    private boolean mAccept;

    @Ignore
    private long mArrived;

    @Ignore
    private static String sDateFormatOut = "yyyy.MM.dd\nHH:mm";

    @Ignore
    private static String sDateFormatIn = "EEE, d MMM yyyy HH:mm:ss Z";

    public HeldMail(@NonNull int id, String list, String name, String email, String subject, String date, String headers, String message) {
        mId = id;
        mList = list;
        mName = name;
        mEmail = email;
        mSubject = subject;
        mDate = date;
        mHeaders = headers;
        mMessage = message;
        mAccept = false;

        try {
            mArrived = new SimpleDateFormat(sDateFormatIn).parse(mDate).getTime();
        } catch (Exception e) {
            mArrived = new Date().getTime();
        }
    }

    public int getId() {
        return mId;
    }

    public String getList() {
        return mList;
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getDate() {
        return mDate;
    }

    public String getHeaders() {
        return mHeaders;
    }

    public String getMessage() {
        return mMessage;
    }

    public boolean getAccept() {
        return mAccept;
    }

    public void setAccept(boolean accept) {
        mAccept = accept;
    }

    public CharSequence getDateString(boolean absDates) {
        if (absDates) {
            return new SimpleDateFormat(sDateFormatOut).format(new Date(mArrived));
        } else {
            return DateUtils.getRelativeTimeSpanString(mArrived, new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        }
    }

    public Date getDateObject() {
        return new Date(mArrived);
    }

    @Override
    public String toString() {
        return String.format("ID: %s\nList: %s\nSender: %s\nE-Mail: %s\nSubject: %s\nDate: %s\nAccept: %s\n\n=== Headers ===\n\n%s\n\n=== Message ===\n\n%s",mId,mList,mName,mEmail,mSubject,mDate, mAccept ? "yes" : "no",mHeaders,mMessage);
    }
}
