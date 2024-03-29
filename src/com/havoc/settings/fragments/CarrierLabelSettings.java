/*
 * Copyright (C) 2018 Havoc-OS
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.havoc.support.preferences.SystemSettingSwitchPreference;
import com.havoc.support.preferences.SystemSettingListPreference;
import com.havoc.support.preferences.CustomSeekBarPreference;
import com.havoc.support.colorpicker.ColorPickerPreference;
import com.android.internal.logging.nano.MetricsProto;

import com.android.internal.util.havoc.HavocUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarrierLabelSettings extends SettingsPreferenceFragment implements
	Preference.OnPreferenceChangeListener, Indexable {

    private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";
    private static final String STATUS_BAR_CARRIER_FONT_SIZE  = "status_bar_carrier_font_size";
    private static final String CARRIER_FONT_STYLE  = "status_bar_carrier_font_style";
    private static final String KEY_CARRIER_LABEL = "status_bar_show_carrier";

    static final int DEFAULT_STATUS_CARRIER_COLOR = 0xFFFFFFFF;

    private PreferenceScreen mCustomCarrierLabel;
    private String mCustomCarrierLabelText;
    private ColorPickerPreference mCarrierColorPicker;
    private CustomSeekBarPreference mStatusBarCarrierSize;
    private ListPreference mCarrierFontStyle;
    private SystemSettingListPreference mShowCarrierLabel;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.carrier_label);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        // custom carrier label
        mCustomCarrierLabel = (PreferenceScreen) findPreference(CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mShowCarrierLabel = (SystemSettingListPreference) findPreference(KEY_CARRIER_LABEL);
        int showCarrierLabel = Settings.System.getInt(resolver,
        Settings.System.STATUS_BAR_SHOW_CARRIER, 1);
        CharSequence[] NonNotchEntries = { getResources().getString(R.string.show_carrier_disabled),
                getResources().getString(R.string.show_carrier_keyguard),
                getResources().getString(R.string.show_carrier_statusbar), getResources().getString(
                        R.string.show_carrier_enabled) };
        CharSequence[] NotchEntries = { getResources().getString(R.string.show_carrier_disabled),
                getResources().getString(R.string.show_carrier_keyguard) };
        CharSequence[] NonNotchValues = {"0", "1" , "2", "3"};
        CharSequence[] NotchValues = {"0", "1"};
        mShowCarrierLabel.setEntries(HavocUtils.hasNotch(getActivity()) ? NotchEntries : NonNotchEntries);
        mShowCarrierLabel.setEntryValues(HavocUtils.hasNotch(getActivity()) ? NotchValues : NonNotchValues);
        mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
        mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
        mShowCarrierLabel.setOnPreferenceChangeListener(this);

        mCarrierColorPicker = (ColorPickerPreference) findPreference(STATUS_BAR_CARRIER_COLOR);
        mCarrierColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_CARRIER_COLOR);
        hexColor = String.format("#%08x", (0xFFFFFFFF & intColor));
        if (hexColor.equals("#ffffffff")) {
            mCarrierColorPicker.setSummary(R.string.default_string);
        } else {
            mCarrierColorPicker.setSummary(hexColor);
        }
        mCarrierColorPicker.setNewPreviewColor(intColor);

        mStatusBarCarrierSize = (CustomSeekBarPreference) findPreference(STATUS_BAR_CARRIER_FONT_SIZE);
        int StatusBarCarrierSize = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, 14);
        mStatusBarCarrierSize.setValue(StatusBarCarrierSize / 1);
        mStatusBarCarrierSize.setOnPreferenceChangeListener(this);

        mCarrierFontStyle = (ListPreference) findPreference(CARRIER_FONT_STYLE);
        int showCarrierFont = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER_FONT_STYLE, 14);
        mCarrierFontStyle.setValue(String.valueOf(showCarrierFont));
        mCarrierFontStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
 		ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarCarrierSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, width);
            return true;
        } else if (preference == mCarrierColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
            return true;
        } else if (preference == mCarrierFontStyle) {
            int showCarrierFont = Integer.valueOf((String) newValue);
            int index = mCarrierFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                STATUS_BAR_CARRIER_FONT_STYLE, showCarrierFont);
            return true;
        } else if (preference == mShowCarrierLabel) {
            int value = Integer.parseInt((String) newValue);
            updateCarrierLabelSummary(value);
            return true;
        }
        return false;
    }

    private void updateCarrierLabelSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Carrier Label disabled
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_disabled));
        } else if (value == 1) {
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_keyguard));
        } else if (value == 2) {
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_statusbar));
        } else if (value == 3) {
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_enabled));
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;
        if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);
            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(55, 20, 55, 20);
            final EditText input = new EditText(getActivity());
            int maxLength = 25;
            input.setLayoutParams(lp);
            input.setGravity(android.view.Gravity.TOP| Gravity.START);
            input.setText(TextUtils.isEmpty(mCustomCarrierLabelText) ? "" : mCustomCarrierLabelText);
            input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            container.addView(input);
            alert.setView(container);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                            getActivity().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
            return true;
        }
        return false;
    }

    private void updateCustomLabelTextSummary() {
        mCustomCarrierLabelText = Settings.System.getString(
            getContentResolver(), Settings.System.CUSTOM_CARRIER_LABEL);
        if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
            mCustomCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomCarrierLabel.setSummary(mCustomCarrierLabelText);
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
                    sir.xmlResId = R.xml.carrier_label;
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
