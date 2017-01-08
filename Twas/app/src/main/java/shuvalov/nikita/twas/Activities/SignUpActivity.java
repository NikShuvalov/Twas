package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.R;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private EditText mUsername, mPassword, mConfirm;
    private Button mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth= FirebaseAuth.getInstance();

        findViews();
        mSignUp.setOnClickListener(this);
    }

    public void findViews(){
        mSignUp = (Button)findViewById(R.id.sign_up_button);
        mUsername = (EditText)findViewById(R.id.username_entry);
        mPassword = (EditText)findViewById(R.id.password_entry);
        mConfirm = (EditText)findViewById(R.id.password_confirm_entry);
    }

    public void createNewUserWithEmail(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Log-In Activity", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            if(task.getException().getClass().equals(FirebaseAuthUserCollisionException.class)){
                                Toast.makeText(SignUpActivity.this, "Username taken", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(SignUpActivity.this, "Something went wrong",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            String userId = mAuth.getCurrentUser().getUid();
                            if(!SelfUserProfileUtils.compareStoredIdWithCurrentId(SignUpActivity.this,userId)){ //If logged in id doesn't match id stored in sharedPref.
                                SelfUserProfileUtils.clearUserProfile(SignUpActivity.this); //We clear the user Preferences. This is a back-up check; typically the preferences should be cleared on sign-out.
                                SelfUserProfileUtils.setUserId(SignUpActivity.this, userId);
                                ConnectionsSQLOpenHelper.getInstance(SignUpActivity.this).clearDatabase();
                            }
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(!mPassword.getText().toString().equals(mConfirm.getText().toString())){
            mPassword.setError("Passwords don't match");
            mConfirm.setError("Passwords don't match");
        }else{
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null && networkInfo.isConnected()){
                createNewUserWithEmail(mUsername.getText().toString().trim(), mPassword.getText().toString());
            }else{
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
