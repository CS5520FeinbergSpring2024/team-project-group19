package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private TextView userIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userIdTextView = findViewById(R.id.userId_textview);

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User not found");


        userIdTextView.setText("User ID: " + userId);


        if ("User not found".equals(userId)) {
            Toast.makeText(this, "User not found. Please sign in.", Toast.LENGTH_SHORT).show();
        }
    }
}