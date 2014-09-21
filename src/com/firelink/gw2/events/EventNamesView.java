package com.firelink.gw2.events;

import java.net.URLDecoder;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firelink.gw2.objects.APICaller;
import com.firelink.gw2.objects.EventAdapter;
import com.firelink.gw2.objects.EventCacher;
import com.firelink.gw2.objects.EventHolder;

public class EventNamesView extends Activity
{
    public static final int INTENT_SERVER_SELECTOR_REQUEST_CODE = 143;

    protected Activity activity;
    protected Context context;

    protected TextView logoTextView;
    protected ListView eventListView;
    protected ProgressDialog eventProgDialog;

    protected int serverID;
    protected String serverName;
    protected EventAdapter eventAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_names_layout);

        activity = this;
        context  = this;

        logoTextView = (TextView)findViewById(R.id.menuBar_logoTextView);
        eventListView  = (ListView)findViewById(R.id.eventNamesView_eventListView);
        eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        eventListView.setOnItemClickListener(eventSelectAdapterView);

        SharedPreferences sharedPrefs = getSharedPreferences(EventCacher.PREFS_NAME, 0);

        serverID   = sharedPrefs.getInt(EventCacher.PREFS_SERVER_ID, 0);
        serverName = sharedPrefs.getString(EventCacher.PREFS_SERVER_NAME, "Pizza");
        
        new DataCacher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
        if(serverID != 0){
            initEventView();
        } else {
            Intent intent = new Intent(this, WorldView.class);
            startActivityForResult(intent, INTENT_SERVER_SELECTOR_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == INTENT_SERVER_SELECTOR_REQUEST_CODE){
            if(resultCode == RESULT_OK){

                serverID 	= data.getIntExtra("serverID", 0);
                serverName 	= data.getStringExtra("serverName");

                initEventView();
            }
        }
    }
    
    /**
     * Initiates the events view
     * 
     * @return void
     */
    private void initEventView()
    {
        //Fix server name. Depends on size of the name
        setServerName();

        //Make list adapter to attach to listview
        //eventAdapter = new ArrayAdapter<String>(this, R.layout.event_list_text_view);

        eventAdapter = new EventAdapter(this);

        new EventSelectAPI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    /**
     * Adjusts the server name depending on the size of the name
     * 
     * @return void
     */
    private void setServerName()
    {
        logoTextView.setText(serverName);
    }

    AdapterView.OnItemClickListener eventSelectAdapterView = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Bundle bundle = new Bundle();
            EventHolder tempEvent = eventAdapter.getItem(position);

            bundle.putString("eventID", tempEvent.eventID);
            bundle.putString("eventName", tempEvent.name);

            Intent intent = new Intent(activity, EventDetailsView.class);
            intent.putExtras(bundle);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };
    
    /**
	 * This caches our background data that we might use in the future
	 */
	public class DataCacher extends AsyncTask<Void, Void, String>
	{
		 @Override
	        protected void onPreExecute()
	        {
	            super.onPreExecute();
	        }

	        @Override
	        protected String doInBackground(Void...params)
	        {
	            String result = "";
	            APICaller api = new APICaller();

	            api.setAPI(APICaller.API_EVENT_DETAILS);
	            api.setLanguage(APICaller.LANG_ENGLISH);
	            api.setEventID("");

	            if (api.callAPI()) {
	                result = api.getJSONString();
	            } else {
	                result = api.getLastError();
	            }

	            Log.d("GW2Events", result + "");
	            
	            try
	            {
	                EventCacher dCH = new EventCacher(context);
	                
	                JSONObject eventsObject = new JSONObject(result);
	                eventsObject = eventsObject.getJSONObject("events");
	                Iterator<?> iterator = eventsObject.keys();
	                
	                while (iterator.hasNext()) 
	                {
	                	String key = iterator.next().toString();
	                	JSONObject eventObject = eventsObject.getJSONObject(key);
	                	String imagePath       = eventObject.getString("imagePath");
	                	String imageFileName   = eventObject.getString("imageFileName");
	                	
	                    dCH.cacheRemoteMedia(imagePath + imageFileName, EventCacher.CACHE_MEDIA_DIR, imageFileName);
	                    
	                    dCH.cacheEventsAPI(eventObject.toString(), EventCacher.CACHE_APIS_DIR, key);
	                }
	            }
	            catch (JSONException e)
	            {
	                Log.d("GW2Events", e.getMessage());
	            }

	            return result;
	        }
	}

    /**
     * This is the class for making our API call to retrieve the event contents.
     */
    public class EventSelectAPI extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            eventProgDialog = new ProgressDialog(activity);
            eventProgDialog.setMessage("Retrieving events...");
            eventProgDialog.setIndeterminate(false);
            eventProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            eventProgDialog.setCancelable(false);
            eventProgDialog.show();
        }

        @Override
        protected String doInBackground(Void...params)
        {
            String result = "";
            APICaller api = new APICaller();

            api.setAPI(APICaller.API_EVENT_NAMES);
            api.setLanguage(APICaller.LANG_ENGLISH);

            if (api.callAPI()) {
                result = api.getJSONString();
            } else {
                result = api.getLastError();
            }

            Log.d("GW2Events", result + "");

            return result;
        }

        @Override
        public void onPostExecute(String result)
        {
            try
            {
                JSONArray json;
                json = new JSONArray(result);
                //EventCacher dCH = new EventCacher(context);

                for(int i = 0; i < json.length(); i++){
                    JSONObject jsonObject = json.getJSONObject(i);

                    String name 		 = URLDecoder.decode(jsonObject.getString("short_name"));
                    String description   = URLDecoder.decode(jsonObject.getString("name"));
                    String eventID       = URLDecoder.decode(jsonObject.getString("id"));
                    int typeID           = jsonObject.getInt("event_class_id");

                    //Add to adapter at some point
                    eventAdapter.add(name, description, eventID, typeID);
                    
                    //dCH.cacheRemoteMedia(imagePath + imageFileName, EventCacher.EVENTS_CACHE_DIR, imageFileName);
                }
            }
            catch (JSONException e)
            {
                Log.d("GW2Events", e.getMessage());
            }

            //Reset adapter
            eventListView.setAdapter(null);
            eventListView.setAdapter(eventAdapter);

            eventProgDialog.dismiss();
        }
    }
}