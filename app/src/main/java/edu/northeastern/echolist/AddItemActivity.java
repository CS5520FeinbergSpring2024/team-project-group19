package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddItemActivity extends AppCompatActivity {
    protected TextView userIdTextView;
    private Button saveEventButton;
    private EditText eventTitle;
    private EditText eventLocation;
    private EditText eventDate;
    private Spinner friendsSpinner;
    private Spinner categorySpinner;
    private Spinner visibilitySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_add_post);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        // Set the user ID to the TextView
        userIdTextView = findViewById(R.id.userId_textview);
        userIdTextView.setText(userId);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        saveEventButton = findViewById(R.id.saveEventButton);
        eventTitle = findViewById(R.id.eventTitle);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        friendsSpinner = findViewById(R.id.friendsSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        visibilitySpinner = findViewById(R.id.visibilitySpinner);

        eventDate.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             showDateDialog();
                                         }
                                     });

                saveEventButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String title = eventTitle.getText().toString();
                        String location = eventLocation.getText().toString();
                        String date = eventDate.getText().toString();

                        String eventId = databaseEvents.push().getKey();
                        Event event = new Event(eventId, userId, title, location, date);


                        databaseEvents.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        eventDate.setText(selectedDate);
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}