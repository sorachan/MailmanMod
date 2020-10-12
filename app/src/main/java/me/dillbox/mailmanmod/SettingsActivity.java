package me.dillbox.mailmanmod;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private static final String FRAGMENT_BASE = "fragment_base";
    private int base = 0;

    public static List<String> listsFromPrefString(String lists) {
        List<String> prefLists = new LinkedList<String>(Arrays.asList(lists.split("\\s*\\n\\s*")));
        Iterator<String> iterator = prefLists.iterator();
        while (iterator.hasNext()) {
            String list = iterator.next().trim();
            if (list.length() == 0)
                iterator.remove();
        }
        return prefLists;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            base = savedInstanceState.getInt(FRAGMENT_BASE, 0);
        }
        Log.i("skeet", "onCreate: SETTINGSACTIVITY");
        setContentView(R.layout.activity_settings);
        SettingsPagerAdapter settingsPagerAdapter = new SettingsPagerAdapter(this, getSupportFragmentManager(), base);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(settingsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.i("skeet", "onSharedPreferenceChanged: "+key);
                if (key.equals("lists_string")) {
                    WorkManager.getInstance(SettingsActivity.this).cancelAllWork();
                    Log.i("work-queue", "killing all work queues");

                    String lists = prefs.getString("lists_string", "");

                    try {
                        MailmanRepository repository = MailmanRepository.getInstance(SettingsActivity.this);
                        List<String> listsInDb = repository.getListsInDb();

                        List<String> prefLists = listsFromPrefString(lists);

                        for (String list : listsInDb) {
                            Log.i("db", "onSharedPreferenceChanged: db contains " + list);
                            if (!prefLists.contains(list)) {
                                Log.i("db", "onSharedPreferenceChanged: purging " + list + " from db");
                                repository.deleteAll(list);
                            }
                        }
                    } catch (Exception e) {

                    }

                    base += lists.length() + 4;
                    recreate();
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(FRAGMENT_BASE, base);
        super.onSaveInstanceState(savedInstanceState);
    }
}