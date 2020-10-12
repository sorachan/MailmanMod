package me.dillbox.mailmanmod;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class MailmanRepository {
    private HeldMailDao mMailDao;

    private static MailmanRepository INSTANCE;

    public static MailmanRepository getInstance(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MailmanRepository(context);
        }
        return INSTANCE;
    }

    private MailmanRepository(final Context context) {
        MailmanRoomDatabase db = MailmanRoomDatabase.getDatabase(context);
        mMailDao = db.mailDao();
    }

    LiveData<List<HeldMail>> getMailQueue(String list) {
        return mMailDao.getLiveMailQueue(list);
    }

    public void insert(HeldMail mail) {
        MailmanRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMailDao.insert(mail);
        });
    }

    public void deleteAll(String list) {
        MailmanRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMailDao.deleteAll(list);
        });
    }

    public void delete(String list, int msgId) {
        MailmanRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMailDao.delete(msgId, list);
        });
    }

    public List<String> getListsInDb() throws ExecutionException, InterruptedException {
        Future<List<String>> future = MailmanRoomDatabase.databaseWriteExecutor.submit(
            () ->  mMailDao.getListsInDb()
        );
        return future.get();
    }

    public boolean hasMsgId(String list, int msgId) {
        /*for (HeldMail mail : mMailDao.getMailQueue(list)) {
            if (mail.getId() == msgId) {
                return true;
            }
        }
        return false;*/
        return getById(list, msgId) != null;
    }

    public HeldMail getById(String list, int msgId) {
        for (HeldMail mail : mMailDao.getMailQueue(list)) {
            if (mail.getId() == msgId) {
                return mail;
            }
        }
        return null;
    }

    public List<Integer> findOldMsgId(String list, List<Integer> currentMsgIds) {
        List<HeldMail> mailQueue = mMailDao.getMailQueue(list);
        List<Integer> toRemove = new ArrayList<>();
        Collections.sort(currentMsgIds);
        Iterator<HeldMail> oldIt = mailQueue.listIterator();
        Iterator<Integer> newIt = currentMsgIds.listIterator();
        int newId = -1;
        while (oldIt.hasNext()) {
            int oldId = oldIt.next().getId();
            while (newId < oldId && newIt.hasNext()) {
                newId = newIt.next();
            }
            if (newId != oldId) {
                toRemove.add(oldId);
            }

        }
        return toRemove;
    }
}
