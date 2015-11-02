package com.aleclownes.procedure;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private static final String TAG = "ChecklistActivity";
    public static final String ID_KEY = "com.aleclownes.procedure.id";
    Checklist checklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListView);
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
        final ChecklistAdapter adapter = new ChecklistAdapter(this, checklist.getItems(), R.id.handle);
        listView.setDragNDropAdapter(adapter);
        Log.d(TAG, "Added adapter");
        //Add a new checklist header
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        fabCh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistHeader());
                Log.d(TAG, "Header add button clicked");
                adapter.notifyDataSetChanged();
                int newChildIndex = checklist.getItems().size()-1;
                listView.setSelection(newChildIndex);
            }
        });
        //Add a new checklist item
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        fabCi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistEntry());
                Log.d(TAG, "Item add button clicked");
                adapter.notifyDataSetChanged();
                int newChildIndex = checklist.getItems().size()-1;
                listView.setSelection(newChildIndex);
            }
        });
        TextView title = (TextView)findViewById(R.id.editTitle);
        title.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "setting text of title to " + s.toString());
                checklist.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        if (checklist instanceof WorkingChecklist){
            fabCh.hide();
            fabCi.hide();
            setTitle("Checklist");
            listView.setDivider(null);
            //Keep screen on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //Disable autofocus for title
            findViewById(R.id.titleContainer).setFocusableInTouchMode(true);
        }
        else{
            setTitle("Edit Checklist");
            listView.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
                @Override
                public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
                    Log.d(TAG, "Begin dragging");
                }

                @Override
                public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
                    List<ChecklistItem> items = checklist.getItems();
                    ChecklistItem item = items.get(startPosition);
                    items.remove(startPosition);
                    items.add(endPosition, item);
                    adapter.notifyDataSetChanged();
                }
            });
        }
        title.setText(checklist.getTitle());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        checklistManager.update(checklist);
    }

    @Override
    public void onBackPressed() {
        Intent listIntent = new Intent(this, MainActivity.class);
        if (checklist instanceof WorkingChecklist){
            listIntent.putExtra(MainActivity.CHECKLIST_TYPE_KEY, MainActivity.WORKING_CHECKLIST_KEY);
        }
        else {
            listIntent.putExtra(MainActivity.CHECKLIST_TYPE_KEY, MainActivity.MASTER_CHECKLIST_KEY);
        }
        startActivity(listIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                //noinspection SimplifiableIfStatement
                return true;
            case android.R.id.home:
                if (this.checklist instanceof WorkingChecklist){
                    Intent listIntent = new Intent(this, MainActivity.class);
                    listIntent.putExtra(MainActivity.CHECKLIST_TYPE_KEY, MainActivity.WORKING_CHECKLIST_KEY);
                    listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(listIntent);
                    return true;
                }
                else{
                    Intent listIntent = new Intent(this, MainActivity.class);
                    listIntent.putExtra(MainActivity.CHECKLIST_TYPE_KEY, MainActivity.MASTER_CHECKLIST_KEY);
                    listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(listIntent);
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public class ChecklistAdapter extends DragNDropArrayAdapter<ChecklistItem> {
        private final Context context;
        private final List<ChecklistItem> items;
        private int selected = -1;

        public ChecklistAdapter(Context context, List<ChecklistItem> items, int handler){
            super(context, -1, items, handler);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rowView = inflater.inflate(R.layout.checklist_row, parent, false);
            TextView textView;
            final ChecklistItem item = items.get(position);
            //whether the checklist is working or master
            if (checklist instanceof WorkingChecklist){
                Log.d(TAG, "working checklist");
                textView = (TextView)rowView.findViewById(R.id.itemText);
                rowView.findViewById(R.id.itemEdit).setVisibility(View.GONE);
                //Put check box if entry
                if (item instanceof WorkingChecklistEntry){
                    final WorkingChecklistEntry entry = (WorkingChecklistEntry)item;
                    final CheckBox check = new CheckBox(context);
                    if (entry.isChecked()){
                        check.setChecked(true);
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    check.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((View)v.getParent()).performClick();
                        }
                    });
                    ((ViewGroup) rowView).addView(check, 0,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                    rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            entry.setChecked(!entry.isChecked());
                            check.setChecked(entry.isChecked());
                            TextView textView = (TextView) rowView.findViewById(R.id.itemText);
                            if (entry.isChecked()) {
                                //Add strikethrough
                                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            } else {
                                //Remove strikethrough
                                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                        }
                    });
                }
                //Remove Delete button
                rowView.findViewById(R.id.delete).setVisibility(View.GONE);
                //Remove handle
                rowView.findViewById(R.id.handle).setVisibility(View.GONE);
            }
            else if (checklist instanceof MasterChecklist){
                Log.d(TAG, "master checklist");
                textView = (EditText)rowView.findViewById(R.id.itemEdit);
                rowView.findViewById(R.id.itemText).setVisibility(View.GONE);
                ImageButton button = (ImageButton)rowView.findViewById(R.id.delete);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.remove(position);
                        ChecklistAdapter.this.notifyDataSetChanged();
                    }
                });
            }
            else{
                //If something strange happens, put it in a text view
                textView = (TextView)rowView.findViewById(R.id.itemText);
            }
            if (item instanceof ChecklistHeader){
                textView.setTypeface(null, Typeface.BOLD);
                textView.setPadding(10, 20, 0, 20);
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

    }

}
