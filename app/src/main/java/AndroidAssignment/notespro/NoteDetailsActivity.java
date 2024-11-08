package AndroidAssignment.notespro;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NoteDetailsActivity extends AppCompatActivity {
    EditText titleEditText, contentEditText;
    ImageButton saveNoteBtn;
    TextView pageTitleTextView, deleteNoteTextViewBtn, textViewDueDate, textViewDueTime;
    String docId;
    boolean isEditMode = false;
    final Calendar calendar = Calendar.getInstance();
    private AlarmManager alarmManager;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn);
        textViewDueDate = findViewById(R.id.textViewDueDate);
        textViewDueTime = findViewById(R.id.textViewDueTime);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        textViewDueDate.setOnClickListener(v -> showDatePickerDialog());
        textViewDueTime.setOnClickListener(v -> showTimePickerDialog());

        // Check if it's an edit mode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isEditMode = true;
            titleEditText.setText(extras.getString("title"));
            contentEditText.setText(extras.getString("content"));
            textViewDueDate.setText(extras.getString("dueDate"));
            textViewDueTime.setText(extras.getString("dueTime"));
            docId = extras.getString("docId");
        }

        saveNoteBtn.setOnClickListener((v) -> saveNote());
        deleteNoteTextViewBtn.setOnClickListener((v) -> deleteNoteFromFirebase());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            textViewDueDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            textViewDueTime.setText(hourOfDay + ":" + (minute < 10 ? "0" + minute : minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        String dueDate = textViewDueDate.getText().toString();
        String dueTime = textViewDueTime.getText().toString();

        if (noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        Map<String, Object> noteData = new HashMap<>();
        noteData.put("title", noteTitle);
        noteData.put("content", noteContent);
        noteData.put("timestamp", Timestamp.now());
        noteData.put("dueDate", dueDate);
        noteData.put("dueTime", dueTime);

        saveNoteToFirebase(noteData);

        // Schedule reminders
        scheduleReminders(noteTitle, dueDate, dueTime);
    }

    private void saveNoteToFirebase(Map<String, Object> noteData) {
        DocumentReference documentReference;
        if (isEditMode) {
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        } else {
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }

        documentReference.set(noteData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, "Note saved successfully");
                    finish();
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed while saving note");
                }
            }
        });
    }

    private void deleteNoteFromFirebase() {
        if (docId == null) return;

        DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, "Note deleted successfully");
                    finish();
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed while deleting note");
                }
            }
        });
    }

    private void scheduleReminders(String title, String dueDate, String dueTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(dueDate + " " + dueTime));
            long deadlineMillis = calendar.getTimeInMillis();

            scheduleAlarm(deadlineMillis - (7 * 24 * 60 * 60 * 1000), title, "1 week before deadline");
            scheduleAlarm(deadlineMillis - (3 * 24 * 60 * 60 * 1000), title, "3 days before deadline");
            scheduleAlarm(deadlineMillis - (24 * 60 * 60 * 1000), title, "1 day before deadline");
            scheduleAlarm(deadlineMillis - (60 * 1000), title, "1 minute before deadline");

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void scheduleAlarm(long triggerTime, String noteTitle, String reminderType) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("noteTitle", noteTitle);
        intent.putExtra("reminderType", reminderType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) (triggerTime / 1000),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
}
