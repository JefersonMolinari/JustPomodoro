package com.scorpionest.justpomodoro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.scorpionest.justpomodoro.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    ActivityMainBinding bd;

    private static final int TIME_RATE_MILISEC = 100;
    private static final int TIME_MIN_TO_SEC = 60;
    private static final String DISPLAY_DISPLAY = "%02d:%02d";
    private static final String DISPLAY_START = "%02d:00";
    private static final String CYCLE_POMODORO = "cycle_pomodoro";
    private static final String CYCLE_BREAK = "cycle_break";

    private int timeMinPomodoro;
    private int timeMinShortBreak;
    private int timeMinLongBreak;
    private int sectionsToLongBreak;

    private boolean autoStart;
    private boolean isRunning;

    private String soundPomodoro;
    private String soundShortBreak;
    private String soundLongBreak;

    private int counterInSecs;
    private int counterPomodoro;

    private String currentCycle = "";

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bd = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setupSharedPreferences();

        nextSection();

        bd.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPomodoro();
            }
        });

        bd.buttonPause.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                bd.buttonStart.setVisibility(View.VISIBLE);
                bd.buttonPause.setVisibility(View.INVISIBLE);
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
            }
        });

        bd.buttonStop.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
                counterPomodoro = 0;
                currentCycle = "";
                nextSection();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setupSharedPreferences() {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        autoStart = sharedPreferences.getBoolean(getString(R.string.settings_auto_start_timer_key),
                getResources().getBoolean(R.bool.settings_auto_start_timer_default));

        loadTimePomodoroFromSharedPreferences(sharedPreferences);
        loadTimeShortBreakFromSharedPreferences(sharedPreferences);
        loadTimeLongBreakFromSharedPreferences(sharedPreferences);
        loadTimeLongBreakFreqFromSharedPreferences(sharedPreferences);

        loadSoundPomodoroFromSharedPreferences(sharedPreferences);
        loadSoundShortBreakFromSharedPreferences(sharedPreferences);
        loadSoundLongBreakFromSharedPreferences(sharedPreferences);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadTimePomodoroFromSharedPreferences(SharedPreferences sharedPreferences) {
        timeMinPomodoro = Integer.parseInt(sharedPreferences.getString(getString(R.string.settings_pomodoro_time_key),
                getString(R.string.settings_pomodoro_time_default)));
        if (!isRunning) {
            bd.textViewCounter.setText(String.format(DISPLAY_START, timeMinPomodoro));
        }
    }

    private void loadTimeShortBreakFromSharedPreferences(SharedPreferences sharedPreferences) {
        timeMinShortBreak = Integer.parseInt(sharedPreferences.getString(getString(R.string.settings_short_break_time_key),
                getString(R.string.settings_short_break_time_default)));
    }

    private void loadTimeLongBreakFromSharedPreferences(SharedPreferences sharedPreferences) {
        timeMinLongBreak = Integer.parseInt(sharedPreferences.getString(getString(R.string.settings_long_break_time_key),
                getString(R.string.settings_long_break_time_default)));
    }

    private void loadTimeLongBreakFreqFromSharedPreferences(SharedPreferences sharedPreferences) {
        sectionsToLongBreak = Integer.parseInt(sharedPreferences.getString(getString(R.string.settings_long_break_freq_time_key),
                getString(R.string.settings_long_break_freq_time_default)));
    }

    private void loadSoundPomodoroFromSharedPreferences(SharedPreferences sharedPreferences) {
        soundPomodoro = sharedPreferences.getString(getString(R.string.settings_pomodoro_sound_key),
                getString(R.string.settings_pomodoro_sound_default));
    }

    private void loadSoundShortBreakFromSharedPreferences(SharedPreferences sharedPreferences) {
        soundShortBreak = sharedPreferences.getString(getString(R.string.settings_short_break_sound_key),
                getString(R.string.settings_short_break_sound_default));
    }

    private void loadSoundLongBreakFromSharedPreferences(SharedPreferences sharedPreferences) {
        soundLongBreak = sharedPreferences.getString(getString(R.string.settings_long_break_sound_key),
                getString(R.string.settings_long_break_sound_default));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startPomodoro() {
        bd.buttonStart.setVisibility(View.INVISIBLE);
        bd.buttonPause.setVisibility(View.VISIBLE);
        timer = startCounter();
        timer.start();
    }

    private CountDownTimer startCounter() {
        isRunning = true;
        return new CountDownTimer(counterInSecs*TIME_RATE_MILISEC, TIME_RATE_MILISEC) {
            @Override
            public void onTick(long millisUntilFinished) {
                int timerMinutes = counterInSecs / TIME_MIN_TO_SEC;
                int timerSeconds = counterInSecs % TIME_MIN_TO_SEC;
                bd.textViewCounter.setText(String.format(DISPLAY_DISPLAY, timerMinutes, timerSeconds));
                counterInSecs--;

            }

            @Override
            public void onFinish() {
                nextSection();
                if (autoStart) {
                    startPomodoro();
                }
            }
        };
    }

    private void nextSection() {
        isRunning = false;
        bd.buttonStart.setVisibility(View.VISIBLE);
        bd.buttonPause.setVisibility(View.INVISIBLE);

        int counterInMins = 0;

        if (currentCycle.equals(CYCLE_POMODORO)) {
            counterPomodoro++;
            currentCycle = CYCLE_BREAK;
            if ((counterPomodoro % sectionsToLongBreak) != 0) {
                counterInMins = timeMinShortBreak;
                bd.textViewCycle.setText("SHORT BREAK");
            } else {
                counterInMins = timeMinLongBreak;
                bd.textViewCycle.setText("LONG BREAK");
            }
        } else {
            currentCycle = CYCLE_POMODORO;
            counterInMins = timeMinPomodoro;
            bd.textViewCycle.setText("POMODORO");
        }

        counterInSecs = counterInMins * TIME_MIN_TO_SEC;
        bd.textViewCounter.setText(String.format(DISPLAY_START, counterInMins));
        bd.textViewCounterPomos.setText(String.format("%d %s%s",
                counterPomodoro, getString(R.string.pomodoro), counterPomodoro == 1 ? "" : "s"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_auto_start_timer_key))) {
            autoStart = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.settings_auto_start_timer_default));
        } else if (key.equals(getString(R.string.settings_pomodoro_sound_key))) {
            loadSoundPomodoroFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_pomodoro_time_key))) {
            loadTimePomodoroFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_short_break_time_key))) {
            loadTimeShortBreakFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_long_break_time_key))) {
            loadTimeLongBreakFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_long_break_freq_time_key))) {
            loadTimeLongBreakFreqFromSharedPreferences(sharedPreferences);
        }
    }
}
