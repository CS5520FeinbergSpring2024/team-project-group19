package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {
    protected TextView userIdTextView;
    private Button saveEventButton;
    private EditText eventTitle;
    private EditText eventLocation;
    private EditText eventDate;
    private Spinner friendsSpinner;
    private boolean textEntered;
    private Spinner categorySpinner;
    private Spinner visibilitySpinner;
    private Button deleteEventButton;
    private Button updateEventButton;
    private BottomNavigationView bottomNavigationView;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Button wishlistButton;
    private Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_add_post);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home || item.getItemId() == R.id.page_view_posts) {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dialog.dismiss();
                            AddItemActivity.super.onBackPressed();
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            return false;
        });
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        // Set the user ID to the TextView
        userIdTextView = findViewById(R.id.userId_textview);
        userIdTextView.setText(userId);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

    }

    @Override
    public void onBackPressed() {
        if (!textEntered) {
            super.onBackPressed();
            Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    AddItemActivity.super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void openAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                eventLocation.setText(place.getAddress());
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "User canceled autocomplete");
            }
        }
    }

}