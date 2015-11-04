package com.aleclownes.procedure;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private static final String TAG = "ChecklistActivity";
    public static final String ID_KEY = "com.aleclownes.procedure.id";
    public final static String CHECKLIST_TYPE_KEY = "com.aleclownes.procedure.mode";
    public static final String EDIT_MODE = "EDIT";
    public static final String CHECK_MODE = "CHECK";
    Checklist checklist;
    ChecklistMode mode;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final RecyclerView listView = (RecyclerView) findViewById(R.id.checklistListView);
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        Intent intent = getIntent();
        if (intent != null && intent.getLongExtra(ID_KEY, 0) != 0){
            checklist = checklistManager.read(intent.getLongExtra(ID_KEY, 0));
            if (intent.getStringExtra(CHECKLIST_TYPE_KEY) != null){
                if (intent.getStringExtra(CHECKLIST_TYPE_KEY).equals(EDIT_MODE)){
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
        else{
            checklist = new Checklist();
            checklistManager.create(checklist);
            switchToEditMode();
        }
        //Add adapter
        final ChecklistAdapter adapter = new ChecklistAdapter(this, checklist.getItems(), R.id.handle);
        listView.setAdapter(adapter);
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
                adapter.setSelected(newChildIndex);
                listView.getChildAt(newChildIndex).setSelected(true);
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
                int newChildIndex = checklist.getItems().size() - 1;
                adapter.setSelected(newChildIndex);
                listView.getChildAt(newChildIndex).setSelected(true);
            }
        });
        //Clear items
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (ChecklistItem item : ChecklistActivity.this.checklist.getItems()){
                    if (item instanceof ChecklistEntry){
                        ((ChecklistEntry)item).setChecked(false);
                    }
                }
                adapter.notifyDataSetChanged();
                fabClear.hide();
            }
        });
        TextView title = (TextView)findViewById(R.id.editTitle);
        TextView textTitle = (TextView)findViewById(R.id.textTitle);
        title.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "setting text of title to " + s.toString());
                checklist.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        title.setText(checklist.getTitle());
        textTitle.setText(checklist.getTitle());
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
        startActivity(listIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checklist, menu);
        this.menu = menu;
        if (mode == ChecklistMode.CHECK){
            menu.getItem(0).setIcon(R.drawable.ic_mode_edit_white_24dp);
        }
        else if (mode == ChecklistMode.EDIT){
            menu.getItem(0).setIcon(R.drawable.ic_list_white_24dp);
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
            case R.id.action_settings:
                //noinspection SimplifiableIfStatement
                return true;
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

    public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>{
        private final Context context;
        private final List<ChecklistItem> items;
        private int selected = -1;
        private int handler;

        public ChecklistAdapter(Context context, List<ChecklistItem> items, int handler){
            super();
            this.context = context;
            this.items = items;
            this.handler = handler;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rowView = inflater.inflate(R.layout.checklist_row, parent, false);
            TextView textView;
            final ChecklistItem item = items.get(position);
            //whether the checklist is working or master
            if (mode == ChecklistMode.CHECK){
                Log.d(TAG, "working checklist");
                rowView.findViewById(R.id.itemEdit).setVisibility(View.GONE);
                //Put check box if entry
                if (item instanceof ChecklistEntry){
                    final ChecklistEntry entry = (ChecklistEntry)item;
                    rowView.findViewById(R.id.itemText).setVisibility(View.GONE);
                    final CheckBox check = (CheckBox)rowView.findViewById(R.id.checkBox);
                    textView = check;
                    if (entry.isChecked()){
                        check.setChecked(true);
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            entry.setChecked(isChecked);
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
                            for (ChecklistItem item : checklist.getItems()) {
                                if (item instanceof ChecklistEntry) {
                                    if (((ChecklistEntry) item).isChecked()) {
                                        fabClear.show();
                                        show = true;
                                        break;
                                    }
                                }
                            }
                            if (!show) {
                                fabClear.hide();
                            }
                        }
                    });
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

        @Override
        public ChecklistAdapter.ChecklistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ChecklistViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ChecklistViewHolder extends RecyclerView.ViewHolder {

            public final CheckBox checkBox;
            public final ImageView handle;
            public final TextView itemText;
            public final EditText itemEdit;
            public final ImageView delete;

            public ChecklistViewHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox)itemView.findViewById(R.id.checkBox);
                handle = (ImageView)itemView.findViewById(R.id.handle);
                itemText = (TextView)itemView.findViewById(R.id.itemText);
                itemEdit = (EditText)itemView.findViewById(R.id.itemEdit);
                delete = (ImageView)itemView.findViewById(R.id.delete);
            }
        }
    }

    private void switchToEditMode(){
        setTitle("Edit Checklist");
        mode = ChecklistMode.EDIT;
        EditText editTitle = (EditText)findViewById(R.id.editTitle);
        TextView textTitle = (TextView)findViewById(R.id.textTitle);
        editTitle.setVisibility(View.VISIBLE);
        textTitle.setVisibility(View.GONE);
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
            menu.getItem(0).setIcon(R.drawable.ic_list_white_24dp);
            menu.getItem(0).setTitle(R.string.check_mode);
        }
    }

    private void switchToCheckMode(){
        mode = ChecklistMode.CHECK;
        EditText editTitle = (EditText)findViewById(R.id.editTitle);
        TextView textTitle = (TextView)findViewById(R.id.textTitle);
        textTitle.setText(checklist.getTitle());
        textTitle.setVisibility(View.VISIBLE);
        editTitle.setVisibility(View.GONE);
        FloatingActionButton fabCh = (FloatingActionButton) findViewById(R.id.fab_ch);
        FloatingActionButton fabCi = (FloatingActionButton) findViewById(R.id.fab_ci);
        FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
        DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListView);
        fabCh.hide();
        fabCi.hide();
        boolean show = false;
        for (ChecklistItem item : checklist.getItems()){
            if (item instanceof ChecklistEntry){
                if (((ChecklistEntry)item).isChecked()){
                    fabClear.show();
                    show = true;
                    break;
                }
            }
        }
        if (!show){
            fabClear.hide();
        }
        setTitle("Checklist");
        listView.setDivider(null);
        //Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (menu != null) {
            menu.getItem(0).setIcon(R.drawable.ic_mode_edit_white_24dp);
            menu.getItem(0).setTitle(R.string.edit_mode);
        }
    }

}
