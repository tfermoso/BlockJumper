package denis.blockjumper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_start;
    private TextView txt_score;
    private final String PREFS_NAME = "MY_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        btn_start = (Button) findViewById(R.id.btn_start);
        txt_score = (TextView) findViewById(R.id.txt_score);
        btn_start.setOnClickListener(this);
        putScore();
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
        }
    }
}
