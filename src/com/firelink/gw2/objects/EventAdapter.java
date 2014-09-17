package com.firelink.gw2.objects;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firelink.gw2.events.R;

public class EventAdapter extends BaseAdapter
{

    static class ViewHolder
    {
        TextView eventNameTV;
        TextView eventDescTV;
        ImageView eventTypeIV;
        RelativeLayout eventExtraInfoRL;
    }

    private Context context;
    private ArrayList<EventHolder> eventData;

    /**
     * 
     * @param context
     */
    public EventAdapter(Context context)
    {
        super();

        this.context   = context;
        this.eventData = new ArrayList<EventHolder>();
    }

    /**
     * 
     * @param context
     * @param eventNames
     * @param eventTypes
     */
    public EventAdapter(Context context, String[] eventNames, String[] eventTypes, int[] eventTypeIDs)
    {
        super();

        this.context   = context;
        this.eventData = new ArrayList<EventHolder>();

        for (int i = 0; i < eventNames.length; i++)
        {
            EventHolder events = new EventHolder();
            events.name   = eventNames[i];
            events.type   = eventTypes[i];
            events.typeID = eventTypeIDs[i];

            this.eventData.add(events);
        }
    }

    /**
     * 
     * @param name
     * @param type
     * @param typeID
     */
    public void add(String name, String description, String eventID, int typeID)
    {
        EventHolder events = new EventHolder();
        events.name        = name;
        events.description = description;
        events.typeID      = typeID;
        events.eventID     = eventID;

        this.eventData.add(events);

        this.notifyDataSetChanged();
    }

    /**
     * @param position
     * @param view
     * @param viewGroup
     */
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        ViewHolder holder;

        if (null == convertView)
        {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);

            convertView = vi.inflate(R.layout.event_adapter, viewGroup, false);
            holder = new ViewHolder();
            holder.eventNameTV      = (TextView)convertView.findViewById(R.id.eventAdapterTitleTextView);
            holder.eventDescTV      = (TextView)convertView.findViewById(R.id.eventAdapterDescriptionTextView);
            holder.eventTypeIV      = (ImageView)convertView.findViewById(R.id.eventAdapterLeftImageView);
            holder.eventExtraInfoRL = (RelativeLayout)convertView.findViewById(R.id.eventAdapterExtraInfoLayout);
            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        EventHolder tempEvent = getItem(position);
        holder.eventNameTV.setText(tempEvent.name);
        holder.eventDescTV.setText(tempEvent.description);
        

        //Determine which color to add to the eventClass left bar thing
        int eventResource;
        int eventBGResource;
        int eventBGPressed;
        switch(tempEvent.typeID)
        {
            case 1:
                eventResource   = R.drawable.event_level_high;
                eventBGResource = R.color.gw_event_level_high;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_high;
                break;
            case 2:
                eventResource   = R.drawable.event_level_standard;
                eventBGResource = R.color.gw_event_level_standard;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_standard;
                break;
            case 3:
                eventResource   = R.drawable.event_level_low;
                eventBGResource = R.color.gw_event_level_low;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_low;
                break;
            default:
                eventResource   = R.drawable.event_level_standard;
                eventBGResource = R.color.gw_event_level_standard;
                eventBGPressed  = R.drawable.event_selector_bg_pressed_high;
                break;
        }

        holder.eventTypeIV.setImageResource(eventResource);
        
        if (tempEvent.isActive)
        {
        	holder.eventExtraInfoRL.setVisibility(View.VISIBLE);
            holder.eventNameTV.setBackgroundResource(eventBGResource);
            holder.eventNameTV.setTextColor(context.getResources().getColor(R.color.white));
            holder.eventNameTV.setActivated(true);
            
            //Get image
            if (tempEvent.image == null) {
            	//File tempFile = new File(this.dCH.getMediaCachePath() + EventCacher.EVENTS_CACHE_DIR, tempEvent.imagePath);
            	//tempEvent.image = new BitmapDrawable(BitmapFactory.decodeFile(tempFile.getAbsolutePath()));
            }
        }
        else
        {
        	holder.eventExtraInfoRL.setVisibility(View.GONE);
            //holder.eventNameTV.setBackgroundResource(eventBGPressed);
        	holder.eventNameTV.setBackgroundResource(eventBGPressed);
            holder.eventNameTV.setTextColor(context.getResources().getColor(R.color.black));
            holder.eventNameTV.setActivated(false);
        }
        

        return (convertView);
    }

    /**
     * 
     */
    @Override
    public int getCount()
    {
        return this.eventData.size();
    }

    /**
     * 
     */
    @Override
    public EventHolder getItem(int position)
    {
        return eventData.get(position);
    }

    /**
     * 
     */
    @Override
    public long getItemId(int position)
    {
        return 0;

    }

    public void setItem(int position, EventHolder event)
    {
        eventData.set(position, event);
    }
}


