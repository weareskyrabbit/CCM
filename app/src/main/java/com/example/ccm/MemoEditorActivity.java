package com.example.ccm;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class MemoEditorActivity extends AppCompatActivity {
    MemoOpenHelper helper = null;
    boolean newFlag = false;
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_editor);

        if(helper == null){
            helper = new MemoOpenHelper(MemoEditorActivity.this);
        }

        Intent intent = this.getIntent();
        id = intent.getStringExtra("id");
        if (id.equals("")) {
            newFlag = true;
        } else {
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                Cursor c = db.rawQuery("select body from MEMO_TABLE where uuid = '" + id + "'", null);
                boolean next = c.moveToFirst();
                while (next) {
                    String dispBody = c.getString(0);
                    EditText body = findViewById(R.id.body);
                    body.setText(dispBody, TextView.BufferType.NORMAL);
                    next = c.moveToNext();
                }
            } finally {
                db.close();
            }
        }

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText body = findViewById(R.id.body);
                String bodyStr = body.getText().toString();

                SQLiteDatabase db = helper.getWritableDatabase();
                try {
                    if (newFlag) {
                        id = UUID.randomUUID().toString();
                        // INSERT
                        db.execSQL("insert into MEMO_TABLE(uuid, body) VALUES('"+ id +"', '"+ bodyStr +"')");
                    } else {
                        // UPDATE
                        db.execSQL("update MEMO_TABLE set body = '"+ bodyStr +"' where uuid = '"+id+"'");
                    }
                } finally {
                    db.close();
                }
                Intent intent = new Intent(MemoEditorActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
