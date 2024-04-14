package edu.northeastern.echolist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<Event> eventsList;

    public EventAdapter(Context context, List<Event> eventsList) {
        this.eventsList = eventsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_event_item_layout, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(event.getDate());

        // when user taps on an item of the event recycler view, the click listener start the
        // EventDetailActivity for the clicked event
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
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


    // display the recycler view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView, EventAdapter eventAdapter) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.event_title);
            dateTextView = itemView.findViewById(R.id.event_date);
        }
    }
}
