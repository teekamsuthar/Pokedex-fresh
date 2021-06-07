package edu.com.demo.pokemon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // declare global vars
    private static final String TAG = "MAIN";
    private ListView list;
    private final ArrayList<Pokemon> pokemon = new ArrayList<>();
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init list
        list = findViewById(R.id.listView);
        requestQueue = Volley.newRequestQueue(this);
        // load pokemon's into list
        loadPokemon();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            // share app intent - you can add link to apk in Extra_Text
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share Pokedex.");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hey there, I'm sharing this cool app that let's you catch Pokemon's");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadPokemon() {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    // for each item in the results, add the details into the pokemon list
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String name = result.getString("name");
                        pokemon.add(new Pokemon(
                                name.substring(0, 1).toUpperCase() + name.substring(1),
                                result.getString("url")
                        ));
                    }
                    // load retrieved pokemon list in adapter
                    CustomAdaperView customAdaperView = new CustomAdaperView(MainActivity.this, pokemon);
                    list.setAdapter(customAdaperView);
                    // set on click listeners for each item in the list
                    list.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(android.widget.AdapterView<?> parent,
                                                View view, int position, long id) {

                            Intent intent = new Intent(MainActivity.this, PokemonActivity.class);
                            intent.putExtra("url", pokemon.get(position).getUrl());
                            startActivity(intent);
                            Log.d(TAG, "Clicked");
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Pokemon list error", error);
            }
        });
        requestQueue.add(request);
    }
}
