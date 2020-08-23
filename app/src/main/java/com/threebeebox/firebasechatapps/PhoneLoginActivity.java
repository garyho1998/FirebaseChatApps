package com.threebeebox.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode, CountryCode;
    private TextView PhoneText, CodeText, phoneRegister, PlusText;
    private String phoneNumber, countryCode, verifyPhone;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private static final String TAG = "PhoneLoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        CountryCode = (EditText) findViewById(R.id.country_code_input);
        PlusText = (TextView) findViewById(R.id.plus_text);
        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        PhoneText = (TextView) findViewById(R.id.phone_number_text);
        CodeText = (TextView) findViewById(R.id.code_text);
        RootRef = FirebaseDatabase.getInstance().getReference();
        loadingBar = new ProgressDialog(this);
        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                countryCode = CountryCode.getText().toString();
                phoneNumber = InputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(countryCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your country code and phone number first...", Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    verifyPhone = "+" + countryCode + phoneNumber;
                    Log.d("phoneAuth", "verifyPhone = " + verifyPhone);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            verifyPhone,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                PlusText.setVisibility(View.INVISIBLE);
                CountryCode.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                PhoneText.setVisibility(View.INVISIBLE);
                CodeText.setVisibility(View.VISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while we are verifying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Verification failed...", Toast.LENGTH_SHORT).show();
                System.out.println(e.toString());
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);
                PhoneText.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
                CodeText.setVisibility(View.INVISIBLE);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(PhoneLoginActivity.this, "Invalid request\nInvalid Phone Number...", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota has been exceeded for the project                     Toast.makeText(getApplicationContext(), "Quota exceeded", Toast.LENGTH_SHORT).show();
                    Toast.makeText(PhoneLoginActivity.this, "Quota exceeded", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("phoneAuth", "onVerificationFailed Error: " + e.getMessage());
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                PhoneText.setVisibility(View.INVISIBLE);
                PlusText.setVisibility(View.INVISIBLE);
                CountryCode.setVisibility(View.INVISIBLE);
                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
                CodeText.setVisibility(View.VISIBLE);

            }
        };

    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserID = mAuth.getCurrentUser().getUid();
                            //RootRef.child("Users").child(currentUserID).setValue("");
                            RootRef.child("Users").child(currentUserID).child("deviceToken").setValue(deviceToken);
                            //RootRef.child("Users").child(currentUserID).child("groups").setValue("");
                            Log.i(TAG, "phoneNumber:" + phoneNumber);
                            RootRef.child("Users").child(currentUserID).child("phoneNumber").setValue(verifyPhone);
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulation, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
