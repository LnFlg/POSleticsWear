package com.example.posleticswear;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class NetworkSingleton {

    private static NetworkSingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;



    private String urlPos = "https://posletics.herokuapp.com/api/pos";
    private String urlGetRoute = "https://posletics.herokuapp.com/api/route?user_id=";
    private String urlGetUsers = "https://posletics.herokuapp.com/api/users";

    private String urlPosUpvote = "https://posletics.herokuapp.com/api/pos/upvote/";
    private String urlPosDownvote = "https://posletics.herokuapp.com/api/pos/downvote/";

    private NetworkSingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized NetworkSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkSingleton(context);
            instance.getAllPos();
            instance.getUsersFromServer();
        }


        return instance;
    }

    //JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
          //  aUrl, null, new Response.Listener<JSONObject>(){

    public void getUsersFromServer(){
        JsonArrayRequest jsnArrq = new JsonArrayRequest(
                Request.Method.GET,
                urlGetUsers,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<Integer> users = new ArrayList<>();
                        for (int i =0; i<response.length();i++) {
                            try {
                                users.add(Integer.valueOf(response.getJSONObject(i).getInt("id")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Collections.sort(users);
                        Log.i("1", "Users:" +users.size());
                        RuntimeData.getInstance().setUsers(users);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: Users :" + error.getMessage());
                        //hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsnArrq);
    }


    public void getRouteFromServer(final int userId) {

        StringRequest stringrq = new StringRequest(
                urlGetRoute.concat(String.valueOf(userId)),
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        if (!response.isEmpty()) {
                            String[] splited = response.substring(1,response.length()-1).split(",");
                            for(int i = 0; i < splited.length; i++) {
                                RuntimeData.getInstance().addToRoute(Integer.parseInt(splited[i]));
                            }
                            Log.i("2", "Got Route of length: " + splited.length);
                        }

                        if(RuntimeData.getInstance().getRoute()==null){
                            RuntimeData.getInstance().setDiscoveryRadius(750d);
                        }else{RuntimeData.getInstance().setDiscoveryRadius(500d);}
                    }}, new Response.ErrorListener() {
                      @Override
                      public void onErrorResponse(VolleyError error) {
                          VolleyLog.d(TAG, "Error: Route: " + error.getMessage());
                          // hideProgressDialog();
                       }
                    });

        getRequestQueue().add(stringrq);


    }


    public void sendPOS(double lat, double lng, int userId) {
        //showProgressDialog();

        Map<String, String> postParam = new HashMap<String, String>();
        postParam.put("lat", String.valueOf(lat));
        postParam.put("lng", String.valueOf(lng));
        postParam.put("user_id", String.valueOf(userId));


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                urlPos,
                new JSONObject(postParam),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //hideProgressDialog();
                        Log.i("2","POS sent to server.");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: SendPos: " + error.getMessage());
                        //hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsonObjReq);
    }




    public void getAllPos(){
        JsonArrayRequest jsnArrq = new JsonArrayRequest(
                Request.Method.GET,
                urlPos,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Map<Integer, Pos> posMap = new HashMap<>();
                        for (int i =0; i<response.length();i++) {
                            try {
                                double lat = response.getJSONObject(i).getDouble("lat");
                                double lng = response.getJSONObject(i).getDouble("lng");
                                int id = response.getJSONObject(i).getInt("id");
                                int upvote = response.getJSONObject(i).getInt("upvotes");
                                Pos toAdd = new Pos(lat,lng,id,upvote);

                                //2 höchsten Hashtags finden und speichern
                                JSONArray hashtags = response.getJSONObject(i).getJSONArray("hashtags");
                                int first=0, second=0;
                                String firstHashtag=null, secondHashtag=null;
                                for(int j =0; j<hashtags.length();++j){
                                    if(hashtags.getJSONObject(j).getInt("upvotes")>= first){
                                        firstHashtag = hashtags.getJSONObject(j).getString("name");
                                        first= hashtags.getJSONObject(j).getInt("upvotes");
                                    }else if (hashtags.getJSONObject(j).getInt("upvotes")>= second){
                                        secondHashtag = hashtags.getJSONObject(j).getString("name");
                                        second= hashtags.getJSONObject(j).getInt("upvotes");
                                    }
                                }

                                ArrayList hashtagNames = new ArrayList<String>();
                                hashtagNames.add(firstHashtag);
                                hashtagNames.add(secondHashtag);
                                toAdd.setHighestHashtags(hashtagNames);


                                posMap.put(id,toAdd);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        RuntimeData.getInstance().setAllPos(posMap);
                        Log.i("1","Amnt Pos fetched:"+posMap.size());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: GetAllPos:" + error.getMessage());
                        //hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsnArrq);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    public void upvotePos(int id){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                urlPosUpvote.concat(String.valueOf(id)),
                new JSONObject(),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //hideProgressDialog();
                        Log.i("2","Upvoted pos");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: UpvotePos: " + error.getMessage());
                        //hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsonObjReq);
    }
    public void downvotePos(int id){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                urlPosDownvote.concat(String.valueOf(id)),
                new JSONObject(),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //hideProgressDialog();
                        Log.i("2","Downvoted pos");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: downvotePos:" + error.getMessage());
                        //hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsonObjReq);

    }


}
