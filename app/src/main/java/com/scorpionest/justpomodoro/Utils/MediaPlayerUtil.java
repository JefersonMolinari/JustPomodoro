package com.scorpionest.justpomodoro.Utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

public class MediaPlayerUtil {

    private MediaPlayer mediaPlayer;
    private Context context;
    private AudioAttributes attributes;
    private int sound;
    private boolean looping = false;

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };

    public MediaPlayerUtil(Context context, boolean isBgSound) {
        this.context = context;
        looping = isBgSound;
        if (isBgSound) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
        } else {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .build();
        }
    }

    public void start(int sound) {
        this.sound = sound;
        releaseMediaPlayer();
        if (!(sound == 0)) {
            mediaPlayer = MediaPlayer.create(context, sound);
            mediaPlayer.setAudioAttributes(attributes);
            mediaPlayer.setLooping(looping);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(completionListener);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
