package AndroidAssignment.notespro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String noteTitle = intent.getStringExtra("noteTitle");
        String reminderType = intent.getStringExtra("reminderType");

        // Notify user with an email (Placeholder: Toast for demonstration)
        Toast.makeText(context, "Reminder: " + noteTitle + " - " + reminderType, Toast.LENGTH_LONG).show();

        // Add email sending code here for production use
    }
}
