package denis.blockjumper;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import denis.blockjumper.Adapters.LeaderboardListAdapter;
import denis.blockjumper.Firebase.User;
import denis.blockjumper.Globals.Prefs;
import mehdi.sakout.fancybuttons.FancyButton;

public class LeaderboardsActivity extends AppCompatActivity {
    private DatabaseReference blockDB;
    private FirebaseAuth auth;
    private List<User> userList;
    private ListView userListView;
    private LeaderboardsActivity self = this;
    private int score;
    private ProgressBar progressBarLeader;
    private FirebaseUser fu;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_leaderboards);
        userListView = findViewById(R.id.list_leader);
        FancyButton btn_publish = findViewById(R.id.btn_publish);
        TextView txtScoreLeaderBoard = findViewById(R.id.txtScoreLeaderBoard);
        progressBarLeader = findViewById(R.id.progressBarLeader);
        userList = new ArrayList<>();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        blockDB = db.getReference(Prefs.USERS);

        SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        score = prefs.getInt(Prefs.SCORE, 0);

        txtScoreLeaderBoard.setText(score + "");
        // addValueEventListener() para recibir siempre
        // addListenerForSingleValueEvent() para recibir una vez
        Query query = blockDB.orderByChild("points").limitToFirst(10);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    User user = dsp.getValue(User.class);
                    userList.add(user);
                }
                Collections.reverse(userList);
                userListView.setAdapter(new LeaderboardListAdapter(R.layout.list_leaderboard, userList, self));
                if (progressBarLeader != null && progressBarLeader.getVisibility() != View.GONE) {
                    progressBarLeader.setVisibility(View.GONE);
                    progressBarLeader = null;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (fu == null) {
            fu = auth.getCurrentUser();
        }
        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fu == null) {
                    showLoginDialog();
                } else {
                    publishScore();
                }
            }
        });


    }

    private void publishScore() {
        if (fu != null) {
            blockDB.child(fu.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (!prefs.getString(Prefs.NAME, "").equals(user.getName())) {
                        editor.putString(Prefs.NAME, user.getName());
                        editor.apply();
                    }
                    if (user.getPoints() > score) {
                        editor = prefs.edit();
                        showToast("You have a better score on leaderboard");
                        score = user.getPoints();
                        editor.putInt(Prefs.SCORE, score);
                        editor.apply();
                    } else {
                        user.setPoints(score);
                        blockDB.child(fu.getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showToast("Score sent: " + score);
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showToast(databaseError.getMessage());
                }
            });
        }
    }

    private void showLoginDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("LOGIN")
                .setMessage("Please login to submit score")
                .setView(login_layout)
                .setPositiveButton("LOGIN", null)
                .setNegativeButton("CANCEL", null)
                .create();

        final EditText txt_email = login_layout.findViewById(R.id.txt_email);
        final EditText txt_password = login_layout.findViewById(R.id.txt_password);
        final TextView btn_register = login_layout.findViewById(R.id.btn_register);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (txt_email.getText().toString().isEmpty() ||
                                txt_password.getText().toString().isEmpty()) {
                            showToast("Complete all fields to continue");
                        } else {
                            auth.signInWithEmailAndPassword(txt_email.getText().toString(), txt_password.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            fu = authResult.getUser();
                                            dialog.dismiss();
                                            publishScore();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showToast(e.getMessage());
                                        }
                                    });
                        }
                    }
                });
                btn_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        showRegisterDialog();
                    }
                });
            }
        });
        dialog.show();
    }

    private void showRegisterDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_register, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("REGISTER")
                .setMessage("Please register to submit score")
                .setView(login_layout)
                .setPositiveButton("REGISTER", null)
                .setNegativeButton("CANCEL", null)
                .create();

        final EditText txt_email = login_layout.findViewById(R.id.txt_email);
        final EditText txt_password = login_layout.findViewById(R.id.txt_password);
        final EditText txt_name = login_layout.findViewById(R.id.txt_name);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (txt_email.getText().toString().isEmpty() ||
                                txt_password.getText().toString().isEmpty() ||
                                txt_name.getText().toString().isEmpty()) {
                            showToast("Complete all fields to continue");
                        } else {
                            auth.createUserWithEmailAndPassword(txt_email.getText().toString(), txt_password.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(final AuthResult authResult) {
                                            User user = new User();
                                            user.setName(txt_name.getText().toString());
                                            user.setDate(System.currentTimeMillis() + "");
                                            user.setPoints(score);
                                            blockDB.child(authResult.getUser().getUid())
                                                    .setValue(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            fu = authResult.getUser();
                                                            dialog.dismiss();
                                                            publishScore();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            showToast(e.getMessage());
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
