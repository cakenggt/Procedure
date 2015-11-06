package com.aleclownes.procedure;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    List<Checklist> checklists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        final ListView listView = (ListView) findViewById(R.id.checklistListListView);
        final ChecklistListAdapter adapter = new ChecklistListAdapter(this, checklists);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        Log.d(TAG, "Added adapter");

        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        for (Checklist check : checklistManager.getAllChecklists()){
            checklists.add(check);
        }
        ((ArrayAdapter<Checklist>)((ListView)findViewById(R.id.checklistListListView)).getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checklist_list_context_menu, menu);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        int checklistIndex = (int) info.id;
        Checklist checklist = checklists.get(checklistIndex);
        switch (item.getItemId()) {
            case R.id.delete:
                checklistManager.delete(checklists.get((int) info.id).getId());
                checklists.remove(checklistIndex);
                ((ArrayAdapter<Checklist>)((ListView)findViewById(R.id.checklistListListView)).getAdapter()).notifyDataSetChanged();
                return true;
            case R.id.clone:
                Log.d(TAG, "on click, creating working checklist");
                Checklist working = new Checklist(checklist);
                checklistManager.create(working);
                Intent editIntent = new Intent(MainActivity.this, ChecklistActivity.class);
                editIntent.putExtra(ChecklistActivity.ID_KEY, working.getId());
                editIntent.putExtra(ChecklistActivity.CHECKLIST_TYPE_KEY, ChecklistActivity.EDIT_MODE);
                startActivity(editIntent);
            default:
                return super.onContextItemSelected(item);
        }
    }

    public class ChecklistListAdapter extends ArrayAdapter<Checklist> {
        private final Context context;
        private final List<Checklist> checklists;

        public ChecklistListAdapter(Context context, List<Checklist> checklists){
            super(context, -1, checklists);
            this.context = context;
            this.checklists = checklists;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.checklist_list_row, parent, false);
            Log.d(TAG, "getview");
            final Checklist checklist = checklists.get(position);
            TextView textView = (TextView)rowView.findViewById(R.id.checklistText);
            textView.setText(checklist.getTitle());
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(MainActivity.this, ChecklistActivity.class);
                    editIntent.putExtra(ChecklistActivity.ID_KEY, checklist.getId());
                    startActivity(editIntent);
                }
            });
            rowView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MainActivity.this.openContextMenu(v);
                    return true;
                }
            });
            //Setting text in circle icon
            TextView circleIcon = (TextView)rowView.findViewById(R.id.circle_icon);
            int contents = 0;
            for (ChecklistItem item : checklist.getItems()){
                if (item instanceof ChecklistEntry){
                    contents++;
                }
            }
            circleIcon.setText(Integer.toString(contents));
            return rowView;
        }

    }
}
