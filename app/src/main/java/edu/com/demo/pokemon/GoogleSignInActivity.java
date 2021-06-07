package edu.com.demo.pokemon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;

public class GoogleSignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 34234;
    private static final String TAG = "SIGN-IN";
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_signup);
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();


        // creating a variable for our BiometricManager
        // and lets check if our user can use biometric sensor or not
        BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        // this means we can use biometric sensor
        int i = biometricManager.canAuthenticate();
        // check for all cases
        if (i == BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(getApplicationContext(), "Please authenticate", Toast.LENGTH_SHORT).show();
            // this means that the device doesn't have fingerprint sensor
        } else if (i == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            Toast.makeText(getApplicationContext(), "This device doesn't have a fingerprint sensor", Toast.LENGTH_SHORT).show();
            // this means that biometric sensor is not available
        } else if (i == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
            Toast.makeText(getApplicationContext(), "The biometric sensor is currently unavailable", Toast.LENGTH_SHORT).show();
            // this means that the device doesn't contain your fingerprint
        } else if (i == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            Toast.makeText(getApplicationContext(), "Your device doesn't have fingerprint saved, please check your security settings", Toast.LENGTH_SHORT).show();
        }
        // creating a variable for our Executor
        Executor executor = ContextCompat.getMainExecutor(this);
        // this will give us result of AUTHENTICATION
        biometricPrompt = new BiometricPrompt(GoogleSignInActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // User canceled the operation
                if (errorCode == BiometricPrompt.ERROR_CANCELED || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    // you can either show the dialog again here
                    biometricPrompt.authenticate(promptInfo);
                }
            }

            // THIS METHOD IS CALLED WHEN AUTHENTICATION IS SUCCESS
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authentication Success", Toast.LENGTH_SHORT).show();
                // login after successful authentication
                FirebaseUser currentUser = mAuth.getCurrentUser();
                updateUI(currentUser);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        // creating a variable for our promptInfo
        // BIOMETRIC DIALOG
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Pokedex")
                .setDescription("Please authenticate using fingerprint")
                .setNegativeButtonText("Cancel").build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        biometricPrompt.authenticate(promptInfo);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + (account != null ? account.getId() : null));
                firebaseAuthWithGoogle(account != null ? account.getIdToken() : null);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser signInAccount) {
        if (signInAccount != null) {
            final String personName = signInAccount.getDisplayName();
            final String personEmail = signInAccount.getEmail();
            final String token = signInAccount.getUid();
            Intent intent = new Intent();
            intent.setClass(GoogleSignInActivity.this, MainActivity.class);
            intent.putExtra("USER", personName).putExtra("EMAIL", personEmail).putExtra("TOKEN", token);
            startActivity(intent);
            finish();
            Log.d(TAG, "Account " + personEmail);
        }
    }

//    public void signOut() {
//        mGoogleSignInClient.signOut()
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.d(TAG, "Signed Out");
//                    }
//                });
//    }
}
