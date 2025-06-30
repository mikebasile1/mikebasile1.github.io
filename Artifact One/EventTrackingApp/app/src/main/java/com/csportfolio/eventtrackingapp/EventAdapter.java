package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * EventAdapter is an adapter class to bind a list of EventStructure objects to
 * RecyclerView rows for displaying event details and handling edit actions.
 */
public class EventAdapter extends ListAdapter<EventStructure, EventAdapter.EventViewHolder> {

    // Event Logging & Context
    private static final String TAG = EventAdapter.class.getSimpleName();
    private final Context context;

    // Constructor
    public EventAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    /**
     * Inflates the layout for each event row
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_event_table_row, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds the event data to the UI components of the row
     * Adds a click listener to the Edit button
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventStructure event = getItem(position);
        Log.d(TAG, "Binding event: " + event.getTitle());  // logs event

        // Formatted Labels
        holder.title.setText(context.getString(R.string.titleLabel, event.getTitle()));
        holder.date.setText(context.getString(R.string.dateLabel, event.getDate()));
        holder.time.setText(context.getString(R.string.timeRangeLabel, event.getStartTime(), event.getEndTime()));
        holder.description.setText(context.getString(R.string.descriptionLabel, event.getDescription()));

        // Click Listener - Edit Button
        holder.editButton.setOnClickListener(v -> {
            Log.d(TAG, "Edit button clicked for event: " + event.getTitle());  // logs event
            Intent intent = new Intent(context, UpdateEvent.class);
            intent.putExtra("id", event.getId());
            context.startActivity(intent);
        });
    }

    /**
     * ViewHolder that holds references to UI components in each row
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, description;
        Button editButton;

        EventViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.date_to_fire);
            time = itemView.findViewById(R.id.time_to_fire);
            description = itemView.findViewById(R.id.event_description);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }

    /**
     * DiffUtil callback to efficiently detect changes in event data
     */
    private static final DiffUtil.ItemCallback<EventStructure> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull EventStructure oldItem,
                                               @NonNull EventStructure newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull EventStructure oldItem,
                                                  @NonNull EventStructure newItem) {
                    return oldItem.equals(newItem);
                }
            };
}