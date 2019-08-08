/*
 * Copyright (C) 2018 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.havoc.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto; 
import com.android.settings.SettingsPreferenceFragment;

import com.havoc.settings.R;

import com.havoc.support.preferences.SystemSettingMasterSwitchPreference;

public class Misc extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Misc";

    private static final String MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "3";
    private static final String GAMING_MODE_ENABLED = "gaming_mode_enabled";
    private static final String HFI = "haptic_feedback_intensity";
    private static final String NVI = "notification_vibration_intensity";

    private ListPreference mMSOB;
    private ListPreference mScrollingCachePref;
    private SystemSettingMasterSwitchPreference mGamingMode;
    private ListPreference mHFI;
    private ListPreference mNVI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.havoc_settings_misc);

        // MediaScanner behavior on boot
        mMSOB = (ListPreference) findPreference(MEDIA_SCANNER_ON_BOOT);
        int mMSOBValue = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MEDIA_SCANNER_ON_BOOT, 0);
        mMSOB.setValue(String.valueOf(mMSOBValue));
        mMSOB.setSummary(mMSOB.getEntry());
        mMSOB.setOnPreferenceChangeListener(this);

        // Scrolling cache
        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setSummary(mScrollingCachePref.getEntry());
        mScrollingCachePref.setOnPreferenceChangeListener(this);

        mGamingMode = (SystemSettingMasterSwitchPreference) findPreference(GAMING_MODE_ENABLED);
        mGamingMode.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.GAMING_MODE_ENABLED, 0) == 1));
        mGamingMode.setOnPreferenceChangeListener(this);

        mHFI = (ListPreference) findPreference(HFI);
        int mHFIV = Settings.System.getInt(getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_INTENSITY, 0);
        mHFI.setValue(Integer.toString(mHFIV));
        mHFI.setSummary(mHFI.getEntry());
        mHFI.setOnPreferenceChangeListener(this);

        mNVI = (ListPreference) findPreference(NVI);
        int mNVIV = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_VIBRATION_INTENSITY, 0);
        mNVI.setValue(Integer.toString(mNVIV));
        mNVI.setSummary(mNVI.getEntry());
        mNVI.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mMSOB) {
            int value = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(getContentResolver(),
                    Settings.System.MEDIA_SCANNER_ON_BOOT, value);
            mMSOB.setValue(String.valueOf(value));
            mMSOB.setSummary(mMSOB.getEntries()[value]);
            return true;
        } else if (preference == mScrollingCachePref) {
            String value = (String) newValue;
            int index = mScrollingCachePref.findIndexOfValue(value);
            SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, value);
            mScrollingCachePref.setSummary(mScrollingCachePref.getEntries()[index]);
            return true;
        } else if (preference == mGamingMode) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GAMING_MODE_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mHFI) {
            int value = Integer.valueOf((String) newValue);
            int index = mHFI.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_INTENSITY, value);
            mHFI.setSummary(mHFI.getEntries()[index]);
            return true;
        } else if (preference == mNVI) {
            int value = Integer.valueOf((String) newValue);
            int index = mNVI.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_VIBRATION_INTENSITY, value);
            mNVI.setSummary(mNVI.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }
}
