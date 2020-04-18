package com.scorpionest.justpomodoro;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;


public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_visualizer);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference preference = prefScreen.getPreference(i);
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(),"");
                setPreferenceSummary(preference, value);
            }
        }

        Preference preference = findPreference(getString(R.string.settings_pomodoro_time_key));
        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(getString(R.string.settings_short_break_time_key));
        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(getString(R.string.settings_long_break_time_key));
        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(getString(R.string.settings_long_break_freq_time_key));
        preference.setOnPreferenceChangeListener(this);

        final Toast toast = Toast.makeText(getContext(), "To Be Implemented", Toast.LENGTH_SHORT);

        toBeImplementedToast(toast, getString(R.string.settings_config_do_not_disturb_mode_key));
        toBeImplementedToast(toast, getString(R.string.settings_config_vibrate_key));
        toBeImplementedToast(toast, getString(R.string.settings_config_rotate_key));
        toBeImplementedToast(toast, getString(R.string.settings_config_prevent_screen_lock_key));
//        toBeImplementedToast(toast, getString(R.string.settings_sound_timer_start_key));
//        toBeImplementedToast(toast, getString(R.string.settings_sound_timer_end_key));
        toBeImplementedToast(toast, getString(R.string.settings_pomodoro_sound_key));
        toBeImplementedToast(toast, getString(R.string.settings_short_break_sound_key));
        toBeImplementedToast(toast, getString(R.string.settings_long_break_sound_key));
    }

    private void toBeImplementedToast(final Toast toast, String key) {
        Preference preference = findPreference(key);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                toast.show();
                return false;
            }
        });
    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(),"");
                setPreferenceSummary(preference, value);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Toast error = Toast.makeText(getContext(), "Please enter a number higher than 0", Toast.LENGTH_SHORT);

        String pomodoroTimeKey = getString(R.string.settings_pomodoro_time_key);
        String shortBreakTimeKey = getString(R.string.settings_short_break_time_key);
        String longBreakTimeKey = getString(R.string.settings_long_break_time_key);
        String longBreakFreqTimeKey = getString(R.string.settings_long_break_freq_time_key);
        String preferenceKey = preference.getKey();
        if (preferenceKey.equals(pomodoroTimeKey) ||
                preferenceKey.equals(shortBreakTimeKey) ||
                preferenceKey.equals(longBreakTimeKey) ||
                preferenceKey.equals(longBreakFreqTimeKey)) {
            String stringTime = (String) newValue;
            try {
                int time = Integer.parseInt(stringTime);
                if (time <= 0) {
                    error.show();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                error.show();
                return false;
            }
        }
        return true;
    }
}
