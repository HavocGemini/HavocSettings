package com.havoc.settings.fragments;
 
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
 
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.havoc.support.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

public class EdgeGesturesSettings extends SettingsPreferenceFragment implements 
        Preference.OnPreferenceChangeListener, Indexable {
 
    public static final String EDGE_GESTURES_SCREEN_PERCENT = "edge_gestures_back_screen_percent";
 
    private String previousTitle;

    private CustomSeekBarPreference screenPercentPreference;
 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.edge_gestures);

        screenPercentPreference = (CustomSeekBarPreference) findPreference(EDGE_GESTURES_SCREEN_PERCENT);
        int percent = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.EDGE_GESTURES_BACK_SCREEN_PERCENT, 60, UserHandle.USER_CURRENT);
        screenPercentPreference.setValue(percent);
        screenPercentPreference.setOnPreferenceChangeListener(this);
    } 
 
    @Override 
    public void onStart() {
        super.onStart();
 
        ActionBar actionBar = getActivity().getActionBar();
        previousTitle = actionBar.getTitle().toString();
        actionBar.setTitle(R.string.edge_gestures_title);
    } 
 
    @Override 
    public void onStop() { 
        super.onStop();
 
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(previousTitle);
    } 
 
    @Override 
    public int getMetricsCategory() { 
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    } 
 
    @Override 
    public boolean onPreferenceChange(Preference preference, Object newValue) { 
        if (preference == screenPercentPreference) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.EDGE_GESTURES_BACK_SCREEN_PERCENT, value);
            return true;
        } /* else if (preference == hapticFeedbackDurationPreference) { 
            int hapticFeedbackValue = Integer.valueOf((String) newValue);
            Settings.Secure.putIntForUser(getContentResolver(), Settings.Secure.EDGE_GESTURES_FEEDBACK_DURATION, hapticFeedbackValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == longPressDurationPreference) { 
            int longPressValue = Integer.valueOf((String) newValue);
            Settings.Secure.putIntForUser(getContentResolver(), Settings.Secure.EDGE_GESTURES_LONG_PRESS_DURATION, longPressValue, UserHandle.USER_CURRENT);
            return true;
        }*/ 
        return false;
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
                    sir.xmlResId = R.xml.edge_gestures;
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
