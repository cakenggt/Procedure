package com.aleclownes.procedure;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private static final String TAG = "ChecklistActivity";
    public static final String ID_KEY = "com.aleclownes.procedure.id";
    Checklist checklist;

    public Checklist getChecklist(){
        return checklist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ListView listView = (ListView) findViewById(R.id.checklistListView);
        //TODO add support for opening both working and master checklists from different parts of the app here
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        Intent intent = getIntent();
        if (intent != null && intent.getLongExtra(ID_KEY, 0) != 0){
            checklist = checklistManager.read(intent.getLongExtra(ID_KEY, 0));
        }
        else{
            checklist = new MasterChecklist();
            checklistManager.create(checklist);
        }
        //Add adapter
        final ChecklistAdapter adapter = new ChecklistAdapter(this, checklist.getItems());
        listView.setAdapter(adapter);
        Log.d(TAG, "Added adapter");
        //Add a new checklist header
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        fabCh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistHeader("Test Header"));
                Log.d(TAG, "Header add button clicked");
                adapter.notifyDataSetChanged();
            }
        });
        //Add a new checklist item
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        fabCi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistEntry("Test Item"));
                Log.d(TAG, "Item add button clicked");
                adapter.notifyDataSetChanged();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause(){
        super.onPause();
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        checklistManager.update(checklist);
    }

    public class ChecklistAdapter extends ArrayAdapter<ChecklistItem> {
        private final Context context;
        private final List<ChecklistItem> items;
        private int selected = -1;

        public ChecklistAdapter(Context context, List<ChecklistItem> items){
            super(context, -1, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.checklist_row, parent, false);
            TextView textView;
            final ChecklistItem item = items.get(position);
            //whether the checklist is working or master
            if (checklist instanceof WorkingChecklist){
                Log.d(TAG, "working checklist");
                textView = (TextView)rowView.findViewById(R.id.itemText);
                //Put check box if entry
                if (item instanceof ChecklistEntry){
                    //TODO put checkbox
                }
            }
            else if (checklist instanceof MasterChecklist){
                Log.d(TAG, "master checklist");
                textView = (EditText)rowView.findViewById(R.id.itemEdit);
                //TODO Put delete button
                Button button = new Button(context);
                button.setText("X");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.remove(position);
                        ChecklistAdapter.this.notifyDataSetChanged();
                    }
                });
                ((ViewGroup)rowView).addView(button, 0,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            else{
                //If something strange happens, put it in a text view
                textView = (TextView)rowView.findViewById(R.id.itemText);
            }
            if (item instanceof ChecklistHeader){
                textView.setTypeface(null, Typeface.BOLD);
            }
            textView.setText(item.getText());

            //Set listeners
            textView.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "setting text of position " + position + " to " + s.toString());
                    item.setText(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Log.d(TAG, "position " + position + " clicked");
                        ChecklistAdapter.this.setSelected(position);
                    }
                }
            });

            //If this one was previously selected, keep it in focus
            if (position == this.selected){
                Log.d(TAG, "requesting focus for position " + position);
                textView.requestFocus();
            }
            else if (this.selected > items.size()){
                this.selected = -1;
            }

            return rowView;
        }

        public void setSelected(int selected){
            this.selected = selected;
        }

        public int getSelected(){
            return selected;
        }
    }

}
