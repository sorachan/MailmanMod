package me.dillbox.mailmanmod;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HeldMailDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(HeldMail mail);

    @Query("DELETE FROM held_mail")
    void deleteAll();

    @Query("DELETE FROM held_mail WHERE list = :list")
    void deleteAll(String list);

    @Query("DELETE FROM held_mail WHERE id = :msgId AND list = :list")
    void delete(int msgId, String list);

    @Query("SELECT * FROM held_mail WHERE list = :list ORDER BY id ASC")
    LiveData<List<HeldMail>> getLiveMailQueue(String list);

    @Query("SELECT * FROM held_mail WHERE list = :list ORDER BY id ASC")
    List<HeldMail> getMailQueue(String list);

    @Query("SELECT DISTINCT list FROM held_mail")
    List<String> getListsInDb();
}
