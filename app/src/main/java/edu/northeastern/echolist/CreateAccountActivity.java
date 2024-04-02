package edu.northeastern.echolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText userIdEditText, passwordEditText;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        userIdEditText = findViewById(R.id.userIdEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        createAccountButton.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String userId = userIdEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            Toast toast = Toast.makeText(this, "Please enter user ID and password", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            userIdEditText.setBackgroundResource(R.color.errorHighlight);
            passwordEditText.setBackgroundResource(R.color.errorHighlight);

            new Handler().postDelayed(() -> {
                userIdEditText.setBackgroundResource(android.R.color.transparent);
                passwordEditText.setBackgroundResource(android.R.color.transparent);
            }, 2000);
            return;

        }

        User newUser = new User(userId, password);

        // Use push() to generate a unique key for the user
        DatabaseReference newUserRef = databaseReference.push();
        String newUserKey = newUserRef.getKey();
        newUserRef.setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", userId);
                    editor.apply();

                    Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(CreateAccountActivity.this, "Account creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}