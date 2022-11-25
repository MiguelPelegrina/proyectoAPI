package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.io.HttpConnectPersonaje;
import com.example.proyecto.model.Personaje;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Preferences.loadPreferences(this, constraintLayout);

        // Obtenemos el Intent de la activity que inicio esta activity
        Intent i = getIntent();
        // Obtenemos el mensaje contenido dentro del Intent a través de la clave "INFO"
        String nombre = i.getStringExtra("name");

        new taskConnection().execute("GET", "characters?name="+nombre);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Preferences.loadPreferences(this, constraintLayout);
    }

    // Sobreescribimos el metodo onCreateOptionsMenu para crearnos un menu personalizada
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Usamos un inflater para construir la vista pasandole el menu por defecto como parámetro
        // para colocarlo en la vista
        getMenuInflater().inflate(R.menu.simple_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_preferencias:
                Intent i = new Intent(DetailActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class taskConnection extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            result = HttpConnectPersonaje.getRequest(strings[1]);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    // ELEGIR INFORMACIÓN QUE SE DESEA MOSTRAR --> todo?
                    String name = "";
                    String actor = "";
                    Uri img = null;
                    name = jsonObject.getString("name");
                    actor = jsonObject.getString("portrayed");
                    img = Uri.parse(jsonObject.getString("img"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}