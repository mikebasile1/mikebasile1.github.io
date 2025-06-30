package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * UserLoginMain handles user login functionality.
 * It validates input, manages login attempts, and navigates
 * to the event home screen.
 */
public class UserLoginMain extends AppCompatActivity {

    // Event Logging
    private static final String TAG = "UserLoginMain";

    // UI Components
    private EditText username, password;

    // Database
    private UserDatabase DB;

    // Login Attempt Tracking
    private static final int MAX_ATTEMPTS = 5;
    private int loginAttempts = 0;
    private boolean isLockedOut = false;
    private static final long LOCKOUT_DURATION_MS = 60000;  // 1 minute

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_main);

        // Initialize Views
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.login_button);
        Button registerBtn = findViewById(R.id.register_button);

        // Initializes Database
        DB = new UserDatabase(this);

        // Login Button Handler
        loginBtn.setOnClickListener(v -> {
            hideKeyboard();  // hides keyboard
            loginBtn.setEnabled(false);  // disables button to prevent multiple clicks

            if (isLockedOut) {  // checks account lockout
                Log.w(TAG, "Login blocked — account locked out");  // logs event
                Toast.makeText(this, getString(R.string.tooManyAttempts),
                        Toast.LENGTH_LONG).show();
                loginBtn.setEnabled(true);
                return;
            }

            String usernameInput = username.getText().toString().trim().toLowerCase();
            String passwordInput = password.getText().toString().trim();

            // Input Validation
            if (validateInput(usernameInput, passwordInput)) {
                Log.d(TAG, "Attempting login for: " + usernameInput);  // logs event

                boolean checkLogin = DB.verifyCredentials(usernameInput, passwordInput);

                if (checkLogin) {
                    Log.d(TAG, "Login successful for: " + usernameInput);  // logs event
                    Toast.makeText(this, getString(R.string.loginSuccess),
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), EventDisplayHome.class));
                } else {
                    Log.w(TAG, "Login failed for: " + usernameInput);  // logs event
                    Toast.makeText(this, getString(R.string.loginFailed),
                            Toast.LENGTH_SHORT).show();
                    password.setText("");  // clears password field
                    loginAttempts++;

                    // checks login attempts
                    if (loginAttempts >= MAX_ATTEMPTS) {
                        isLockedOut = true;
                        Log.w(TAG, "Login lockout triggered after " + MAX_ATTEMPTS +
                                " failed attempts");  // logs event
                        Toast.makeText(this, getString(R.string.lockoutMessage),
                                Toast.LENGTH_LONG).show();

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            isLockedOut = false;
                            loginAttempts = 0;
                            Log.d(TAG, "Lockout period ended — login re-enabled");  // logs event
                        }, LOCKOUT_DURATION_MS);
                    }
                }
            }

            // enables login button
            loginBtn.setEnabled(true);
        });

        // Registration Button Handler
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UserRegistration.class)));
    }

    /**
     * Hides the soft keyboard from the screen
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Validates the login input fields
     */
    private boolean validateInput(String usernameInput, String passwordInput) {
        if (usernameInput.isEmpty()) {
            username.requestFocus();
            Toast.makeText(this, getString(R.string.emptyUsername), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordInput.isEmpty()) {
            password.requestFocus();
            Toast.makeText(this, getString(R.string.emptyPassword), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordInput.length() < 8) {
            password.requestFocus();
            Toast.makeText(this, getString(R.string.passwordLength), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}