package com.aleclownes.procedure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private static final String TAG = "ChecklistActivity";
    Checklist checklist;
    ChecklistMode mode;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListView);
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        final ChecklistAdapter adapter;
        Intent intent = getIntent();
        if (intent != null && intent.getLongExtra(ChecklistUtility.ID_KEY, 0) != 0){
            checklist = checklistManager.read(intent.getLongExtra(ChecklistUtility.ID_KEY, 0));
            //Add adapter
            adapter = new ChecklistAdapter(this, checklist.getItems(), R.id.handle);
            listView.setDragNDropAdapter(adapter);
            if (intent.getStringExtra(ChecklistUtility.CHECKLIST_TYPE_KEY) != null){
                if (intent.getStringExtra(ChecklistUtility.CHECKLIST_TYPE_KEY).equals(ChecklistUtility.EDIT_MODE)){
                    switchToEditMode();
                }
                else{
                    switchToCheckMode();
                }
            }
            else {
                switchToCheckMode();
            }
        }
        else if (savedInstanceState != null){
            //Saved a checklist id from a new checklist
            checklist = checklistManager.read(savedInstanceState.getLong(ChecklistUtility.ID_KEY));
            adapter = new ChecklistAdapter(this, checklist.getItems(), R.id.handle);
            listView.setDragNDropAdapter(adapter);
            switchToEditMode();
        }
        else{
            checklist = new Checklist();
            checklistManager.create(checklist);
            //Add adapter
            adapter = new ChecklistAdapter(this, checklist.getItems(), R.id.handle);
            listView.setDragNDropAdapter(adapter);
            switchToEditMode();
        }
        if (savedInstanceState != null){
            mode = ChecklistMode.valueOf(savedInstanceState.getCharSequence(ChecklistUtility.MODE_KEY).toString());
            if (ChecklistMode.CHECK.equals(mode)){
                switchToCheckMode();
            }
            else{
                switchToEditMode();
            }
        }
        setTitle(checklist.getTitle());
        Log.d(TAG, "Added adapter");
        //Add a new checklist header
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        fabCh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistActivity.this.checklist.getItems().add(new ChecklistItem());
                Log.d(TAG, "Header add button clicked");
                adapter.notifyDataSetChanged();
                int newChildIndex = checklist.getItems().size()-1;
                adapter.setSelected(newChildIndex);
                listView.setSelection(newChildIndex);
            }
        });
        //Add a new checklist item
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        fabCi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChecklistItem item = new ChecklistItem();
                item.setCheckable(true);
                ChecklistActivity.this.checklist.getItems().add(item);
                Log.d(TAG, "Item add button clicked");
                adapter.notifyDataSetChanged();
                int newChildIndex = checklist.getItems().size() - 1;
                adapter.setSelected(newChildIndex);
                listView.setSelection(newChildIndex);
            }
        });
        //Clear items
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (ChecklistItem item : ChecklistActivity.this.checklist.getItems()) {
                    if (item.isCheckable()) {
                        item.setChecked(false);
                    }
                }
                adapter.notifyDataSetChanged();
                fabClear.hide();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        //When orientation changes
        super.onSaveInstanceState(outState);
        outState.putCharSequence(ChecklistUtility.MODE_KEY, mode.toString());
        outState.putLong(ChecklistUtility.ID_KEY, checklist.getId());
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
        startActivity(listIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checklist, menu);
        this.menu = menu;
        if (checklist.getParentId() == null) {
            if (mode == ChecklistMode.CHECK) {
                menu.getItem(0).setTitle(R.string.edit_mode);
                menu.getItem(0).setIcon(R.drawable.ic_mode_edit_white_24dp);
            } else if (mode == ChecklistMode.EDIT) {
                menu.getItem(0).setTitle(R.string.check_mode);
                menu.getItem(0).setIcon(null);
            }
        }
        else{
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent listIntent = new Intent(this, MainActivity.class);
                listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(listIntent);
                return true;
            case R.id.switch_mode:
                if (mode == ChecklistMode.CHECK){
                    switchToEditMode();
                }
                else{
                    switchToCheckMode();
                }
                return true;
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
            View colView = inflater.inflate(R.layout.checklist_row, parent, false);
            final View rowView = colView.findViewById(R.id.rowLayout);
            TextView textView;
            final ChecklistItem item = items.get(position);
            //whether the checklist is working or master
            if (mode == ChecklistMode.CHECK){
                Log.d(TAG, "working checklist");
                rowView.findViewById(R.id.itemEdit).setVisibility(View.GONE);
                //Put check box if entry
                if (item.isCheckable()){
                    rowView.findViewById(R.id.itemText).setVisibility(View.GONE);
                    final CheckBox check = (CheckBox)rowView.findViewById(R.id.checkBox);
                    textView = check;
                    if (item.isChecked()){
                        check.setChecked(true);
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            item.setChecked(isChecked);
                            if (isChecked) {
                                //Add strikethrough
                                buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            } else {
                                //Remove strikethrough
                                buttonView.setPaintFlags(buttonView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                            //show the clear button if there are any checked entries
                            FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
                            boolean show = false;
                            for (ChecklistItem item : checklist.getItems()){
                                if (item.isCheckable()){
                                    if (item.isChecked()){
                                        fabClear.show();
                                        show = true;
                                        break;
                                    }
                                }
                            }
                            if (!show){
                                fabClear.hide();
                            }
                        }
                    });
                    colView.findViewById(R.id.divider).setVisibility(View.GONE);
                }
                else{
                    //Is a header
                    textView = (TextView)rowView.findViewById(R.id.itemText);
                    rowView.findViewById(R.id.checkBox).setVisibility(View.GONE);
                }
                //Remove Delete button
                rowView.findViewById(R.id.delete).setVisibility(View.GONE);
                //Remove handle
                rowView.findViewById(R.id.handle).setVisibility(View.GONE);
            }
            else if (mode == ChecklistMode.EDIT){
                Log.d(TAG, "master checklist");
                textView = (EditText)rowView.findViewById(R.id.itemEdit);
                rowView.findViewById(R.id.itemText).setVisibility(View.GONE);
                rowView.findViewById(R.id.checkBox).setVisibility(View.GONE);
                ImageView button = (ImageView)rowView.findViewById(R.id.delete);
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
            if (!item.isCheckable()){
                textView.setTextColor(getResources().getColor(R.color.gray1));
                if (position != 0) {
                    colView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                } else {
                    colView.findViewById(R.id.divider).setVisibility(View.GONE);
                }
            }
            else {
                colView.findViewById(R.id.divider).setVisibility(View.GONE);
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

            return colView;
        }

        public void setSelected(int selected){
            this.selected = selected;
        }

    }

    private void switchToEditMode(){
        if (checklist.getParentId() != null){
            switchToCheckMode();
            return;
        }
        mode = ChecklistMode.EDIT;
        DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListView);
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
        fabCh.show();
        fabCi.show();
        fabClear.hide();
        listView.setDivider(new ColorDrawable(0xFFFFFF));
        listView.setDividerHeight(1);
        final ChecklistAdapter adapter = (ChecklistAdapter)listView.getAdapter();
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
        if (menu != null) {
            menu.getItem(0).setTitle(R.string.check_mode);
            menu.getItem(0).setIcon(null);
        }
        ((ChecklistAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

    private void switchToCheckMode(){
        mode = ChecklistMode.CHECK;
        ChecklistUtility.hideSoftKeyboard(this);
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
        DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListView);
        fabCh.hide();
        fabCi.hide();
        boolean show = false;
        for (ChecklistItem item : checklist.getItems()){
            if (item.isCheckable()){
                if (item.isChecked()){
                    fabClear.show();
                    show = true;
                    break;
                }
            }
        }
        if (!show){
            fabClear.hide();
        }
        listView.setDivider(null);
        //Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (menu != null) {
            menu.getItem(0).setIcon(R.drawable.ic_mode_edit_white_24dp);
        }
        ((ChecklistAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

}
