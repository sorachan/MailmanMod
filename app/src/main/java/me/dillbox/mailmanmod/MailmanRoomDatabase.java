package me.dillbox.mailmanmod;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {HeldMail.class}, version = 1, exportSchema = false)
public abstract class MailmanRoomDatabase extends RoomDatabase {
    public abstract HeldMailDao mailDao();

    private static volatile MailmanRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static MailmanRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MailmanRoomDatabase.class, "mailman_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
