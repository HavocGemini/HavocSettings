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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.nano.MetricsProto; 
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;

import com.havoc.settings.R;
import com.havoc.support.preferences.SecureSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class Battery extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    public static final String TAG = "Battery";
    private static final String SCREEN_STATE_TOGGLES_ENABLE = "screen_state_toggles_enable_key";

    private SecureSettingMasterSwitchPreference mEnableScreenStateToggles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.havoc_settings_battery);

        ContentResolver resolver = getActivity().getContentResolver();

        mEnableScreenStateToggles = (SecureSettingMasterSwitchPreference) findPreference(SCREEN_STATE_TOGGLES_ENABLE);
        int enabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.START_SCREEN_STATE_SERVICE, 0, UserHandle.USER_CURRENT);
        mEnableScreenStateToggles.setChecked(enabled != 0);
        mEnableScreenStateToggles.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mEnableScreenStateToggles) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                    Settings.Secure.START_SCREEN_STATE_SERVICE, value ? 1 : 0, UserHandle.USER_CURRENT);
            Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.havoc.screenstate.ScreenStateService");
            if (value) {
                getActivity().stopService(service);
                getActivity().startService(service);
            } else {
                getActivity().stopService(service);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
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
                    sir.xmlResId = R.xml.havoc_settings_battery;
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
