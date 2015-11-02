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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = "MainActivity";
    public final static String CHECKLIST_TYPE_KEY = "com.aleclownes.procedure.checklistType";
    public final static String MASTER_CHECKLIST_KEY = "MASTER";
    public final static String WORKING_CHECKLIST_KEY = "WORKING";
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ListView listView = (ListView) findViewById(R.id.checklistListListView);
        final ChecklistListAdapter adapter = new ChecklistListAdapter(this, checklists);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        Log.d(TAG, "Added adapter");

        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(CHECKLIST_TYPE_KEY) != null){
            switch (intent.getStringExtra(CHECKLIST_TYPE_KEY)){
                case MASTER_CHECKLIST_KEY:
                    switchToMasterMode();
                    break;
                case WORKING_CHECKLIST_KEY:
                    switchToWorkingMode();
                    break;
                default:
                    switchToMasterMode();
                    break;
            }
        }
        else{
            if (checklistManager.getAllWorkingChecklists().size() > 0){
                //If there are any working checklists, switch to working mode
                switchToWorkingMode();
            }
            else{
                switchToMasterMode();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.master) {
            switchToMasterMode();
        } else if (id == R.id.working) {
            switchToWorkingMode();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checklist_list_context_menu, menu);
        if (checklists.get(0) instanceof WorkingChecklist){
            //Remove clone option for working checklists
            menu.removeItem(R.id.clone);
        }
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
                WorkingChecklist working;
                if (checklist instanceof MasterChecklist){
                    //Create a new working checklist
                    working = new WorkingChecklist((MasterChecklist)checklist);
                    checklistManager.create(working);
                }
                else{
                    working = (WorkingChecklist)checklist;
                }
                Intent editIntent = new Intent(MainActivity.this, ChecklistActivity.class);
                editIntent.putExtra(ChecklistActivity.ID_KEY, working.getId());
                startActivity(editIntent);
            default:
                return super.onContextItemSelected(item);
        }
    }

    @SuppressWarnings("unchecked")
    private void switchToWorkingMode(){
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        ((FloatingActionButton)findViewById(R.id.fab)).hide();
        checklists.clear();
        for (Checklist check : checklistManager.getAllWorkingChecklists()){
            checklists.add(check);
        }
        ((ArrayAdapter<Checklist>)((ListView)findViewById(R.id.checklistListListView)).getAdapter()).notifyDataSetChanged();
        setTitle("Working Checklists");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(1).setChecked(true);
    }

    @SuppressWarnings("unchecked")
    private void switchToMasterMode(){
        ChecklistManager checklistManager = new ChecklistManagerImpl(this);
        ((FloatingActionButton)findViewById(R.id.fab)).show();
        checklists.clear();
        for (Checklist check : checklistManager.getAllMasterChecklists()){
            checklists.add(check);
        }
        ((ArrayAdapter<Checklist>)((ListView)findViewById(R.id.checklistListListView)).getAdapter()).notifyDataSetChanged();
        setTitle("Master Checklists");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
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
            return rowView;
        }

    }
}
