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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.google.firebase.database.ValueEventListener;

import denis.blockjumper.Firebase.User;
import denis.blockjumper.Globals.Prefs;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseUser fu;
    private FirebaseAuth auth;
    private DatabaseReference blockDB;
    private String name;
    private Toast toast;
    private int score;
    private TextView txt_loged_in;

    private Button btn_login, btn_default, btn_save;
    private EditText txt_columnNumber, txt_boxInterval, txt_maxBoxInterval, txt_maxGravity;
    private CheckBox check_godMode, check_boxedBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        btn_login = findViewById(R.id.btn_login_settings);
        txt_loged_in = findViewById(R.id.txt_loged_in);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        blockDB = db.getReference(Prefs.USERS);
        auth = FirebaseAuth.getInstance();
        if (fu == null) {
            fu = auth.getCurrentUser();
        }
        SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        name = prefs.getString(Prefs.NAME, "");
        score = prefs.getInt(Prefs.SCORE, 0);

        int columnNumber = prefs.getInt(Prefs.COLUMN_NUMBER, 8);
        int boxInterval = prefs.getInt(Prefs.BOX_INTERVAL, 50);
        int maxBoxInterval = prefs.getInt(Prefs.MAX_BOX_INTERVAL, 100);
        int maxGravity = prefs.getInt(Prefs.MAX_GRAVITY, 40);
        boolean godMode = prefs.getBoolean(Prefs.GOD_MODE, false);
        boolean boxedBox = prefs.getBoolean(Prefs.BOXED_BOX, false);
        showUser();

        // Config
        txt_columnNumber = findViewById(R.id.txt_columnNumber);
        txt_boxInterval = findViewById(R.id.txt_boxInterval);
        txt_maxBoxInterval = findViewById(R.id.txt_maxBoxInterval);
        txt_maxGravity = findViewById(R.id.txt_maxGravity);

        check_boxedBox = findViewById(R.id.check_boxedBox);
        check_godMode = findViewById(R.id.check_godMode);

        btn_default = findViewById(R.id.btn_default);
        btn_save = findViewById(R.id.btn_save);

        txt_columnNumber.setText(columnNumber + "");
        txt_boxInterval.setText(boxInterval + "");
        txt_maxBoxInterval.setText(maxBoxInterval + "");
        txt_maxGravity.setText(maxGravity + "");

        check_godMode.setChecked(godMode);
        check_boxedBox.setChecked(boxedBox);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOrNot();
            }
        });

        btn_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_columnNumber.setText(8 + "");
                txt_boxInterval.setText(50 + "");
                txt_maxBoxInterval.setText(100 + "");
                txt_maxGravity.setText(40 + "");
                check_godMode.setChecked(false);
                check_boxedBox.setChecked(false);
                saveOptions(false);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOptions(true);
            }
        });
    }

    private void saveOptions(Boolean custom) {
        String column = txt_columnNumber.getText().toString();
        String interval = txt_boxInterval.getText().toString();
        String max_interval = txt_maxBoxInterval.getText().toString();
        String gravity = txt_maxGravity.getText().toString();
        if (column.isEmpty() || interval.isEmpty() || max_interval.isEmpty() || gravity.isEmpty()) {
            showToast("Need all options to continue");
            return;
        }
        SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Prefs.COLUMN_NUMBER, Integer.parseInt(column));
        editor.putInt(Prefs.BOX_INTERVAL, Integer.parseInt(interval));
        editor.putInt(Prefs.MAX_BOX_INTERVAL, Integer.parseInt(max_interval));
        editor.putInt(Prefs.MAX_GRAVITY, Integer.parseInt(gravity));
        editor.putBoolean(Prefs.GOD_MODE, check_godMode.isChecked());
        editor.putBoolean(Prefs.BOXED_BOX, check_boxedBox.isChecked());
        editor.apply();
        if (custom) {
            showToast("Saved. Points are not saved with custom options");
        } else {
            showToast("Saved default options");
        }
    }

    private void showUser() {
        if (fu != null) {
            String text = getString(R.string.loggedAs) + " " + name;
            txt_loged_in.setText(text);
            btn_login.setText(R.string.Exit);
        }
    }

    private void loginOrNot() {
        if (fu == null) {
            showLoginDialog();
        } else {
            confirmLogout();
        }
    }

    private void confirmLogout() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("You will lose your current score, do you want to continue?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(Prefs.SCORE, 0);
                        editor.putString(Prefs.NAME, "");
                        editor.apply();
                        auth.signOut();
                        fu = auth.getCurrentUser();
                        score = 0;
                        txt_loged_in.setText(R.string.NotLogged);
                        btn_login.setText(R.string.Login);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create();
        dialog.show();

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
                                            getUserName();
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
                                                            getUserName();
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

    private void getUserName() {
        blockDB.child(fu.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                SharedPreferences prefs = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE);
                if (!name.equals(user.getName())) {
                    name = user.getName();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Prefs.NAME, user.getName());
                    editor.apply();
                }
                if (user.getPoints() > score) {
                    showToast("Score updated");
                    score = user.getPoints();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(Prefs.SCORE, user.getPoints());
                    editor.apply();
                }
                showUser();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
