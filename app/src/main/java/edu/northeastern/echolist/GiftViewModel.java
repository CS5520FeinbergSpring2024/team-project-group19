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
import java.util.Collections;
import java.util.List;


public class GiftViewModel extends ViewModel {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private final DatabaseReference userRef = dbRef.child("users");
    private final DatabaseReference giftsRef = dbRef.child("gifts");
    private final MutableLiveData<List<String>> favoriteGiftIdsLiveData = new MutableLiveData<>();

    public GiftViewModel() {}

    public void getUserKey(String userId, OnUserKeyFetchedListener listener) {
        userRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String userKey = snapshot.getKey();
                                Log.d("GiftViewModel", "User key received: " + userKey);
                                listener.onUserKeyFetched(userKey);
                                return;
                            }
                        } else {
                            Log.e("GiftViewModel", "No user found for userId: " + userId);
                            listener.onUserKeyFetched(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("GiftViewModel", "Database error fetching user key: " + databaseError.getMessage());
                        listener.onUserKeyFetched(null);
                    }
            });
    }

    public LiveData<List<String>> getFavoriteGiftIds() {
        return favoriteGiftIdsLiveData;
    }

    public void fetchTrendingGifts(int maxGifts, DataLoadCallback<Gift> callback) {
        giftsRef.orderByChild("isTrending").equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Gift> gifts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Gift gift = snapshot.getValue(Gift.class);
                    if (gift != null) {
                        gifts.add(gift);
                    }
                }
                if (!gifts.isEmpty()) {
                    List<Gift> selectedGifts = selectRandomGifts(gifts, maxGifts);
                    callback.onDataLoaded(selectedGifts);
                } else {
                    callback.onDataNotAvailable("No trending gifts found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onDataNotAvailable(error.getMessage());
            }
        });
    }

    public void fetchUserFavorites(String userId) {
        getUserKey(userId, userKey -> {
            if (userKey != null) {
                DatabaseReference userFavoritesRef = dbRef.child("users").child(userKey).child("favoritedGifts");
                userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> favoriteGiftIds = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String giftId = snapshot.getKey();
                            favoriteGiftIds.add(giftId);
                        }
                        favoriteGiftIdsLiveData.setValue(favoriteGiftIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("fetchUserFavorites", databaseError.getMessage());
                    }
                });
            } else {
                Log.e("fetchUserFavorites", "User key not found.");
            }
        });
    }

    private List<Gift> selectRandomGifts(List<Gift> gifts, int max) {
        Collections.shuffle(gifts);
        return gifts.subList(0, Math.min(max, gifts.size()));
    }

    public void addGiftToUserFavorites(String userId, String giftId, OperationCallback callback) {
        getUserKey(userId, userKey -> {
            if (userKey != null) {
                dbRef.child("users").child(userKey).child("favoritedGifts").child(giftId).setValue(true)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            } else {
                callback.onError("User key not found.");
            }
        });
    }

    public void removeGiftFromUserFavorites(String userId, String giftId, OperationCallback callback) {
        getUserKey(userId, userKey -> {
            if (userKey != null) {
                dbRef.child("users").child(userKey).child("favoritedGifts").child(giftId).removeValue()
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            } else {
                callback.onError("User key not found.");
            }
        });
    }

    public interface OnUserKeyFetchedListener {
        void onUserKeyFetched(String userKey);
    }

    public interface DataLoadCallback<T> {
        void onDataLoaded(List<T> data);
        void onDataNotAvailable(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }
}