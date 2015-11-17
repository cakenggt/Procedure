package com.aleclownes.procedure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.List;

/**
 * Created by alownes on 11/16/2015.
 */
public class ChecklistSyncTask extends AsyncTask<String, Void, List<Checklist>> {
    private String baseUrl = "http://10.0.2.2:8000/procedure/";
    private final ArrayAdapter adapter;
    private final List<Checklist> checklists;
    private final Context context;
    private static String TAG = "MainActivity";

    public static final String GET_ALL_CHECKLISTS = "get-all-checklists/";
    public static final String CREATE_CHECKLIST = "create-checklist/";
    public static final String LOGIN = "api-token-auth/";

    public ChecklistSyncTask(ArrayAdapter adapter, List<Checklist> checklists, Context context){
        this.adapter = adapter;
        this.checklists = checklists;
        this.context = context;
    }

    @Override
    protected List<Checklist> doInBackground(String... params) {
        // params comes from the execute() call. Pass in stringified object for json as index 1
        String type = params[0];
        String token = "";
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key),
                Context.MODE_PRIVATE);
        JSONObject result;
        List<Checklist> checklistList = new ArrayList<>();
        checklistList.addAll(this.checklists);
        switch(type) {
            case GET_ALL_CHECKLISTS:
                token = sharedPref.getString(context.getString(R.string.token_key), "");
                result = doRequest(new byte[0], "GET", baseUrl + GET_ALL_CHECKLISTS, token);
                buildAndUpdateChecklists(checklistList, result);
                break;
            case CREATE_CHECKLIST:
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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return checklistList;
    }

    @Override
    protected void onPostExecute(List<Checklist> checklistList) {
        if (isCancelled()) {
            checklistList = null;
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
                strFileContents = new String(contents, 0, bytesRead);
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
        List<Checklist> retrieved = new ArrayList<Checklist>();
        try {
            JSONArray checklists = json.getJSONArray("checklists");
            for (int i = 0; i < checklists.length(); i++){
                retrieved.add(new Checklist(checklists.getJSONObject(i)));
            }
            checklistList.clear();
            checklistList.addAll(retrieved);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
