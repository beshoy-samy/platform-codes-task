package com.example.bisho.interviewtask.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bisho.interviewtask.R;
import com.example.bisho.interviewtask.adapters.FlickrImagesRecyclerAdapter;
import com.example.bisho.interviewtask.classes.Photo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, RequestQueue.RequestFinishedListener<Object> {

    private SwipeRefreshLayout images_swipe_refresh;
    private RecyclerView flickrImages_recyclerView;
    private Context context;

    //volley object to request json
    private JsonObjectRequest jsObjRequest;
    //volley queue to add requests
    private RequestQueue requestQueue;

    //ArrayList to hold the photos
    private ArrayList<Photo> flickrImages;
    private FlickrImagesRecyclerAdapter flickrAdapter;
    private boolean requestInProgress;
    private boolean rotated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        images_swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.images_swipe_refresh);
        images_swipe_refresh.setColorSchemeColors(ContextCompat.getColor(context, R.color.colorPrimary)
                ,ContextCompat.getColor(context, R.color.colorPrimaryDark)
                ,ContextCompat.getColor(context, R.color.colorAccent));
        images_swipe_refresh.setOnRefreshListener(this);


        flickrImages_recyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);

        flickrImages = new ArrayList<>();
        flickrAdapter = new FlickrImagesRecyclerAdapter(context,flickrImages);

        requestQueue = Volley.newRequestQueue(context); // creating a request queue

        if(savedInstanceState != null) {
            // getting the saved booleans to maintain the app state
            requestInProgress = savedInstanceState.getBoolean("requestInProgress");
            rotated           = savedInstanceState.getBoolean("rotated");
        }
        
        if(networkAvailable()) {
            if(rotated) { //if the device has been rotated
                if (!requestInProgress) // check if there is a request in progress
                    showImagesInOfflineMode();// if not will show the cached images
                //if yes and there is a request in progress will not create a new one
                //and the flow of the app should be normal but it is not working don't know why
                //although after debugging the data is downloaded and parsed but recycler view is not
                //showing them
            }
            else create_new_request();
        }else{ // if network is not available will display the cached images
            showImagesInOfflineMode();
        }
        rotated = false;
        Log.d("beshoy","onCreate");
    }

    private void showImagesInOfflineMode() {
        //getting images from sugar database
        flickrImages = (ArrayList<Photo>) Photo.listAll(Photo.class);

        flickrAdapter.updateFlickrImages(flickrImages);
        flickrImages_recyclerView.setAdapter(flickrAdapter);
        flickrImages_recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));

        if(!networkAvailable())
            Toast.makeText(context,getResources().getString(R.string.no_internet_error_message),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRefresh() {
        if(networkAvailable())
            create_new_request();
        else {
            // no internet available display error message
            Toast.makeText(context, getResources().getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
            images_swipe_refresh.setRefreshing(false); // ending the refresh progress circle
        }
    }

    public boolean networkAvailable () {
        ConnectivityManager networkManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkState = networkManager.getActiveNetworkInfo();

        boolean available = (networkState != null && networkState.isConnected());

        return available;
    }

    public void create_new_request(){
        Log.d("beshoy","new request");
        final String flickr_URL = "https://api.flickr.com/" +
                "services/rest/?method=flickr.photos.search" +
                "&api_key=cca5c934cb35f3b62ad20ff75b5c3af0" +
                "&format=json&nojsoncallback=1" +
                "&extras=url_l" +
                "&safe_search=for%20safe&per_page=20" +
                "&tags=bird";

        if (jsObjRequest != null)
            jsObjRequest = null;
        //  creating new jsonRequest
        jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, flickr_URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("beshoy","on response");
                        parseJsonObject(response);//passing the Json object to be parsed
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(context,"Error:"+error.getMessage(),Toast.LENGTH_LONG).show();
                        images_swipe_refresh.setRefreshing(false);
                    }
                });
        requestInProgress = true;
        //creating a request queue
        requestQueue.add(jsObjRequest);
        requestQueue.addRequestFinishedListener(this);
    }

    private void parseJsonObject(JSONObject flickJsonObject) {
        Log.d("beshoy","parsing");
        final String photos_JsonObject = "photos";
        final String photos_JsonArray  = "photo";
        final String image_title       = "title";
        final String image_URL         = "url_l";

        try {
            flickrImages.clear();   // clearing the images before getting the new images
            Photo.deleteAll(Photo.class);// clearing the images before saving the new images

            //the main flickr Photos Json object
            JSONObject photos = flickJsonObject.getJSONObject(photos_JsonObject);
            //getting flickr Images array
            JSONArray images = photos.getJSONArray(photos_JsonArray);
            //looping on the array to fetch every image
            for (int i=0;i<images.length();i++){
                JSONObject photo = images.getJSONObject(i);
                String photoURL = photo.getString(image_URL);
                String photoTitle = photo.getString(image_title);
                Photo flickrPhoto = new Photo(photoURL,photoTitle);
                flickrPhoto.save(); //caching the downloaded image data
                flickrImages.add(flickrPhoto);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!flickrImages.isEmpty()){
            flickrAdapter.updateFlickrImages(flickrImages); // updating adapter's data
            flickrImages_recyclerView.setAdapter(flickrAdapter);
            flickrImages_recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
            Log.d("beshoy","arrayList size: "+flickrImages.size());
        }

    }

    @Override
    public void onRequestFinished(Request<Object> request) {
        Log.d("beshoy","request finished");
        images_swipe_refresh.setRefreshing(false); // ending the refresh progress circle
        requestInProgress = false; // request has finished
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        rotated = true; // the app is rotated
        // putting booleans
        outState.putBoolean("requestInProgress",requestInProgress);
        outState.putBoolean("rotated",rotated);
        Log.d("beshoy","onSave");
    }
}
