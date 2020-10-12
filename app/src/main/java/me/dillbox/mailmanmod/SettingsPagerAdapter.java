package me.dillbox.mailmanmod;

import android.content.Context;

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
public class SettingsPagerAdapter extends FragmentPagerAdapter {

    private static List<String> sLists;
    private final Context mContext;
    private long mBase;

    public SettingsPagerAdapter(Context context, FragmentManager fm, int base) {
        super(fm);
        mContext = context;
        String lists = PreferenceManager.getDefaultSharedPreferences(context).getString("lists_string","");
        mBase = base;
        sLists = SettingsActivity.listsFromPrefString(lists);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new SettingsFragmentGlobal();
        return SettingsFragmentList.newInstance(sLists.get(position - 1));
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
        if (position == 0)
            return mContext.getResources().getString(R.string.settings_global);
        return sLists.get(position - 1);
    }

    @Override
    public int getCount() {
        return sLists.size() + 1;
    }
}