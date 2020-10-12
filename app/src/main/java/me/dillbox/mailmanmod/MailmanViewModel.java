package me.dillbox.mailmanmod;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MailmanViewModel extends AndroidViewModel {
    private MailmanRepository mRepository;

    public MailmanViewModel (Application application) {
        super(application);
        mRepository = MailmanRepository.getInstance(application);
    }

    LiveData<List<HeldMail>> getMailQueue(String list) {
        return mRepository.getMailQueue(list);
    }

    public void insert(HeldMail mail) {
        mRepository.insert(mail);
    }

    public void deleteAll(String list) {
        mRepository.deleteAll(list);
    }

    public void delete(String list, int msgId) {
        mRepository.delete(list, msgId);
    }
}
