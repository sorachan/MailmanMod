package me.dillbox.mailmanmod;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.tabs.TabLayout;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private WorkManager mWorkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Random r = new Random();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), r.nextInt() >>> 1);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            int i = sectionsPagerAdapter.getListPosition(b.getString("list"));
            viewPager.post(new Runnable() {
                @Override
                public void run() {
                    viewPager.setCurrentItem(i);
                }
            });
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        try {
            int position = tabs.getSelectedTabPosition();
            //notificationManager.cancel(sectionsPagerAdapter.getLists().get(position).hashCode());
        } catch (IndexOutOfBoundsException e) {

        }

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            //notificationManager.cancel(sectionsPagerAdapter.getLists().get(position).hashCode());
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mWorkManager = WorkManager.getInstance(this);
        for (String list : sectionsPagerAdapter.getLists()) {
            int interval = -1;
            try {
                interval = Integer.parseInt(prefs.getString(list + "_interval", null));
            } catch (Exception e) {

            }
            if (interval != -1) {
                createNotificationChannel(list);
                initWork(list, interval, false);
            }
        }

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                for (String list : sectionsPagerAdapter.getLists()) {
                    if (key.equals(list + "_interval")) {
                        int interval = -1;
                        try {
                            interval = Integer.parseInt(prefs.getString(key, null));
                        } catch (Exception e) {

                        }
                        if (interval != -1) {
                            createNotificationChannel(list);
                            initWork(list, interval, true);
                        }
                    }
                }
                recreate();
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);

        ImageButton ib = findViewById(R.id.imageButton);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuItemClick(item);
    }

    public void showPopup(View v) {
        Log.i("foobar", "showPopup: ");
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.main_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            default:
                return false;
        }
    }

    private void createNotificationChannel(String list) {
        String list_display = PreferenceManager.getDefaultSharedPreferences(this).getString(list+"_display_name", list);
        String list_name = list_display.trim().isEmpty() ? list : list_display;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = list + "-notification";
            CharSequence name = String.format(getString(R.string.channel_description), list_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void initWork(String list, int minutes, boolean replace) {
        Data.Builder builder = new Data.Builder();
        builder.putString("list", list);

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(RefreshWorker.class, minutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(builder.build())
                .build();

        ExistingPeriodicWorkPolicy policy = replace ? ExistingPeriodicWorkPolicy.REPLACE : ExistingPeriodicWorkPolicy.KEEP;

        mWorkManager.enqueueUniquePeriodicWork(list, policy, work);

        Log.i("work-queue", "set up queue for "+list+": "+minutes+" min");
    }
}