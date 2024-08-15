package com.sproj.arimagerecognizer;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class LogEvent {
    private final String email;
    private final FirebaseFirestore db;

    public LogEvent(String email) {
        this.email = email;
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> logNewEvent(Map<String, Object> logData, String question) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        logData.put("timestamp", calendar.getTime());
        logData.put("email", this.email);

        // Format the date
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));

        // Print the formatted date
        System.out.println("Formatted Date: " + formattedDate);

        return db.collection("Events/" + this.email + "/LoggedEvent")
                .document(question + formattedDate)
                .set(logData);
    }

    public Task<QuerySnapshot> getTodayTop(boolean onlyCorrect) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date todayEnd = calendar.getTime();

        if (onlyCorrect) {
            return db.collectionGroup("LoggedEvent").where(
                    Filter.and(
                            Filter.greaterThanOrEqualTo("timestamp", todayStart),
                            Filter.lessThan("timestamp", todayEnd),
                            Filter.equalTo("answer", true)
                    )
            ).get();
        } else {
            return db.collectionGroup("LoggedEvent").where(
                    Filter.and(
                            Filter.greaterThanOrEqualTo("timestamp", todayStart),
                            Filter.lessThan("timestamp", todayEnd)
                    )
            ).get();
        }


    }

}
