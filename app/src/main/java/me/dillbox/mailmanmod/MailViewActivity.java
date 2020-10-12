package me.dillbox.mailmanmod;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MailViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_view);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            ((TextView) findViewById(R.id.sender_value)).setText(b.getString("sender"));
            ((TextView) findViewById(R.id.mail_value)).setText(b.getString("email"));
            ((TextView) findViewById(R.id.subject_value)).setText(b.getString("subject"));
            ((TextView) findViewById(R.id.received_value)).setText(b.getString("received"));
            ((EditText) findViewById(R.id.headers_text)).setText(b.getString("headers"));
            ((EditText) findViewById(R.id.message_text)).setText(b.getString("message"));
        }
    }
}