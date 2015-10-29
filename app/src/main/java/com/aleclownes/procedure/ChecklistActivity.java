package com.aleclownes.procedure;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChecklistActivity extends AppCompatActivity {

    private static final String TAG = "ChecklistActivity";
    Checklist checklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //TODO add support for opening both working and master checklists from different parts of the app here
        checklist = new MasterChecklist();
        //Add a new checklist header
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        fabCh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistHeader("Test Header"));
                ChecklistActivity.this.refreshChecklist();
            }
        });
        //Add a new checklist item
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        fabCi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistEntry("Test Item"));
                ChecklistActivity.this.refreshChecklist();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void refreshChecklist(){
        final LinearLayout ll = (LinearLayout) findViewById(R.id.checklistLinearLayout);
        for (int i = 0; i < ll.getChildCount(); i++){
            View child = ll.getChildAt(i);
            Log.d(TAG, child.getClass().toString());
            if (child instanceof EditText){
                ChecklistItem item = checklist.getItems().get(i);
                Log.d(TAG, ((EditText) child).getText().toString());
                item.setText(((EditText) child).getText().toString());
            }
        }
        ll.removeAllViews();

        for (ChecklistItem ci : checklist.getItems()){
            // Add text
            final TextView tv = new TextView(this);
            tv.setText(ci.getText());
            if (ci instanceof ChecklistHeader){
                tv.setTypeface(null, Typeface.BOLD);
            }
            tv.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    int index = ll.indexOfChild(view);
                    ll.removeView(view);
                    EditText et = new EditText(ChecklistActivity.this);
                    et.setText(tv.getText());
                    ll.addView(et, index);
                }
            });
            ll.addView(tv);
        }
    }

}
