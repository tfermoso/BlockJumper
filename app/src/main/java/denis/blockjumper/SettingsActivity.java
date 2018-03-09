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

import org.w3c.dom.Text;

import denis.blockjumper.Firebase.FirebaseReference;
import denis.blockjumper.Firebase.User;

public class SettingsActivity extends AppCompatActivity {
    private final String PREFS_NAME = "MY_PREFS";
    private FirebaseUser fu;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference blockDB;
    private String name;
    private Toast toast;
    private int score;
    private Button btn_login;
    private TextView txt_loged_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        btn_login = findViewById(R.id.btn_login_settings);
        txt_loged_in = findViewById(R.id.txt_loged_in);
        db = FirebaseDatabase.getInstance();
        blockDB = db.getReference(FirebaseReference.USERS);
        auth = FirebaseAuth.getInstance();
        if (fu == null) {
            fu = auth.getCurrentUser();
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        name = prefs.getString("name", null);
        score = prefs.getInt("score", 0);
        showUser();
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOrNot();
            }
        });
    }

    private void showUser() {
        if (fu != null) {
            String text = R.string.loggedAs + " " + name;
            txt_loged_in.setText(text);
            btn_login.setText(R.string.Exit);
        }
    }

    private void loginOrNot() {
        if (fu == null) {
            showLoginDialog();
        } else {
            auth.signOut();
            fu = auth.getCurrentUser();
            if (fu == null) {
                txt_loged_in.setText(R.string.NotLogged);
                btn_login.setText(R.string.Login);
            }
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
                name = user.getName();
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                if (!prefs.getString("name", "").equals(user.getName())) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name", user.getName());
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
