package shuvalov.nikita.twas.Activities;

import android.content.Intent;
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

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.R;


//ToDo: On Sign-up we should have another edittext activity for "confirm password"

public class FirebaseLogInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmailEntry, mPasswordEntry;
    private Button mSignIn, mSignUp;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_log_in);

        findViews();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");

        mAuth = FirebaseAuth.getInstance();

        //ToDo: Once I have a splash loading screen(& set as launch activity), do this check in that activity.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null){
                    String userId = user.getUid();

                    //Check if the authenticated user's id matches the id we have stored in preferences. If not, clear preferences.
                    if(!SelfUserProfileUtils.compareStoredIdWithCurrentId(FirebaseLogInActivity.this,userId)){
                        SelfUserProfileUtils.clearUserProfile(FirebaseLogInActivity.this); //We clear the user Preferences if the IDs don't match. This is a back-up check, typically the preferences should be cleared on sign-out as well.
                        //ToDo: Check to see if user has a profile in FBDB, if so add that info to preferences, otherwise add only UserId to preferences.
                        SelfUserProfileUtils.setUserId(FirebaseLogInActivity.this, userId);

                    }
                    Toast.makeText(FirebaseLogInActivity.this, "Signed as "+ userId, Toast.LENGTH_SHORT).show();
                    Log.d("AuthStateChanged", "Logged in as "+ user.getUid());
                    Intent intent = new Intent(FirebaseLogInActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Log.d("AuthStateChanged", "Signed Out");
                }
            }
        };

        View.OnClickListener logInListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mEmailEntry.getText().toString().isEmpty()) {
                    mEmailEntry.setError("Can't be empty");
                }
                if (mPasswordEntry.getText().toString().isEmpty()) {
                    mPasswordEntry.setError("Password field is empty");
                } else {
                    String email = mEmailEntry.getText().toString();
                    String password = mPasswordEntry.getText().toString();

                    mPasswordEntry.setText("");
                    switch (view.getId()) {
                        case (R.id.sign_up_button):
                            createNewUserWithEmail(email,password);
                            Intent intent = new Intent(FirebaseLogInActivity.this, SelfProfileActivity.class);
                            intent.putExtra(AppConstants.ORIGIN_ACTIVITY,AppConstants.ORIGIN_LOG_IN);
                            startActivity(intent);
                            break;
                        case (R.id.sign_in_button):
                            signInUserWithEmail(email, password);
                            break;
                        default:
                            Toast.makeText(FirebaseLogInActivity.this, "Well....\n this is embarassing", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        mSignIn.setOnClickListener(logInListener);
        mSignUp.setOnClickListener(logInListener);
    }

    public void findViews(){
        mEmailEntry= (EditText)findViewById(R.id.email_entry);
        mPasswordEntry = (EditText)findViewById(R.id.password_entry);
        mSignUp = (Button)findViewById(R.id.sign_up_button);
        mSignIn = (Button)findViewById(R.id.sign_in_button);
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
                        if (!task.isSuccessful()) {
                            Toast.makeText(FirebaseLogInActivity.this, "An Error Occurred",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            String userId = mAuth.getCurrentUser().getUid();
                            if(!SelfUserProfileUtils.compareStoredIdWithCurrentId(FirebaseLogInActivity.this,userId)){ //If logged in id doesn't match id stored in sharedPref.
                                SelfUserProfileUtils.clearUserProfile(FirebaseLogInActivity.this); //We clear the user Preferences. This is a back-up check; typically the preferences should be cleared on sign-out.
                                SelfUserProfileUtils.setUserId(FirebaseLogInActivity.this, userId);
                            }
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
                        if (!task.isSuccessful()) {
                            Log.w("Sign in", "signInWithEmail:failed", task.getException());
                            Toast.makeText(FirebaseLogInActivity.this, "Password doesn't match username",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(FirebaseLogInActivity.this, "Logged in as blah-blah", Toast.LENGTH_SHORT).show();
                            String userId = mAuth.getCurrentUser().getUid();
                            if(!SelfUserProfileUtils.compareStoredIdWithCurrentId(FirebaseLogInActivity.this,userId)){ //If logged in id doesn't match id stored in sharedPref.
                                SelfUserProfileUtils.clearUserProfile(FirebaseLogInActivity.this); //We clear the user Preferences. This is a back-up check; typically the preferences should be cleared on sign-out.
                                SelfUserProfileUtils.setUserId(FirebaseLogInActivity.this, userId);
                            }
                            //ToDo: On Successful log-in download all of user's made connections.
                        }

                    }
                });
    }
}
