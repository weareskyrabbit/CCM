package com.example.ccm;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class MemoListActivity extends AppCompatActivity {
    private MemoOpenHelper helper = null;
    String mSearchWord;
    private ListView memoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        if (helper == null) {
            helper = new MemoOpenHelper(MemoListActivity.this);
        }
        final ArrayList<HashMap<String, String>> memoList = new ArrayList<>();
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select uuid, body from MEMO_TABLE order by id", null);
            boolean next = c.moveToFirst();

            while (next) {
                HashMap<String, String> data = new HashMap<>();
                String uuid = c.getString(0);
                String body = c.getString(1);
                if (body.contains("\n")) {
                    body = body.split("\n")[0];
                } else if (body.length() > 10) {
                    body = body.substring(0, 10) + "...";
                }
                data.put("body",body);
                data.put("id",uuid);
                memoList.add(data);
                next = c.moveToNext();
            }
        } finally {
            db.close();
        }

        final SimpleAdapter simpleAdapter = new SimpleAdapter(this,
                memoList,
                android.R.layout.simple_list_item_2,
                new String[]{"body","id"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        this.memoList = findViewById(R.id.memoList);
        this.memoList.setAdapter(simpleAdapter);

        // click: edit
        this.memoList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MemoListActivity.this, MemoEditorActivity.class);

                TwoLineListItem two = (TwoLineListItem)view;
                TextView idTextView = two.getText2();
                String idStr = (String) idTextView.getText();
                intent.putExtra("id", idStr);
                startActivity(intent);
            }
        });

        // long click: delete
        this.memoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TwoLineListItem two = (TwoLineListItem)view;
                TextView idTextView = two.getText2();
                String idStr = (String) idTextView.getText();

                SQLiteDatabase db = helper.getWritableDatabase();
                try {
                    db.execSQL("DELETE FROM MEMO_TABLE WHERE uuid = '"+ idStr +"'");
                } finally {
                    db.close();
                }
                memoList.remove(position);
                simpleAdapter.notifyDataSetChanged();

                return true;
            }
        });

        // new
        Button newButton = findViewById(R.id.new_button);
        newButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemoListActivity.this, MemoEditorActivity.class);
                intent.putExtra("id", ""); // TODO
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        // setDatabaseDataBySearchWord(mSearchWord);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String searchWord) {
                // setDatabaseDataBySearchWord(searchWord);
                mSearchWord = searchWord;
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}