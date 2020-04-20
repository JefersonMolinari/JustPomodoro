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

import com.scorpionest.justpomodoro.Utils.MediaPlayerUtil;
import com.scorpionest.justpomodoro.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    ActivityMainBinding bd;

    private static final int TIME_RATE_MILISEC = 1000;
    private static final int TIME_MIN_TO_SEC = 60;
    private static final String DISPLAY_COUNTER = "%02d:%02d";
    private static final String DISPLAY_START = "%02d:00";
    private static final String CYCLE_POMODORO = "POMODORO";
    private static final String CYCLE_SHORT_BREAK = "SHORT BREAK";
    private static final String CYCLE_LONG_BREAK = "LONG BREAK";

    private int timeMinPomodoro;
    private int timeMinShortBreak;
    private int timeMinLongBreak;
    private int sectionsToLongBreak;

    private boolean autoStart;
    private boolean isRunning;
    private boolean isPaused;
    private boolean playSoundTimeStart;
    private boolean playSoundTimeEnd;

    private String soundPomodoro;
    private String soundShortBreak;
    private String soundLongBreak;

    private int soundPomodoroInt;
    private int soundShortBreakInt;
    private int soundLongBreakInt;
    private int currentBgSound;


    private final int NOTIFICATION_START_SOUND = R.raw.bell_sound;;
    private final int NOTIFICATION_END_SOUND = R.raw.bell_sound;;

    private int counterInSecs;
    private int counterPomodoro;

    private String currentCycle = "";

    private CountDownTimer timer;

    private MediaPlayerUtil bgMp;
    private MediaPlayerUtil notificaionStartMp;
    private MediaPlayerUtil notificaionEndMp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bgMp = new MediaPlayerUtil(this, true);
        notificaionStartMp = new MediaPlayerUtil(this, false);
        notificaionEndMp = new MediaPlayerUtil(this, false);

        bd = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setupSharedPreferences();

        setPomodoro();

        bd.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    startCounter();
                } else {
                    bd.buttonStart.setText(getString(R.string.continue_label));
                    counterInSecs++;
                    isPaused = true;
                    bgMp.stop();
                    if(timer != null){
                        timer.cancel();
                        timer = null;
                    }
                }
            }
        });

        bd.buttonStop.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                bgMp.stop();
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
                setPomodoro();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_auto_start_timer_key))) {
            loadAutoStartFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_pomodoro_time_key))) {
            loadTimePomodoroFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_short_break_time_key))) {
            loadTimeShortBreakFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_long_break_time_key))) {
            loadTimeLongBreakFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_long_break_freq_time_key))) {
            loadTimeLongBreakFreqFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_pomodoro_sound_key))) {
            loadSoundPomodoroFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_short_break_sound_key))) {
            loadSoundShortBreakFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_long_break_sound_key))) {
            loadSoundLongBreakFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_sound_timer_start_key))) {
            loadPlaySoundStartFromSharedPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.settings_sound_timer_end_key))) {
            loadPlaySoundEndFromSharedPreferences(sharedPreferences);
        }
    }

    private void setupSharedPreferences() {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        loadAutoStartFromSharedPreferences(sharedPreferences);

        loadTimePomodoroFromSharedPreferences(sharedPreferences);
        loadTimeShortBreakFromSharedPreferences(sharedPreferences);
        loadTimeLongBreakFromSharedPreferences(sharedPreferences);
        loadTimeLongBreakFreqFromSharedPreferences(sharedPreferences);

        loadSoundPomodoroFromSharedPreferences(sharedPreferences);
        loadSoundShortBreakFromSharedPreferences(sharedPreferences);
        loadSoundLongBreakFromSharedPreferences(sharedPreferences);

        loadPlaySoundStartFromSharedPreferences(sharedPreferences);
        loadPlaySoundEndFromSharedPreferences(sharedPreferences);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadPlaySoundEndFromSharedPreferences(SharedPreferences sharedPreferences) {
        playSoundTimeEnd = sharedPreferences.getBoolean(getString(R.string.settings_sound_timer_end_key),
                getResources().getBoolean(R.bool.settings_sound_timer_end_default));
    }

    private void loadPlaySoundStartFromSharedPreferences(SharedPreferences sharedPreferences) {
        playSoundTimeStart = sharedPreferences.getBoolean(getString(R.string.settings_sound_timer_start_key),
                getResources().getBoolean(R.bool.settings_sound_timer_start_default));
    }

    private void loadAutoStartFromSharedPreferences(SharedPreferences sharedPreferences) {
        autoStart = sharedPreferences.getBoolean(getString(R.string.settings_auto_start_timer_key),
                getResources().getBoolean(R.bool.settings_auto_start_timer_default));
    }

    private void loadTimePomodoroFromSharedPreferences(SharedPreferences sharedPreferences) {
        timeMinPomodoro = Integer.parseInt(sharedPreferences.getString(getString(R.string.settings_pomodoro_time_key),
                getString(R.string.settings_pomodoro_time_default)));
        if (!isRunning) {
            setPomodoro();
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

        soundPomodoroInt = setBgSound(soundPomodoro, CYCLE_POMODORO);
    }

    private void loadSoundShortBreakFromSharedPreferences(SharedPreferences sharedPreferences) {
        soundShortBreak = sharedPreferences.getString(getString(R.string.settings_short_break_sound_key),
                getString(R.string.settings_short_break_sound_default));

        soundShortBreakInt = setBgSound(soundShortBreak, CYCLE_SHORT_BREAK);
    }

    private void loadSoundLongBreakFromSharedPreferences(SharedPreferences sharedPreferences) {
        soundLongBreak = sharedPreferences.getString(getString(R.string.settings_long_break_sound_key),
                getString(R.string.settings_long_break_sound_default));

        soundLongBreakInt = setBgSound(soundLongBreak, CYCLE_LONG_BREAK);
    }

    private int setBgSound(String sound, String cycle) {
        int soundInt;
        if (sound.equals(getString(R.string.settings_sound_option_wilderness_value))) {
            soundInt = R.raw.wilderness_sound;
        } else{
            soundInt = 0;
        }
        if (currentCycle.equals(cycle)) {
            currentBgSound = soundInt;
            if (isRunning && !isPaused) {
                bgMp.start(soundInt);
            }
        }

        return soundInt;
    }

    private void startCounter() {
        if (playSoundTimeStart) {
            notificaionStartMp.start(NOTIFICATION_START_SOUND);
        }
        bgMp.start(currentBgSound);

        if (!isRunning) {
            isRunning = true;
        }
        if (isPaused) {
            bd.buttonStart.setText(getString(R.string.pause));
            isPaused = false;
        }

        timer = new CountDownTimer(counterInSecs*TIME_RATE_MILISEC, TIME_RATE_MILISEC) {
            @Override
            public void onTick(long millisUntilFinished) {
                int timerMinutes = counterInSecs / TIME_MIN_TO_SEC;
                int timerSeconds = counterInSecs % TIME_MIN_TO_SEC;
                bd.textViewCounter.setText(String.format(DISPLAY_COUNTER, timerMinutes, timerSeconds));
                counterInSecs--;
            }

            @Override
            public void onFinish() {
                bgMp.stop();
                if (playSoundTimeEnd) {
                    notificaionEndMp.start(NOTIFICATION_END_SOUND);
                }
                nextSection();
                if (autoStart) {
                    startCounter();
                } else{
                    bd.buttonStart.setText(getString(R.string.start));
                    isPaused = true;
                }
            }
        };

        timer.start();
    }

    private void nextSection() {
        int counterInMin;

        if (currentCycle.equals(CYCLE_POMODORO)) {
            counterPomodoro++;
            if ((counterPomodoro % sectionsToLongBreak) != 0) {
                counterInMin = timeMinShortBreak;
                currentCycle = CYCLE_SHORT_BREAK;
                currentBgSound = soundShortBreakInt;
            } else {
                counterInMin = timeMinLongBreak;
                currentCycle = CYCLE_LONG_BREAK;
                currentBgSound = soundLongBreakInt;
            }
        } else {
            currentCycle = CYCLE_POMODORO;
            counterInMin = timeMinPomodoro;
            currentBgSound = soundPomodoroInt;
        }

        counterInSecs = counterInMin * TIME_MIN_TO_SEC;

        bd.textViewCycle.setText(currentCycle);
        bd.textViewCounter.setText(String.format(DISPLAY_START, counterInMin));
        bd.textViewCounterPomos.setText(String.format("%d %s%s",
                counterPomodoro, getString(R.string.pomodoro), counterPomodoro == 1 ? "" : "s"));
    }

    private void setPomodoro() {
        counterPomodoro = 0;
        isPaused = true;
        isRunning = false;
        currentCycle = CYCLE_POMODORO;
        currentBgSound = soundPomodoroInt;
        counterInSecs = timeMinPomodoro * TIME_MIN_TO_SEC;

        bd.buttonStart.setText(getString(R.string.start));
        bd.textViewCycle.setText(currentCycle);
        bd.textViewCounter.setText(String.format(DISPLAY_START, timeMinPomodoro));
        bd.textViewCounterPomos.setText(String.format("0 %s", getString(R.string.pomodoro)));
    }
}
