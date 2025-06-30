package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/**
 * UserRegistration handles new user account creation.
 * It includes input validation, password strength checks,
 * and registers users into the local SQLite database.
 */
public class UserRegistration extends AppCompatActivity {

    // Event Logging
    private static final String TAG = "UserRegistration";

    // UI Components
    private TextInputEditText userEmail, password1, password2;
    private Button submitBtn;

    // Database
    UserDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        // Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.topAppBarRegistration);
        setSupportActionBar(toolbar);  // sets as action bar
        toolbar.setNavigationOnClickListener(v -> finish());  // closes activity

        // Initializes Views
        userEmail = findViewById(R.id.user_email);
        password1 = findViewById(R.id.password1);
        password2 = findViewById(R.id.password2);
        submitBtn = findViewById(R.id.submit_button);

        // Initializes Database
        DB = new UserDatabase(this);

        // Submit Button Handler
        submitBtn.setOnClickListener(v -> {
            submitBtn.setEnabled(false);  // disables button to prevent multiple clicks

            String emailInput = Objects.requireNonNull(userEmail.getText()).toString().trim().toLowerCase();
            String passInput1 = Objects.requireNonNull(password1.getText()).toString().trim();
            String passInput2 = Objects.requireNonNull(password2.getText()).toString().trim();

            // Input Validation
            if (emailInput.isEmpty() || passInput1.isEmpty() || passInput2.isEmpty()) {
                Log.w(TAG, "One or more required fields empty");  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.missingField),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                enableSubmitButton();
                return;
            }

            if (emailInput.length() > 50) {  // checks if password is too long
                Log.w(TAG, "Email is too long: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.emailTooLong),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                enableSubmitButton();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                Log.w(TAG, "Invalid email format: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.invalidEmail),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                enableSubmitButton();
                return;
            }

            if (!passInput1.equals(passInput2)) {
                Log.w(TAG, "Passwords do not match for: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.passwordsNotMatching),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                enableSubmitButton();
                return;
            }

            if (!isStrongPassword(passInput1)) {  // checks if password meets requirements
                Log.w(TAG, "Weak password provided for: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.passwordRequirements),
                        Toast.LENGTH_LONG).show();
                clearPasswords();
                enableSubmitButton();
                return;
            }

            if (DB.checkUsername(emailInput)) {
                Log.w(TAG, "User already exists: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.userExists),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                enableSubmitButton();
                return;
            }

            // registers user
            boolean success = DB.addUser(emailInput, passInput1);

            if (success) {  // check registration
                Log.d(TAG, "User registered successfully: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.registrationSuccess),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                Intent intent = new Intent(getApplicationContext(), EventDisplayHome.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {  // registration failed
                Log.e(TAG, "User registration failed: " + emailInput);  // logs event
                Toast.makeText(getApplicationContext(), getString(R.string.registrationFailed),
                        Toast.LENGTH_SHORT).show();
                clearPasswords();
                enableSubmitButton();
            }
        });
    }

    /**
     * Enables the submit button after a failed attempt
     */
    private void enableSubmitButton() {
        submitBtn.setEnabled(true);
    }

    /**
     * Clears both password input fields
     */
    private void clearPasswords() {
        password1.setText("");
        password2.setText("");
    }

    /**
     * Validates if the password is strong
     */
    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*+=?-].*");
    }
}