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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.R;


//ToDo: On Sign-up we should have another edittext activity for "confirm password"

public class FirebaseLogInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static final int RC_SIGN_IN = 1414;

    private EditText mEmailEntry, mPasswordEntry;
    private Button mSignIn;
    private TextView mSignUp;
    private Toolbar mToolbar;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_log_in);

        findViews();

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this , this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

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
                switch (view.getId()) {
                    case (R.id.sign_up_text):
                        Intent intent = new Intent(FirebaseLogInActivity.this, SignUpActivity.class);
                        startActivity(intent);
                        break;
                    case (R.id.sign_in_button):
                        if (mEmailEntry.getText().toString().isEmpty()) {
                            mEmailEntry.setError("Can't be empty");
                        }
                        if (mPasswordEntry.getText().toString().isEmpty()) {
                            mPasswordEntry.setError("Password field is empty");
                        } else {
                            String email = mEmailEntry.getText().toString();
                            String password = mPasswordEntry.getText().toString();

                            mPasswordEntry.setText("");
                            signInUserWithEmail(email, password);
                        }
                        break;
                    default:
                        Toast.makeText(FirebaseLogInActivity.this, "Well....\n this is embarassing", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mSignIn.setOnClickListener(logInListener);
        mSignUp.setOnClickListener(logInListener);
    }

    public void findViews(){
        mEmailEntry= (EditText)findViewById(R.id.email_entry);
        mPasswordEntry = (EditText)findViewById(R.id.password_entry);
        mSignUp = (TextView) findViewById(R.id.sign_up_text);
        mSignIn = (Button)findViewById(R.id.sign_in_button);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);

//
//        SignInButton signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
//        signInButton.setOnClickListener(this);
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
                                ConnectionsSQLOpenHelper.getInstance(FirebaseLogInActivity.this).clearDatabase();
                            }
                            //ToDo: On Successful log-in download all of user's made connections.
                        }

                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

//    @Override
//    public void onClick(View view) {
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleSignInResult(result);
//        }
//    }
//
//    private void handleSignInResult(GoogleSignInResult result) {
//        if (result.isSuccess()) {
//            // Signed in successfully, show authenticated UI.
//            GoogleSignInAccount acct = result.getSignInAccount();
//            SelfUserProfileUtils.setUserId(this, acct.getId());
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        } else {
//            // Signed out, show unauthenticated UI.
//        }
//    }
}
