package com.csportfolio.eventtrackingapp;

import java.util.Objects;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * EvenStructure represents an event with properties
 * such as id, title, date, start/end times, description.
 */
public class EventStructure {

    // Event Properties
    private final String id;
    private final String title;
    private final String date;
    private final String startTime;
    private final String endTime;
    private final String description;

    /**
    * Constructs a new EventStructure
    */
    public EventStructure(String id,
                          String title,
                          String date,
                          String startTime,
                          String endTime,
                          String description) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Equality check based on all fields
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventStructure)) return false;
        EventStructure that = (EventStructure) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(date, that.date) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(description, that.description);
    }

    /**
     * Generates hash code for EventStructure object based on its fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title, date, startTime, endTime, description);
    }

    /**
     * Checks if this event conflicts with another event
     * based on overlapping time intervals on the same date
     */
    public boolean conflictsWith(EventStructure other) {
        if (!this.date.equals(other.date)) {
            return false; // events on different dates do not conflict
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime thisStart = LocalTime.parse(this.startTime, formatter);
        LocalTime thisEnd = this.endTime != null ? LocalTime.parse(this.endTime, formatter)
                : thisStart.plusHours(1);
        LocalTime otherStart = LocalTime.parse(other.startTime, formatter);
        LocalTime otherEnd = otherStart.plusHours(1);

        return thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
    }
}