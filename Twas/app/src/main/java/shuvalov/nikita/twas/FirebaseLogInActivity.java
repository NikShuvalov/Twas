package shuvalov.nikita.twas;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseLogInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmailEntry, mPasswordEntry;
    private Button mSignIn;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_log_in);

        findViews();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null){
                    Log.d("AuthStateChanged", "Logged in as "+ user.getUid());
                }else{
                    Log.d("AuthStateChanged", "Signed Out");
                }
            }
        };
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEmailEntry.getText().toString().isEmpty()){
                    mEmailEntry.setError("Can't be empty");
                }
                if(mPasswordEntry.getText().toString().isEmpty()){
                    mPasswordEntry.setError("Password field is empty");
                }else{
                    String email = mEmailEntry.getText().toString();
                    String password = mPasswordEntry.getText().toString();

                    mEmailEntry.setText("");
                    mPasswordEntry.setText("");
                    signInUserWithEmail(email,password);
                }
            }
        });
    }

    public void findViews(){
        mEmailEntry= (EditText)findViewById(R.id.email_entry);
        mPasswordEntry = (EditText)findViewById(R.id.password_entry);
        mSignIn = (Button)findViewById(R.id.log_in_button);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void createNewUserWithEmail(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Log-In Activity", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(FirebaseLogInActivity.this, "An oopsie happened",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    public void signInUserWithEmail(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Sign in", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("Sign in", "signInWithEmail:failed", task.getException());
                            Toast.makeText(FirebaseLogInActivity.this, "Not sign in good",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
