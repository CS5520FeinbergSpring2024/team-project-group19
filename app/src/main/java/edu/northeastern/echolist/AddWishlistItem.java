package edu.northeastern.echolist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddWishlistItem extends Fragment {
    private DatabaseReference events;
    private DatabaseReference gifts;
    private String userId;
    private Spinner eventSpinner;
    private List<String> selectedGifts = new ArrayList<>();

    public AddWishlistItem(DatabaseReference events, String userId) {
        this.events = events;
        this.userId = userId;
        gifts = FirebaseDatabase.getInstance().getReference("gifts");
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

        // gift selection
        MultiAutoCompleteTextView giftTextView = view.findViewById(R.id.giftSelect);
        giftTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        giftTextView.setThreshold(0);
        giftTextView.setOnItemClickListener((adapterView, view1, i, l) -> {
           String selectedGift = (String) adapterView.getItemAtPosition(i);
           if (selectedGift != null) {
               selectedGifts.add(selectedGift);
           }
        });
        gifts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> giftTitles = new ArrayList<>();
                for (DataSnapshot giftSnapshot: snapshot.getChildren()) {
                    Gift gift = giftSnapshot.getValue(Gift.class);
                    if (gift != null) {
                        giftTitles.add(gift.getName());
                    }
                }
                ArrayAdapter<String> giftAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, giftTitles);
                giftTextView.setAdapter(giftAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
}