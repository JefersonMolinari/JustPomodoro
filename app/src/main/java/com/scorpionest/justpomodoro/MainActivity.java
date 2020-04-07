package com.scorpionest.justpomodoro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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

    Button btnStart;
    Button btnStop;
    Button btnPause;
    TextView counterView;
    TextView counterPomodoroView;

    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counterView = (TextView) findViewById(R.id.textViewCounter);
        counterPomodoroView = (TextView) findViewById(R.id.textViewCounterPomos);
        btnStart = (Button) findViewById(R.id.buttonStart);
        btnStop = (Button) findViewById(R.id.buttonStop);
        btnPause = (Button) findViewById(R.id.buttonPause);

        nextSection();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStart.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                timer = startCounter();
                timer.start();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                btnStart.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener(){

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
                counterView.setText(String.format(DISPLAY_DISPLAY, timerMinutes, timerSeconds));
                counterInSecs--;
            }

            @Override
            public void onFinish() {
                nextSection();
            }
        };
    }

    private void nextSection() {
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.INVISIBLE);

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
        counterView.setText(String.format(DISPLAY_START, counterInMins));
        counterPomodoroView.setText(String.format("%d Pomodoro%s",counterPomodoro, counterPomodoro == 1 ? "" : "s"));
    }
}
