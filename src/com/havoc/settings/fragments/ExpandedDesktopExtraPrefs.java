/*
 * Copyright (C) 2015 The CyanogenMod Project
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.view.WindowManager;
import android.view.WindowManagerPolicyControl;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class ExpandedDesktopExtraPrefs extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {
    private static final String KEY_EXPANDED_DESKTOP_STYLE = "expanded_desktop_style";

    private ListPreference mExpandedDesktopStylePref;
    private int mExpandedDesktopStyle;
    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    public static ExpandedDesktopExtraPrefs newInstance() {
        ExpandedDesktopExtraPrefs expandedDesktopExtraPrefs = new ExpandedDesktopExtraPrefs();
        return expandedDesktopExtraPrefs;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.expanded_desktop_prefs);
        mExpandedDesktopStyle = getExpandedDesktopStyle();
        createPreferences();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.HAVOC_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSettingsObserver.register(true);
    }

    @Override
    public void onPause() {
        mSettingsObserver.register(false);
        super.onPause();
    }

    public void createPreferences() {
        mExpandedDesktopStylePref = (ListPreference) findPreference(KEY_EXPANDED_DESKTOP_STYLE);
        mExpandedDesktopStylePref.setOnPreferenceChangeListener(this);
        updateExpandedDesktopStyle();
    }

    private void updateExpandedDesktopStyle() {
        if (mExpandedDesktopStylePref == null) {
            return;
        }
        mExpandedDesktopStyle = getExpandedDesktopStyle();
        mExpandedDesktopStylePref.setValueIndex(mExpandedDesktopStyle);
        mExpandedDesktopStylePref.setSummary(getDesktopSummary(mExpandedDesktopStyle));
        // We need to visually show the change
        // TODO: This is hacky, but it works
        writeValue("");
        writeValue("immersive.full=*");
    }

    private int getDesktopSummary(int state) {
        switch (state) {
            case WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_STATUS:
                return R.string.expanded_hide_status;
            case WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_NAVIGATION:
                return R.string.expanded_hide_navigation;
            case WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_FULL:
            default:
                return R.string.expanded_hide_both;
        }
    }

    private int getExpandedDesktopStyle() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.POLICY_CONTROL_STYLE,
                WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_FULL);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final int val = Integer.valueOf((String) value);
        WindowManagerPolicyControl.saveStyleToSettings(getActivity(), val);
        return false;
    }

    private void writeValue(String value) {
        Settings.Global.putString(getContentResolver(), Settings.Global.POLICY_CONTROL, value);
    }

    // === Window Policy Style Callbacks ===

    private final class SettingsObserver extends ContentObserver {
        private final Uri DEFAULT_WINDOW_POLICY_STYLE =
                Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL_STYLE);

        public SettingsObserver() {
            super(mHandler);
        }

        public void register(boolean register) {
            final ContentResolver cr = getContentResolver();
            if (register) {
                cr.registerContentObserver(DEFAULT_WINDOW_POLICY_STYLE, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (DEFAULT_WINDOW_POLICY_STYLE.equals(uri)) {
                updateExpandedDesktopStyle();
            }
        }
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
                    sir.xmlResId = R.xml.expanded_desktop_prefs;
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
