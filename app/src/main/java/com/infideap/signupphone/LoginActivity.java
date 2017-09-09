package com.infideap.signupphone;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private View signUpLayout;
    private View verificationLayout;

    private EditText phoneNoEditext;
    private EditText verificationCodeEditext;

    private Button okayButton;
    private Button verifyButton;

    private FirebaseAuth auth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize all views
        signUpLayout = findViewById(R.id.layout_sign_up);
        verificationLayout = findViewById(R.id.layout_verification);

        phoneNoEditext = (EditText) findViewById(R.id.editText_phone_no);
        verificationCodeEditext = (EditText) findViewById(R.id.editText_verification_code);

        okayButton = (Button) findViewById(R.id.button_okay);
        verifyButton = (Button) findViewById(R.id.button_verify);

        okayButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);

        // initilize auth parameter
        auth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        showSignUpLayout();
    }

    /**
     * Method to show sign up form
     */
    private void showSignUpLayout() {
        signUpLayout.setVisibility(View.VISIBLE);
        verificationLayout.setVisibility(View.GONE);
    }

    /**
     * Method to show verification form
     */
    private void showVerificationLayout() {
        signUpLayout.setVisibility(View.GONE);
        verificationLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Method to handle all button's event
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_okay:
                signUp(v);
                break;
            case R.id.button_verify:
                verify(v);
                break;
        }
    }

    private void verify(View v) {

        if (verificationCodeEditext.length() == 0) {
            Snackbar.make(v, R.string.insertcode, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        String code = verificationCodeEditext.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signIn(v, credential);
    }

    /**
     * Method to handle sign up process
     *
     * @param v
     */
    private void signUp(final View v) {
        if (phoneNoEditext.length() == 0) { // if phone no field empty
            Snackbar.make(v, R.string.insertphoneno, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }


        String phoneNumber = phoneNoEditext.getText().toString();

        // Copy code from google
        // https://firebase.google.com/docs/auth/android/phone-auth
        // code to verify phone number.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signIn(v, phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            // [START_EXCLUDE]
                            phoneNoEditext.setError("Invalid phone number.");
                            // [END_EXCLUDE]
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            // [START_EXCLUDE]
                            Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                                    Snackbar.LENGTH_SHORT).show();
                            // [END_EXCLUDE]
                        }
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);

                        verificationId = s;
                        showVerificationLayout();
                    }


                });
    }

    /**
     * Method to handle sign in;
     * @param v
     * @param phoneAuthCredential
     */
    private void signIn(View v, PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            redirectToMainActivity();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                verificationCodeEditext.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                        }
                    }
                });
    }

    /**
     * Open Main Activity
     */
    private void redirectToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
