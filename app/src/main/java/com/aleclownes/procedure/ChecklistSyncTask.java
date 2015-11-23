package com.aleclownes.procedure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alownes on 11/16/2015.
 */
public class ChecklistSyncTask extends AsyncTask<String, Void, List<Checklist>> {
    private String baseUrl = "http://10.0.2.2:8000/procedure/";
    private final ArrayAdapter adapter;
    private final List<Checklist> checklists;
    private final Checklist checklist;
    private final Context context;
    private static String TAG = "MainActivity";

    public static final String GET_ALL_CHECKLISTS = "get-all-checklists/";
    public static final String CREATE_CHECKLIST = "create-checklist/";
    public static final String LOGIN = "api-token-auth/";
    public static final String SAVE_CHECKLIST_ORDER = "save-checklist-order/";

    public ChecklistSyncTask(ArrayAdapter adapter, List<Checklist> checklists, Checklist checklist, Context context){
        this.adapter = adapter;
        this.checklists = checklists;
        this.context = context;
        this.checklist = checklist;
    }

    @Override
    protected List<Checklist> doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        // params comes from the execute() call. Pass in stringified object for json as index 1
        String type = params[0];
        String token = "";
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key),
                Context.MODE_PRIVATE);
        JSONObject result;
        List<Checklist> checklistList = new ArrayList<>();
        checklistList.addAll(this.checklists);
        ChecklistManager checklistManager = new ChecklistManagerImpl(context);
        switch(type) {
            case SAVE_CHECKLIST_ORDER:
                token = sharedPref.getString(context.getString(R.string.token_key), "");
                doRequest(("order="+jsonifyChecklistOrder()).toString().getBytes(), "POST",
                        baseUrl + SAVE_CHECKLIST_ORDER, token);
                return checklistManager.getAllChecklists();
            case GET_ALL_CHECKLISTS:
                token = sharedPref.getString(context.getString(R.string.token_key), "");
                result = doRequest(new byte[0], "GET", baseUrl + GET_ALL_CHECKLISTS, token);
                buildAndUpdateChecklists(checklistList, result);
                break;
            case CREATE_CHECKLIST:
                token = sharedPref.getString(context.getString(R.string.token_key), "");
                result = doRequest(("checklist="+params[1]).getBytes(), "POST", baseUrl + CREATE_CHECKLIST, token);
                try {
                    //This method is called on new unsaved checklists and already saved checklists
                    if (checklist.isUnSynced()){
                        //Unsaved checklist
                        checklistManager.delete(checklist.getId());
                        checklist.setId(result.getJSONObject("checklist").getLong("pk"));
                        checklist.setUnSynced(false);
                        checklistManager.create(checklist);
                    }
                    else{
                        //Already saved checklist
                        //This updates the last_modified date
                        checklistManager.update(checklist);
                    }
                    return checklistManager.getAllChecklists();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case LOGIN:
                String username = params[1];
                String password = params[2];
                StringBuilder urlParams = new StringBuilder();
                urlParams.append("username=" + username);
                urlParams.append("&password=" + password);
                byte[] postData = urlParams.toString().getBytes();
                result = doRequest(postData, "POST", baseUrl+LOGIN, token);
                try {
                    if (result.getString("token") != null){
                        token = result.getString("token");
                        SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putString(context.getString(R.string.token_key), token);
                        edit.commit();
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,
                                        "Login Successful", Toast.LENGTH_LONG).show();
                                ((MainActivity) context).menu.getItem(0).setTitle(context.getString(R.string.logout));
                            }
                        });
                        //Update checklists
                        JSONObject updateResult = doRequest(new byte[0], "GET", baseUrl + GET_ALL_CHECKLISTS, token);
                        buildAndUpdateChecklists(checklistList, updateResult);
                    }
                } catch (JSONException e) {
                    ((MainActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "Login Failed", Toast.LENGTH_LONG).show();
                            ((MainActivity) context).menu.getItem(0).setTitle(context.getString(R.string.logout));
                        }
                    });
                }
                break;
        }
        return checklistList;
    }

    @Override
    protected void onPostExecute(List<Checklist> checklistList) {
        if (isCancelled()) {
            return;
        }

        if (adapter != null) {
            if (checklistList != null && checklistList.size() > 0) {
                this.checklists.clear();
                this.checklists.addAll(checklistList);
                ChecklistManager checklistManager = new ChecklistManagerImpl(context);
                checklistManager.saveAllChecklists(this.checklists);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private JSONObject doRequest(byte[] data, String method, String urlStr, String token){
        HttpURLConnection urlConnection = null;
        String strFileContents = "";
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            if (method.equals("POST")) {
                urlConnection.setDoOutput(true);
            }
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty( "charset", "utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(data.length ));
            if (token != null && token.length() > 1) {
                urlConnection.setRequestProperty("Authorization", "Token " + token);
            }
            urlConnection.setUseCaches(false);
            if (data.length > 0) {
                try (DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream())) {
                    wr.write(data);
                }
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] contents = new byte[1024];

            int bytesRead=0;
            while( (bytesRead = in.read(contents)) != -1){
                strFileContents += new String(contents, 0, bytesRead);
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        try {
            return new JSONObject(strFileContents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    private void buildAndUpdateChecklists(List<Checklist> checklistList, JSONObject json){
        ChecklistManager checklistManager = new ChecklistManagerImpl(context);
        //These two sets help determine which checklists should be uploaded to server or deleted from local
        HashSet<Long> currentIdSet = new HashSet<>();
        HashSet<Long> receivedIdSet = new HashSet<>();
        for (Checklist checklist : checklistList){
            currentIdSet.add(checklist.getId());
        }
        try {
            JSONArray checklists = json.getJSONArray("checklists");
            for (int i = 0; i < checklists.length(); i++){
                Checklist checklist = new Checklist(checklists.getJSONObject(i));
                receivedIdSet.add(checklist.getId());
                Checklist savedChecklist = checklistManager.read(checklist.getId());
                if (savedChecklist == null){
                    checklistManager.create(checklist);
                }
                else{
                    if (savedChecklist.getParentId() == null) {
                        //Normal checklist
                        if (savedChecklist.getLastModified().after(checklist.getLastModified())) {
                            //send saved checklist to the server
                            new ChecklistSyncTask(null, new ArrayList<Checklist>(), savedChecklist, context).execute(ChecklistSyncTask.CREATE_CHECKLIST,
                                    ChecklistSyncTask.jsonifyChecklist(savedChecklist).toString());
                        } else {
                            //Update existing checklist
                            checklistManager.update(checklist);
                        }
                    }
                    else{
                        //This is a child checklist
                        HashSet<Long> checkedIds = new HashSet<>();
                        for (ChecklistItem item : savedChecklist.getItems()){
                            if (item.isCheckable() && item.isChecked()){
                                checkedIds.add(item.getId());
                            }
                        }
                        savedChecklist.getItems().clear();
                        for (ChecklistItem item : checklist.getItems()){
                            item.setChecked(checkedIds.contains(item.getId()));
                            savedChecklist.getItems().add(item);
                        }
                        //All of the checklist items have been updated with the server's copy
                        //and are rechecked according to the local checking
                        checklistManager.update(savedChecklist);
                        if (savedChecklist.getLastModified().after(checklist.getLastModified())) {
                            //The checked items need to be sent to the server
                            new ChecklistSyncTask(null, new ArrayList<Checklist>(), savedChecklist, context).execute(ChecklistSyncTask.CREATE_CHECKLIST,
                                    ChecklistSyncTask.jsonifyChecklist(savedChecklist).toString());
                        } else {
                            //Update existing checklist
                            checklistManager.update(checklist);
                        }
                    }
                }
            }
            //Find which ids in local do not appear in the server
            currentIdSet.removeAll(receivedIdSet);
            for (Long id : currentIdSet){
                Checklist checklist = checklistManager.read(id);
                if (checklist == null){
                    //If already deleted, continue
                    continue;
                }
                if (checklist.isUnSynced()){
                    //If the id is negative, post to server and update lastModified
                    new ChecklistSyncTask(null, new ArrayList<Checklist>(), checklist, context).execute(ChecklistSyncTask.CREATE_CHECKLIST,
                            ChecklistSyncTask.jsonifyChecklist(checklist).toString());
                    checklist.setUnSynced(false);
                    checklistManager.update(checklist);
                }
                else{
                    //If the id is positive, remove the local checklist
                    checklistManager.delete(id);
                }
            }
            checklistList.clear();
            checklistList.addAll(checklistManager.getAllChecklists());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject jsonifyChecklist(Checklist checklist){
        JSONObject json = new JSONObject();
        try {
            if (checklist.getId() != null) {
                json.put("pk", checklist.getId());
            }
            json.put("title", checklist.getTitle());
            json.put("parent", checklist.getParentId());
            json.put("order", checklist.getOrder());
            List<JSONObject> checklistItems = new ArrayList<>();
            for (int i = 0; i < checklist.getItems().size(); i++){
                ChecklistItem item = checklist.getItems().get(i);
                JSONObject itemJ = new JSONObject();
                itemJ.put("text", item.getText());
                itemJ.put("checkable", item.isCheckable());
                itemJ.put("checked", item.isChecked());
                if (item.getId() != null){
                    itemJ.put("pk", item.getId());
                }
                itemJ.put("order", i);
                checklistItems.add(itemJ);
            }
            json.put("items", new JSONArray(checklistItems));
            json.put("unSynced", checklist.isUnSynced());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private JSONObject jsonifyChecklistOrder(){
        JSONObject json = new JSONObject();
        List<JSONObject> checklistOrder = new ArrayList<>();
        try {
            for (Checklist checklist : checklists){
                if (checklist.isUnSynced()){
                    //If it is unsynced, the server will try to delete a non-existing checklist.
                    //Prevent this from happening.
                    continue;
                }
                JSONObject cJson = new JSONObject();
                cJson.put("pk", checklist.getId());
                cJson.put("title", checklist.getTitle());
                checklistOrder.add(cJson);
            }
            json.put("checklists", new JSONArray(checklistOrder));
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
