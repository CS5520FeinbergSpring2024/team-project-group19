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

    private List<GiftItem> giftList;
    private final OnGiftFavoriteListener favoriteListener;
    private final OnGiftClickListener cardListener;

    public GiftAdapter(List<GiftItem> giftList, OnGiftFavoriteListener favoriteListener, OnGiftClickListener cardListener) {
        this.giftList = giftList;
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
        GiftItem giftItem = giftList.get(position);
        holder.favoriteCheckBox.setVisibility(View.VISIBLE);
        Log.d("GiftAdapter", "onBindViewHolder called for gift at position " + giftList.get(position));
        Gift gift = giftItem;

        holder.giftNameTextView.setText(gift.getName());
        Glide.with(holder.itemView.getContext()).load(gift.getImage()).into(holder.giftImageView);

        holder.favoriteCheckBox.setOnCheckedChangeListener(null);
        holder.favoriteCheckBox.setChecked(giftItem.isFavorite());
        holder.favoriteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (favoriteListener != null) {
                favoriteListener.onGiftFavoriteChanged(gift, isChecked);
            }
        });
        holder.favoriteCheckBox.setVisibility(View.VISIBLE);
        Log.d("GiftAdapter", "Checkbox for " + gift.getGiftId() + " is " + holder.favoriteCheckBox.getVisibility());
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public void setGiftItems(List<GiftItem> giftItems) {
        this.giftList = giftItems;
        notifyDataSetChanged();
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