package edu.northeastern.echolist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private List<Event> eventsList;
    private Button editButton;

    public MyListAdapter(Context context, List<Event> eventsList) {
        this.eventsList = eventsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_my_list_item, parent, false);
        return new MyListAdapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(MyListAdapter.ViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.titleTextView.setText(event.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), WishListActivity.class);
                intent.putExtra("eventId", event.getEventId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public void removeEventById(String eventId) {
        for (int i = 0; i < eventsList.size(); i++) {
            if (eventsList.get(i).getEventId().equals(eventId)) {
                eventsList.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        private MyListAdapter myListAdapter;
        public Button editButton;

        public ViewHolder(View itemView, MyListAdapter listAdapter) {
            super(itemView);
            this.titleTextView = itemView.findViewById(R.id.event_title);
            this.editButton = itemView.findViewById(R.id.editbutton);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Event event = listAdapter.eventsList.get(position);
                        Intent intent = new Intent(v.getContext(), AddItemActivity.class);
                        intent.putExtra("eventId", event.getEventId());
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }

}
