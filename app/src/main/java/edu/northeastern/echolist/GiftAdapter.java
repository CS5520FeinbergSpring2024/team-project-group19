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
        this.favoriteListener = favoriteListener;
        this.cardListener = cardListener;
    }

    public void setGifts(List<Gift> gifts) {
        this.giftList = gifts;
        notifyDataSetChanged();
    }

    public void setFavoritedGiftIds(List<String> favoritedGiftIds) {
        this.favoritedGiftIds = favoritedGiftIds;
        notifyDataSetChanged(); // notify the adapter to re-bind all view holders
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
        holder.bind(gift, isFavorite);
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

            favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Gift gift = giftList.get(position);
                    if (favoriteListener != null) {
                        favoriteListener.onGiftFavoriteChanged(gift, isChecked);
                    }
                }
            });
        }

        public void bind(Gift gift, boolean isFavorite) {
            // using Glide image loading library
            Glide.with(itemView.getContext()).load(gift.getImage()).into(giftImageView);

            giftNameTextView.setText(gift.getName());
            favoriteCheckBox.setChecked(isFavorite);
        }
    }

    public interface OnGiftClickListener {
        void onGiftClick(Gift gift);
    }

    public interface OnGiftFavoriteListener {
        void onGiftFavoriteChanged(Gift gift, boolean isFavorite);
    }
}