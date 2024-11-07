package AndroidAssignment.notespro;

import android.app.IntentService;
import android.content.Intent;
import AndroidAssignment.notespro.Utility;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ReminderService extends IntentService {
    public ReminderService() { super("ReminderService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String dueDate = document.getString("dueDate");
                            String dueTime = document.getString("dueTime");

                            if (isDeadlineApproaching(dueDate, dueTime)) {
                                sendEmailReminder(document.getString("title"));
                            }
                        }
                    }
                });
    }

    private boolean isDeadlineApproaching(String dueDate, String dueTime) {
        // Logic to determine if the deadline is within 24 hours
        return true;
    }

    private void sendEmailReminder(String title) {
        // Code to send email notification using JavaMail API or similar library
    }
}
