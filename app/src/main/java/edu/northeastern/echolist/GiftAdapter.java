package edu.northeastern.echolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    private List<Gift> giftList;
    private OnGiftClickListener listener;

    public GiftAdapter(List<Gift> giftList, OnGiftClickListener listener) {
        this.giftList = giftList;
        this.listener = listener;
    }

    public void setGifts(List<Gift> gifts) {
        this.giftList = gifts;
        notifyDataSetChanged();
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
        holder.bind(gift);
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public class GiftViewHolder extends RecyclerView.ViewHolder {
        private ImageView giftImageView;
        private TextView giftNameTextView;
        // private CheckBox favoriteCheckBox;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            giftImageView = itemView.findViewById(R.id.gift_image);
            giftNameTextView = itemView.findViewById(R.id.gift_title);
            // favoriteCheckBox = itemView.findViewById(R.id.favoriteCheckBox);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onGiftClick(giftList.get(position));
                    }
                }
            });
        }

        public void bind(Gift gift) {
            // using Glide image loading library
            Glide.with(itemView.getContext()).load(gift.getImage()).into(giftImageView);

            giftNameTextView.setText(gift.getName());
            // favoriteCheckBox.setChecked(gift.isFavorite());
        }
    }

    public interface OnGiftClickListener {
        void onGiftClick(Gift gift);
    }
}