/*
 * Copyright (C) 2013 Slimroms
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.havoc.settings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;

public class LockscreenShortcutFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String PREF_LOCKSCREEN_SHORTCUTS_LAUNCH_TYPE =
            "lockscreen_shortcuts_launch_type";

    private ListPreference mLockscreenShortcutsLaunchType;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.HAVOC_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_shortcut_fragment);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLockscreenShortcutsLaunchType = (ListPreference) findPreference(
                PREF_LOCKSCREEN_SHORTCUTS_LAUNCH_TYPE);
        mLockscreenShortcutsLaunchType.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView) view.findViewById(android.R.id.list);
        // our container already takes care of the padding
        if (list != null) {
            int paddingTop = list.getPaddingTop();
            int paddingBottom = list.getPaddingBottom();
            list.setPadding(0, paddingTop, 0, paddingBottom);
        }
        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenShortcutsLaunchType) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_SHORTCUTS_LONGPRESS,
                    Integer.valueOf((String) newValue));
        }
        return true;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.lockscreen_shortcut_fragment;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
