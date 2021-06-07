package edu.com.demo.pokemon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private static final String TAG = "DETAILS";
    // declare global vars
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private boolean isCaught;
    private Button catchPokemon;
    private ImageView sprite;
    private TextView description;
    private int pokemonId;
    private String descriptionUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        // set views, init vars
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        catchPokemon = findViewById(R.id.catch_pokemon);
        sprite = findViewById(R.id.imageView);
        description = findViewById(R.id.description);
        load();
        checkCatch();
    }

    public void checkCatch() {
        // check if pokemon is already caught or not.
        isCaught = getPreferences(Context.MODE_PRIVATE).getBoolean(url, false);
        if (isCaught) {
            catchPokemon.setText(getString(R.string.release));
        } else {
            catchPokemon.setText(getString(R.string.catch_pok));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (IOException e) {
                Log.e(TAG, "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // load the bitmap into the ImageView!
            sprite.setMinimumHeight(450);
            sprite.setImageBitmap(bitmap);
        }
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name").toUpperCase());
                    pokemonId = response.getInt("id");
                    numberTextView.setText(String.format("#%03d", pokemonId));
                    // get description URL using above pokemon id value to use in description function
                    descriptionUrl = "https://pokeapi.co/api/v2/pokemon-species/" + pokemonId + "/";
                    // LOAD description url when I get the url
                    loadDescription();
                    JSONArray typeEntries = response.getJSONArray("types");
                    // get sprite object from response
                    JSONObject sprites = response.getJSONObject("sprites");
                    // extract the url from object
                    String spriteUrl = sprites.getString("front_default");
                    // download sprite and apply it to image view
                    new DownloadSpriteTask().execute(spriteUrl);

                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");
                        if (slot == 1) {
                            type1TextView.setText(type);
                        } else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Pokemon details error", error);
            }
        });
        requestQueue.add(request);
    }

    public void loadDescription() {
        description.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, descriptionUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // get json array of flavor text entries
                    JSONArray flavorEntries = response.getJSONArray("flavor_text_entries");
                    //   iterate over the array to get correct language description
                    for (int i = 0; i < flavorEntries.length(); i++) {
                        // get flavor text for each index
                        String text = flavorEntries.getJSONObject(i).getString("flavor_text");
                        JSONObject language = flavorEntries.getJSONObject(i).getJSONObject("language");
                        String languageString = language.getString("name");
                        // make sure its lang is english
                        if (languageString.equals("en")) {
                            //   apply the description text to view
                            description.setText(text);
                            // break out of the loop, the moment we get our first match of language
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Description json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Pokemon description error", error);
            }
        });
        requestQueue.add(request);
    }

    public void toggleCatch(View view) {
        isCaught = !isCaught;
        if (isCaught) {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(url, true).apply();
            catchPokemon.setText(getString(R.string.release));
        } else {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(url, false).apply();
            catchPokemon.setText(getString(R.string.catch_pok));
        }
    }
}
