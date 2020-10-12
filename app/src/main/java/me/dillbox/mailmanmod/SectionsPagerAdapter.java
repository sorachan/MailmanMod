package me.dillbox.mailmanmod;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static List<String> sLists;
    private final Context mContext;
    private long mBase;

    public SectionsPagerAdapter(Context context, FragmentManager fm, int base) {
        super(fm);
        mContext = context;
        String lists = PreferenceManager.getDefaultSharedPreferences(context).getString("lists_string","");
        mBase = base;
        Log.i("mqueue", "SectionsPagerAdapter: base = "+base);
        sLists = SettingsActivity.listsFromPrefString(lists);
    }

    @Override
    public Fragment getItem(int position) {
        String list = sLists.get(position);
        return ModeratorFragment.newInstance(list);
    }

    @Override
    public long getItemId(int position) {
        return mBase + position;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String list = sLists.get(position);
        String list_display = PreferenceManager.getDefaultSharedPreferences(mContext).getString(list+"_display_name", list);
        return list_display.trim().isEmpty() ? list : list_display;
    }

    @Override
    public int getCount() {
        return sLists.size();
    }

    public int getListPosition(String list) {
        for (int i = 0; i < sLists.size(); i++) {
            if (sLists.get(i).equals(list)) {
                return i;
            }
        }
        return PagerAdapter.POSITION_NONE;
    }

    public List<String> getLists() {
        return sLists;
    }
}