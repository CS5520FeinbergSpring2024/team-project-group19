package edu.northeastern.echolist;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GiftViewModel extends ViewModel {

    private final DatabaseReference dbRef;
    private MutableLiveData<String> userKeyLiveData;

    public GiftViewModel() {
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        userKeyLiveData = new MutableLiveData<>();
    }

    public LiveData<String> getUserKey(String userId) {
        Log.d("FirebaseData", "Checking for userId " + userId);
        dbRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d("FirebaseData1", "No data found for the query.");
                    return;
                }
                Log.d("FirebaseData2", "DataSnapshot: " + dataSnapshot.getValue());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("FirebaseData3", "User Key: " + snapshot.getKey());
                    userKeyLiveData.setValue(snapshot.getKey());
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // log error or handle cancellation
                userKeyLiveData.setValue(null);
            }
        });
        return userKeyLiveData;
    }


    public LiveData<List<String>> fetchUserFavoritedGiftIds(String userId) {
        MutableLiveData<List<String>> favoritedGiftIdsLiveData = new MutableLiveData<>();
        getUserKey(userId).observeForever(userKey -> {
            dbRef.child(userKey).child("favoritedGifts")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<String> favoritedGiftIds = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String giftId = snapshot.getKey(); // assuming the key is the gift ID
                                favoritedGiftIds.add(giftId);
                            }
                            favoritedGiftIdsLiveData.setValue(favoritedGiftIds);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // handle the error, for now setting LiveData to null
                            favoritedGiftIdsLiveData.setValue(null);
                        }
                    });
        });
        return favoritedGiftIdsLiveData;
    }

    public void addGiftToUserFavorites(String userId, String giftId) {
        getUserKey(userId).observeForever(userKey -> {
            if (userKey == null || giftId == null || giftId.isEmpty()) {
                Log.e("GiftViewModel-AddToFavorites", "Can't add, userKey (" + userKey + ") or giftId (" + giftId + ") is null or empty.");
            } else {
                // remove placeholder
                dbRef.child(userKey).child("favoritedGifts").child("placeholder").removeValue();
                // add real gift
                dbRef.child(userKey).child("favoritedGifts").child(giftId).setValue(true)
                        .addOnSuccessListener(aVoid -> Log.d("GiftViewModel-AddToFavorites", "giftId (" + giftId + ") added to " + userId + " favorites"))
                        .addOnFailureListener(e -> Log.e("GiftViewModel-AddToFavorites", "Failed to add gift to favorites", e));
            }
        });
    }

    public void removeGiftFromUserFavorites(String userId, String giftId) {
        getUserKey(userId).observeForever(userKey -> {
            if (userKey != null) {
                dbRef.child(userKey).child("favoritedGifts").child(giftId).removeValue();
            } else {
                Log.e("GiftViewModel-RemoveFromFavorites", "Can't remove giftId " + giftId);
            }
        });
    }
}

