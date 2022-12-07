package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.example.proyecto.R;
import com.example.proyecto.Utilities.Preferences;
import com.example.proyecto.io.APIConnection;
import com.example.proyecto.model.Personaje;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class DetailActivity extends AppCompatActivity {
    // Declaracion de variables
    private ConstraintLayout constraintLayout;
    private ImageView imgPersonajeGrande;
    private EditText txtNombrePersonaje;
    private EditText txtActorPersonaje;
    private EditText txtFechaNacimiento;
    private Spinner sbEstadoPersonaje;
    private Button btnGuardar;
    private CircularProgressDrawable progressDrawable;
    private String accion;
    private boolean imagenNueva = false;
    private Uri uri;
    private String name;
    private String actor;
    private String fecha;
    private String estado;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Asociamos los elementos del layout con el código
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);
        imgPersonajeGrande = (ImageView) findViewById(R.id.imagenGrande);
        txtNombrePersonaje = (EditText) findViewById(R.id.editTextPersonNameDetalle);
        txtActorPersonaje = (EditText) findViewById(R.id.editTextPersonajeActorDetalle);
        txtFechaNacimiento = (EditText) findViewById(R.id.editTextPersonajeDetalleNacimiento);
        sbEstadoPersonaje = (Spinner) findViewById(R.id.spEstado);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        //
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.estados, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sbEstadoPersonaje.setAdapter(adapter);

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
        intent = getIntent();
        accion = intent.getStringExtra("info");
        // Obtenemos el mensaje contenido dentro del Intent a través de la clave "info"
        name = intent.getStringExtra("name");
        actor = intent.getStringExtra("actor");
        uri = Uri.parse(intent.getStringExtra("uri"));
        fecha = intent.getStringExtra("birthday");
        estado = intent.getStringExtra("status");

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                update();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                update();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        };
        txtNombrePersonaje.addTextChangedListener(textWatcher);
        txtActorPersonaje.addTextChangedListener(textWatcher);
        txtFechaNacimiento.addTextChangedListener(textWatcher);
        sbEstadoPersonaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                update();
            }
        });
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(accion){
                    case "mod":
                        if(comprobarCamposDiferentes()){
                            if(comprobarCampoFecha()){
                                createAlertDialog("Modificar", "¿De verdad quiere modificar los datos del personaje?").show();
                            }
                        }
                        break;
                    case "add":
                        if(comprobarCampoFecha()){
                            volver();
                        }
                        break;
                }
            }
        });

        // Esta parte del código corresponde a la biblioteca FilePicker. Esta nos permite elegir
        // ficheros. En este caso en concreto nos permite añadir o modificar la imagen del personaje
        // Nos creamos un objeto de la clase DialogProperties
        DialogProperties properties = new DialogProperties();
        // Configuramos las variables de dicho objeto
        // El modo de selección será de un único fichero
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        // Solo se podrán elegir ficheros
        properties.selection_type = DialogConfigs.FILE_SELECT;
        // Obtenemos el directorio de la sdExterna que guarda los datos del usuario
        File sdExterna = new File(Environment.getExternalStorageDirectory().getPath());
        // Establecemos como directorios la ruta de la sdExterna
        properties.root = sdExterna;
        properties.error_dir = sdExterna;
        properties.offset = sdExterna;
        // Establecemos las extensiones permitidas
        properties.extensions = new String[]{"jpg","jpeg","png"};
        // Nos creamos un objeto de la ventana de dialogo
        FilePickerDialog dialog = new FilePickerDialog(DetailActivity.this, properties);
        // Modificamos su título
        dialog.setTitle("Eliga una imagen");
        // Asignamos un oyente al dialogo
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                // Cuando se elige un fichero obtenemos su uri local
                // NO HE PODIDO COMPROBAR AÚN LO SUFICIENTE LA POSIBILIDAD DE EXCEPCIONES
                //try {
                    uri = Uri.fromFile(new File(files[0]));
                    // Asignamos la uri al imageView
                    imgPersonajeGrande.setImageURI(uri);
                    // Controlamos las posibles excepciones
                /*}catch(Exception e){
                    Toasty.error(DetailActivity.this,"Solo eliga ficheros con la " +
                            "extensión jpg o png").show();
                }*/

                // Modificamos nuestra variable booleana que registra cambios en las imagenes
                imagenNueva = true;
                // Comprobamos si se han modificado los datos para habilitar el boton de guardar al
                // cambiar la imagen
                update();
            }
        });
        // Le asignamos al imageView que mostrará el dialog configurado previamente cuando se realice
        // un onLongClick
        imgPersonajeGrande.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                dialog.show();

                return false;
            }
        });

        if(Preferences.notificationPreference(this)) {
            Toasty.info(DetailActivity.this, "Para poder guardar los cambios los campos" +
                    " no deben estar vacios", Toasty.LENGTH_LONG, true).show();
            Toasty.info(DetailActivity.this, "Puede modificar la imagen manteniendo " +
                    " el dedo pulsado sobre ella", Toasty.LENGTH_LONG, true).show();
        }

        switch (accion){
            case "mod":
                txtNombrePersonaje.setText(name);
                txtActorPersonaje.setText(actor);
                imgPersonajeGrande.setImageURI(uri);
                Glide.with(DetailActivity.this)
                        .load(uri)
                        .placeholder(progressDrawable)
                        .error(R.drawable.image_not_found)
                        .into(imgPersonajeGrande);
                txtFechaNacimiento.setText(fecha);
                switch (estado){
                    case "Alive":
                    case "true":
                        sbEstadoPersonaje.setSelection(0);
                        break;
                    case "Presumed dead":
                        sbEstadoPersonaje.setSelection(1);
                        break;
                    case "Deceased":
                    case "false":
                        sbEstadoPersonaje.setSelection(2);
                        break;
                }
                break;
            case "add":
                imgPersonajeGrande.setImageResource(R.drawable.image_not_found);
                break;
        }
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
        getMenuInflater().inflate(R.menu.simple, menu);

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

    //Métodos auxiliares
    private boolean comprobarCamposVacios() {
        boolean vacios = true;

        if(!txtNombrePersonaje.getText().toString().trim().equals("") &&
                !txtActorPersonaje.getText().toString().trim().equals("") &&
                !txtFechaNacimiento.getText().toString().trim().equals("")){
            vacios = false;
        }
        return vacios;
    }

    private boolean comprobarCamposDiferentes(){
        boolean diferentes = true;
        if(accion.equals("mod")){
            if(name.equals(txtNombrePersonaje.getText().toString().trim()) &&
                    actor.equals(txtActorPersonaje.getText().toString().trim()) &&
                    fecha.equals(txtFechaNacimiento.getText().toString().trim()) &&
                    estado.equals(sbEstadoPersonaje.getSelectedItem().toString().trim()) &&
                    !imagenNueva){
                diferentes = false;
            }
        }

        return diferentes;
    }

    @NonNull
    private String comprobarFecha(String stringFecha) throws ParseException {
        Date fecha = null;
        //SimpleDateFormat formato = new SimpleDateFormat("MM-dd-yyyy");
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");

        fecha = formato.parse(stringFecha);

        stringFecha = formato.format(fecha);

        return stringFecha;
    }

    private boolean comprobarCampoFecha(){
        boolean valid = false;

        if(txtFechaNacimiento.getText().toString().trim().equalsIgnoreCase("Unknown")){
            valid = true;
        }else{
            try {
                comprobarFecha(txtFechaNacimiento.getText().toString().trim());
                valid = true;
            } catch (ParseException e) {
                Toasty.error(DetailActivity.this,"Introducza una fecha válida según el " +
                        "formato MM-dd-yyyy").show();
            }
        }

        return valid;
    }

    @NonNull
    private AlertDialog createAlertDialog(String titulo, String mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);

        builder.setMessage(mensaje).setTitle(titulo);

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toasty.error(DetailActivity.this, "Modificación cancelada",
                        Toasty.LENGTH_SHORT,true).show();
            }
        });

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toasty.success(DetailActivity.this, "Modificación realizada",
                        Toasty.LENGTH_SHORT, true).show();
                volver();
            }
        });

        return builder.create();
    }

    private void volver(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
        returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
        returnIntent.putExtra("uri", uri.toString());
        returnIntent.putExtra("birthday", txtFechaNacimiento.getText() + "");
        returnIntent.putExtra("status",sbEstadoPersonaje.getSelectedItem().toString());

        setResult(DetailActivity.RESULT_OK, returnIntent);

        finish();
    }

    private void update(){
        if(!comprobarCamposVacios()){
            if(comprobarCamposDiferentes()){
                btnGuardar.setEnabled(true);
            }else{
                btnGuardar.setEnabled(false);
            }
        }else{
            btnGuardar.setEnabled(false);
        }
    }
}