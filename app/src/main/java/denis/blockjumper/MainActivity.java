package denis.blockjumper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;

import denis.blockjumper.Globals.Prefs;
import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FancyButton btn_start, btn_highscores, btn_settings;
    private TextView txt_score;
    private MediaPlayer mediaPlayer;
    private int length = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        btn_highscores = findViewById(R.id.btn_highscores);
        btn_settings = findViewById(R.id.btn_settings);
        txt_score = findViewById(R.id.txt_score);
        btn_start.setOnClickListener(this);
        btn_highscores.setOnClickListener(this);
        btn_settings.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer = MediaPlayer.create(this, R.raw.game_menu);
        mediaPlayer.seekTo(length);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        putScore();
    }

    private void putScore() {
        SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        int score = prefs.getInt(Prefs.SCORE, 0);
        System.out.println(score);
        txt_score.setText(score + "");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            mediaPlayer.stop();
            length = 0;
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_highscores) {
            length = mediaPlayer.getCurrentPosition();
            Intent intent = new Intent(MainActivity.this, LeaderboardsActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_settings) {
            length = mediaPlayer.getCurrentPosition();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
    }
}
