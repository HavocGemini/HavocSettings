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

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import android.support.annotation.NonNull;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;

import com.havoc.settings.Utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class PowerMenu extends SettingsPreferenceFragment     
                implements Preference.OnPreferenceChangeListener, Indexable {

    public static final String TAG = "PowerMenu";
	
    private static final String KEY_POWERMENU_TORCH = "powermenu_torch";
    private static final String KEY_POWERMENU_LOCKSCREEN = "powermenu_lockscreen";
    private static final String KEY_POWERMENU_LS_REBOOT = "powermenu_ls_reboot";
    private static final String KEY_POWERMENU_LS_ADVANCED_REBOOT = "powermenu_ls_advanced_reboot";
    private static final String KEY_POWERMENU_LS_SCREENSHOT = "powermenu_ls_screenshot";
    private static final String KEY_POWERMENU_LS_AIRPLANE = "powermenu_ls_airplane";

    private SwitchPreference mPowermenuTorch;
    private SwitchPreference mPowerMenuLockscreen;
    private SwitchPreference mPowerMenuReboot;
    private SwitchPreference mPowerMenuAdvancedReboot;
    private SwitchPreference mPowerMenuScreenshot;
    private SwitchPreference mPowerMenuAirplane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu);
		
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
			
        mPowermenuTorch = (SwitchPreference) findPreference(KEY_POWERMENU_TORCH);
        mPowermenuTorch.setOnPreferenceChangeListener(this);
        if (!Utils.deviceSupportsFlashLight(getActivity())) {
            prefScreen.removePreference(mPowermenuTorch);
        } else {
        mPowermenuTorch.setChecked((Settings.System.getInt(resolver,
                Settings.System.POWERMENU_TORCH, 0) == 1));
        }

        mPowerMenuLockscreen = (SwitchPreference) findPreference(KEY_POWERMENU_LOCKSCREEN);
        mPowerMenuLockscreen.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LOCKSCREEN, 1) == 1));
        mPowerMenuLockscreen.setOnPreferenceChangeListener(this);

        mPowerMenuReboot = (SwitchPreference) findPreference(KEY_POWERMENU_LS_REBOOT);
        mPowerMenuReboot.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_REBOOT, 1) == 1));
        mPowerMenuReboot.setOnPreferenceChangeListener(this);

        mPowerMenuAdvancedReboot = (SwitchPreference) findPreference(KEY_POWERMENU_LS_ADVANCED_REBOOT);
        mPowerMenuAdvancedReboot.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_ADVANCED_REBOOT, 0) == 1));
        mPowerMenuAdvancedReboot.setOnPreferenceChangeListener(this);

        mPowerMenuScreenshot = (SwitchPreference) findPreference(KEY_POWERMENU_LS_SCREENSHOT);
        mPowerMenuScreenshot.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_SCREENSHOT, 0) == 1));
        mPowerMenuScreenshot.setOnPreferenceChangeListener(this);

        mPowerMenuAirplane = (SwitchPreference) findPreference(KEY_POWERMENU_LS_AIRPLANE);
        mPowerMenuAirplane.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_AIRPLANE, 0) == 1));
        mPowerMenuAirplane.setOnPreferenceChangeListener(this);

        updateLockscreen();
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mPowermenuTorch) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_TORCH, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuLockscreen) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LOCKSCREEN, value ? 1 : 0);
            updateLockscreen();
            return true;
        } else if (preference == mPowerMenuReboot) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_REBOOT, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuAdvancedReboot) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_ADVANCED_REBOOT, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuScreenshot) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_SCREENSHOT, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuAirplane) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_AIRPLANE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }

    private void updateLockscreen() {
        boolean lockscreenOptions = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POWERMENU_LOCKSCREEN, 1) == 1;

        if (lockscreenOptions) {
            mPowerMenuReboot.setEnabled(true);
            mPowerMenuAdvancedReboot.setEnabled(true);
            mPowerMenuScreenshot.setEnabled(true);
            mPowerMenuAirplane.setEnabled(true);
        } else {
            mPowerMenuReboot.setEnabled(false);
            mPowerMenuAdvancedReboot.setEnabled(false);
            mPowerMenuScreenshot.setEnabled(false);
            mPowerMenuAirplane.setEnabled(false);
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
                    sir.xmlResId = R.xml.power_menu;
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
