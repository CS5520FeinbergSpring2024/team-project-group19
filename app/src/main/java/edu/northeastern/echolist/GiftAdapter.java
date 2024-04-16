package edu.northeastern.echolist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    private List<Gift> giftList;
    private List<String> favoritedGiftIds;
    private final OnGiftFavoriteListener favoriteListener;
    private final OnGiftClickListener cardListener;


    public GiftAdapter(List<Gift> giftList, List<String> favoritedGiftIds, OnGiftFavoriteListener favoriteListener, OnGiftClickListener cardListener) {
        this.giftList = giftList;
        this.favoritedGiftIds = favoritedGiftIds;
        Log.d("GiftAdapter", "Favorite IDs set: " + favoritedGiftIds);
        this.favoriteListener = favoriteListener;
        this.cardListener = cardListener;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_gift_layout, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        Gift gift = giftList.get(position);
        if (gift.getGiftId() == null) {
            Log.e("GiftAdapter", "Gift at position " + position + " has a null giftId");
        }

        boolean isFavorite = favoritedGiftIds.contains(gift.getGiftId());
        Log.d("GiftAdapter", "Gift ID: " + gift.getGiftId() + ", isFavorite: " + isFavorite);

        holder.giftNameTextView.setText(gift.getName());

        // using Glide image loading library
        Glide.with(holder.itemView.getContext()).load(gift.getImage()).into(holder.giftImageView);

        holder.favoriteCheckBox.setOnCheckedChangeListener(null);
        holder.favoriteCheckBox.setChecked(isFavorite);
        holder.favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (favoriteListener != null) {
                favoriteListener.onGiftFavoriteChanged(gift, isChecked);
            }
        });
        holder.favoriteCheckBox.setVisibility(View.VISIBLE);

        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                holder.favoriteCheckBox.setOnCheckedChangeListener(null);
                holder.favoriteCheckBox.setChecked(isFavorite);
                holder.favoriteCheckBox.setVisibility(View.VISIBLE);
                holder.favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (favoriteListener != null) {
                        favoriteListener.onGiftFavoriteChanged(gift, isChecked);
                    }
                });
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull GiftViewHolder holder) {
        super.onViewRecycled(holder);
        holder.favoriteCheckBox.setOnCheckedChangeListener(null);
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public class GiftViewHolder extends RecyclerView.ViewHolder {
        private final ImageView giftImageView;
        private final TextView giftNameTextView;
        private final CheckBox favoriteCheckBox;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            giftImageView = itemView.findViewById(R.id.gift_image);
            giftNameTextView = itemView.findViewById(R.id.gift_title);
            favoriteCheckBox = itemView.findViewById(R.id.favorite);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && cardListener != null) {
                    cardListener.onGiftClick(giftList.get(position));
                }
            });
        }
    }

    public interface OnGiftClickListener {
        void onGiftClick(Gift gift);
    }

    public interface OnGiftFavoriteListener {
        void onGiftFavoriteChanged(Gift gift, boolean isFavorite);
    }
}