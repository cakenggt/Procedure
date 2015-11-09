package com.aleclownes.procedure;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    List<Checklist> checklists = new ArrayList<>();
    private ChecklistMode mode;
    public static String MODE_KEY = "MODE";
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null){
            mode = ChecklistMode.valueOf(savedInstanceState.getCharSequence(MODE_KEY).toString());
        }
        else {
            mode = ChecklistMode.CHECK;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChecklistActivity.class);
                startActivity(intent);
            }
        });

        final DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListListView);
        final ChecklistListAdapter adapter = new ChecklistListAdapter(this, checklists, R.id.handle);
        listView.setDragNDropAdapter(adapter);
        registerForContextMenu(listView);
        Log.d(TAG, "Added adapter");

        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        for (Checklist check : checklistManager.getAllChecklists()){
            checklists.add(check);
        }
        ((ArrayAdapter<Checklist>)((ListView)findViewById(R.id.checklistListListView)).getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        //When orientation changes
        super.onSaveInstanceState(outState);
        outState.putCharSequence(MODE_KEY, mode.toString());
    }

    @Override
    public void onPause(){
        super.onPause();
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        checklistManager.saveAllChecklists(checklists);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.switch_mode:
                if (mode == ChecklistMode.CHECK) {
                    switchToEditMode();
                } else {
                    switchToCheckMode();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchToEditMode(){
        mode = ChecklistMode.EDIT;
        DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListListView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        listView.setDivider(new ColorDrawable(0xFFFFFF));
        listView.setDividerHeight(1);
        final ChecklistListAdapter adapter = (ChecklistListAdapter)listView.getAdapter();
        listView.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
            @Override
            public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
                Log.d(TAG, "Begin dragging");
            }

            @Override
            public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
                List<Checklist> items = checklists;
                Checklist item = items.get(startPosition);
                items.remove(startPosition);
                items.add(endPosition, item);
                adapter.notifyDataSetChanged();
            }
        });
        if (menu != null) {
            menu.getItem(0).setTitle(R.string.check_mode);
            menu.getItem(0).setIcon(null);
        }
        ((ChecklistListAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

    private void switchToCheckMode(){
        mode = ChecklistMode.CHECK;
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        checklistManager.saveAllChecklists(checklists);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.show();
        DragNDropListView listView = (DragNDropListView) findViewById(R.id.checklistListListView);
        if (menu != null) {
            menu.getItem(0).setIcon(R.drawable.ic_mode_edit_white_24dp);
        }
        ((ChecklistListAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

    public class ChecklistListAdapter extends DragNDropArrayAdapter<Checklist> {
        private final Context context;
        private final List<Checklist> checklists;
        private int selected = -1;

        public ChecklistListAdapter(Context context, List<Checklist> checklists, int handler){
            super(context, -1, checklists, handler);
            this.context = context;
            this.checklists = checklists;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View colView = inflater.inflate(R.layout.checklist_list_row, parent, false);
            final View rowView = colView.findViewById(R.id.rowLayout);
            TextView textView;
            final Checklist item = checklists.get(position);
            //whether the checklist is working or master
            if (mode == ChecklistMode.CHECK){
                rowView.findViewById(R.id.itemEdit).setVisibility(View.GONE);
                //Render item
                TextView circleIcon = (TextView)rowView.findViewById(R.id.circle_icon);
                int contents = 0;
                for (ChecklistItem iteme : item.getItems()){
                    if (iteme instanceof ChecklistEntry){
                        contents++;
                    }
                }
                circleIcon.setText(Integer.toString(contents));
                textView = (TextView)rowView.findViewById(R.id.checklistText);
                rowView.findViewById(R.id.delete).setVisibility(View.GONE);
                //Remove handle
                rowView.findViewById(R.id.handle).setVisibility(View.GONE);
                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editIntent = new Intent(MainActivity.this, ChecklistActivity.class);
                        editIntent.putExtra(ChecklistActivity.ID_KEY, item.getId());
                        startActivity(editIntent);
                    }
                });
            }
            else if (mode == ChecklistMode.EDIT){
                textView = (EditText)rowView.findViewById(R.id.itemEdit);
                rowView.findViewById(R.id.checklistText).setVisibility(View.GONE);
                rowView.findViewById(R.id.circle_icon).setVisibility(View.GONE);
                ImageView button = (ImageView)rowView.findViewById(R.id.delete);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checklists.remove(position);
                        ChecklistListAdapter.this.notifyDataSetChanged();
                    }
                });
            }
            else{
                //If something strange happens, put it in a text view
                textView = (TextView)rowView.findViewById(R.id.checklistText);
            }
            textView.setText(item.getTitle());

            //Set listeners
            textView.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "setting text of position " + position + " to " + s.toString());
                    item.setTitle(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Log.d(TAG, "position " + position + " clicked");
                        ChecklistListAdapter.this.setSelected(position);
                    }
                }
            });

            //If this one was previously selected, keep it in focus
            if (position == this.selected){
                Log.d(TAG, "requesting focus for position " + position);
                textView.requestFocus();
            }
            else if (this.selected > checklists.size()){
                this.selected = -1;
            }

            return colView;
        }

        public void setSelected(int selected){
            this.selected = selected;
        }

    }
}
