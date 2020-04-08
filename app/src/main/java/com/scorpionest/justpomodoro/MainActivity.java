package com.scorpionest.justpomodoro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import com.scorpionest.justpomodoro.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding bd;

    private static final int TIME_MIN_POMODORO = 25;
    private static final int TIME_MIN_SHORT_BREAK = 5;
    private static final int TIME_MIN_LONG_BREAK = 15;
    private static final int TIME_RATE_MILISEC = 1000;
    private static final int TIME_MIN_TO_SEC = 60;
    private static final int SECTIONS_TO_LONG_BREAK = 40;

    private static final String DISPLAY_DISPLAY = "%02d:%02d";
    private static final String DISPLAY_START = "%02d:00";
    private static final String CYCLE_POMODORO = "activity_pomodoro";
    private static final String CYCLE_BREAK = "activity_break";

    private int timerMinutes;
    private int timerSeconds;
    private int counterInSecs;
    private int counterPomodoro;

    private String currentCycle = "";

    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bd = DataBindingUtil.setContentView(this, R.layout.activity_main);

        nextSection();

        bd.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bd.buttonStart.setVisibility(View.INVISIBLE);
                bd.buttonPause.setVisibility(View.VISIBLE);
                timer = startCounter();
                timer.start();
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

    private CountDownTimer startCounter() {
        return new CountDownTimer(counterInSecs*TIME_RATE_MILISEC, TIME_RATE_MILISEC) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerMinutes = counterInSecs / TIME_MIN_TO_SEC;
                timerSeconds = counterInSecs % TIME_MIN_TO_SEC;
                bd.textViewCounter.setText(String.format(DISPLAY_DISPLAY, timerMinutes, timerSeconds));
                counterInSecs--;
            }

            @Override
            public void onFinish() {
                nextSection();
            }
        };
    }

    private void nextSection() {
        bd.buttonStart.setVisibility(View.VISIBLE);
        bd.buttonPause.setVisibility(View.INVISIBLE);

        int counterInMins = 0;

        if (currentCycle.equals(CYCLE_POMODORO)) {
            counterPomodoro++;
            currentCycle = CYCLE_BREAK;
            if ((counterPomodoro % SECTIONS_TO_LONG_BREAK) != 0) {
                counterInMins = TIME_MIN_SHORT_BREAK;
            } else {
                counterInMins = TIME_MIN_LONG_BREAK;
            }
        } else {
            currentCycle = CYCLE_POMODORO;
            counterInMins = TIME_MIN_POMODORO;
        }

        counterInSecs = counterInMins * TIME_MIN_TO_SEC;
        bd.textViewCounter.setText(String.format(DISPLAY_START, counterInMins));
        bd.textViewCounterPomos.setText(String.format("%d Pomodoro%s",counterPomodoro, counterPomodoro == 1 ? "" : "s"));
    }
}
