package edu.northeastern.echolist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddWishlistItem extends Fragment {
    private TextWatcher textWatcher;
    private NavigationRouter navigationRouter;
    private DatabaseReference events;
    private String userId;
    private EditText giftTitle;
    private EditText descriptionTitle;
    private Spinner eventSpinner;

    public AddWishlistItem(NavigationRouter navigationRouter, DatabaseReference events, String userId) {
        this.navigationRouter = navigationRouter;
        this.events = events;
        this.userId = userId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_wishlist_item, container, false);

        // watch for text input
        initTextWatcher();

        // Init input fields
        // title
        giftTitle = view.findViewById(R.id.giftTitle);
        giftTitle.addTextChangedListener(textWatcher);

        // description
        descriptionTitle = view.findViewById(R.id.descriptionTitle);
        descriptionTitle.addTextChangedListener(textWatcher);

        // event selection
        eventSpinner = view.findViewById(R.id.eventSpinner);
        // get the user's events
        Query query = events
                .orderByChild("userId")
                .equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> eventTitles = new ArrayList<>();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        eventTitles.add(event.getTitle());
                    }
                }
                ArrayAdapter<String> eventAdapter = new ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        eventTitles
                );
                eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                eventSpinner.setAdapter(eventAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void initTextWatcher() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the flag based on whether any text is entered
                navigationRouter.setTextEntered(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }
}