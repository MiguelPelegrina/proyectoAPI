package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.io.APIConnectionBreakingBad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

public class DetailActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;
    private ImageView imgPersonajeGrande;
    private EditText txtNombrePersonaje;
    private EditText txtActorPersonaje;
    private EditText txtFechaNacimiento;
    private EditText txtEstadoPersonaje;
    private Button btnGuardar;
    private CircularProgressDrawable progressDrawable;
    private String name;
    private String actor;
    private String fecha;
    private String estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);
        imgPersonajeGrande = (ImageView) findViewById(R.id.imagenGrande);
        txtNombrePersonaje = (EditText) findViewById(R.id.editTextPersonNameDetalle);
        txtActorPersonaje = (EditText) findViewById(R.id.editTextPersonajeActorDetalle);
        txtFechaNacimiento = findViewById(R.id.editTextPersonajeDetalleNacimiento);
        txtEstadoPersonaje = findViewById(R.id.editTextPersonajeDetalleEstado);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Preferences.loadPreferences(this, constraintLayout);

        progressDrawable = new CircularProgressDrawable(this);
        progressDrawable.setStrokeWidth(15f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(45f);
        progressDrawable.start();

        // Obtenemos el Intent de la activity que inicio esta activity
        Intent intent = getIntent();

        String accion = intent.getStringExtra("info");
        // Obtenemos el mensaje contenido dentro del Intent a través de la clave "info"
        String nombre = intent.getStringExtra("name");

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(accion.equals("mod")){
                    if(!name.equals(txtNombrePersonaje.getText().toString()) ||
                            !actor.equals(txtActorPersonaje.getText().toString()) ||
                            !fecha.equals(txtFechaNacimiento.getText().toString()) ||
                            !estado.equals(txtEstadoPersonaje.getText().toString())){
                        createAlertDialog("Modificar", "¿De verdad quiere modificar los datos del personaje?").show();
                    }
                }else{
                    if(accion.equals("add")){
                        volver();
                    }
                }
            }
        });

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

    private class taskConnection extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = null;

            result = APIConnectionBreakingBad.getRequest(strings[1]);

            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    // ELEGIR INFORMACIÓN QUE SE DESEA MOSTRAR --> todo?
                    name = jsonObject.getString("name");
                    txtNombrePersonaje.setText(name);
                    actor = jsonObject.getString("portrayed");
                    txtActorPersonaje.setText(actor);
                    fecha = jsonObject.getString("birthday");
                    txtFechaNacimiento.setText(fecha);
                    estado = jsonObject.getString("status");
                    txtEstadoPersonaje.setText(estado);
                    Glide.with(DetailActivity.this)
                            .load(jsonObject.getString("img"))
                            .placeholder(progressDrawable)
                            .error(R.mipmap.ic_launcher)
                            .into(imgPersonajeGrande);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AlertDialog createAlertDialog(String titulo, String mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);

        builder.setMessage(mensaje).setTitle(titulo);

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toasty.info(DetailActivity.this, "Modificación cancelada");
            }
        });

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                volver();
            }
        });

        return builder.create();
    }

    private void volver(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
        returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
        setResult(DetailActivity.RESULT_OK, returnIntent);
                /*if(accion.equals("add")){
                    returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
                    returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
                    setResult(DetailActivity.RESULT_OK, returnIntent);
                }else{
                    if(accion.equals("mod")){
                        returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
                        returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
                        setResult(DetailActivity.RESULT_OK, returnIntent);
                    }
                }*/
        finish();
    }
}