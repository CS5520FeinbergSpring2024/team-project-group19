package edu.northeastern.echolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Gravity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {
    private EditText userIdEditText, passwordEditText;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        userIdEditText = findViewById(R.id.userIdEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button signInButton = findViewById(R.id.signInButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        signInButton.setOnClickListener(v -> signIn());
        createAccountButton.setOnClickListener(v -> openCreateAccountActivity());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to MainActivity
            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn() {
        String userId = userIdEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            Toast toast = Toast.makeText(SignInActivity.this, "Please enter user ID and password", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            // highlight color for empty input field.
            userIdEditText.setBackgroundResource(R.color.errorHighlight);
            passwordEditText.setBackgroundResource(R.color.errorHighlight);

            new Handler().postDelayed(() -> {
                userIdEditText.setBackgroundResource(android.R.color.transparent);
                passwordEditText.setBackgroundResource(android.R.color.transparent);
            }, 2000);
            return;
        }

        databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getPassword().equals(password)) {
                            openHomeActivity(user);
                            return;
                        }
                    }
                    // Password does not match
                    Toast.makeText(SignInActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                } else {
                    // User does not exist
                    Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("SignInActivity", "Database error: " + databaseError.getMessage());
                Toast.makeText(SignInActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openHomeActivity(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", user.getUserId());
        editor.apply();

        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();

    }
    private void openCreateAccountActivity() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }
}