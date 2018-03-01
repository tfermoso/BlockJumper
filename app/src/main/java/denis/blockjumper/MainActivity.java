package denis.blockjumper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import denis.blockjumper.Firebase.FirebaseReference;
import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FancyButton btn_start, btn_highscores;
    private TextView txt_score;
    private final String PREFS_NAME = "MY_PREFS";
    FirebaseDatabase db;
    DatabaseReference blockDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        btn_highscores = findViewById(R.id.btn_highscores);
        txt_score = findViewById(R.id.txt_score);
        btn_start.setOnClickListener(this);
        btn_highscores.setOnClickListener(this);
        putScore();

        db = FirebaseDatabase.getInstance();
        blockDB = db.getReference(FirebaseReference.NAME);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        putScore();
    }
    private void putScore(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int score = prefs.getInt("score", 0);
        System.out.println(score);
        txt_score.setText(score + "");
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_highscores){
            Intent intent = new Intent(MainActivity.this, LeaderboardsActivity.class);
            startActivity(intent);
        }
    }
}
