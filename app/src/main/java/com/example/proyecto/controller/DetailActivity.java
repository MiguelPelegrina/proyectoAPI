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
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextMenu;
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
import com.example.proyecto.uilities.Preferences;

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
    private FilePickerDialog dialog;
    private String enlaceWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Asociamos los elementos del layout con el c??digo
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_detail_constraint);
        imgPersonajeGrande = (ImageView) findViewById(R.id.imagenGrande);
        txtNombrePersonaje = (EditText) findViewById(R.id.editTextPersonNameDetalle);
        txtActorPersonaje = (EditText) findViewById(R.id.editTextPersonajeActorDetalle);
        txtFechaNacimiento = (EditText) findViewById(R.id.editTextPersonajeDetalleNacimiento);
        sbEstadoPersonaje = (Spinner) findViewById(R.id.spEstado);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        // Registramos la imagen como men?? de contexto
        // De tal forma tendremos la opci??n de modificar o a??adir una imagen a partir de un fichero
        // almacenado previamente o un imagen elegida a trav??s de una URI
        registerForContextMenu(imgPersonajeGrande);

        // Rellenamos el spinner que guarda los posible estados de los personajes
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.estados, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sbEstadoPersonaje.setAdapter(adapter);

        // Activamos el icono de "Volver"(flecha atr??s)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Cargamos las preferencias
        Preferences.loadPreferences(this, constraintLayout);

        // Configuramos el CircularProgressDrawable que indicar?? que se est?? cargando una imagen
        progressDrawable = new CircularProgressDrawable(this);
        progressDrawable.setStrokeWidth(15f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(45f);
        progressDrawable.start();

        // Obtenemos el Intent de la activity que inicio esta activity
        intent = getIntent();
        // Obtenemos toda la informaci??n pasada a trav??s del intent
        accion = intent.getStringExtra("info");
        name = intent.getStringExtra("name");
        actor = intent.getStringExtra("actor");
        uri = Uri.parse(intent.getStringExtra("uri"));
        fecha = intent.getStringExtra("birthday");
        estado = intent.getStringExtra("status");
        // Esta parte es necesaria para adaptar el contenido del spinner a ambas APIs ya que una lo
        // guarda como variable boleana y la otro como "enumerado" de tres opciones
        if(accion.equals("mod")){
            if(estado.equals("true")){
                estado = "Alive";
            }else{
                if(estado.equals("false")){
                    estado = "Dead";
                }
            }
        }

        // El textWatcher est?? implementado como habilitador del bot??n de guardar:
        // La idea es que mientras que el bot??n solo se habilite cuando
        // 1. Los campos NO est??n vacios
        // 2. La informaci??n se haya modificado de alguna manera en alguno de los campos, incluyendo
        // la imagen
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
        // Asignamos el oyente configurado a todos los componentes de texto
        txtNombrePersonaje.addTextChangedListener(textWatcher);
        txtActorPersonaje.addTextChangedListener(textWatcher);
        txtFechaNacimiento.addTextChangedListener(textWatcher);
        // Asignamos un oyente al spinner de tal forma que se comprueba si se han cambiado los datos
        // al elegir un elemento del spinner
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

        // Asignamos al bot??n de guardar un oyente
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos que la fecha sea v??lida
                if(comprobarCampoFecha()){
                // En funci??n de la acci??n que se quiere realizar
                    switch(accion) {
                        // Comprobamos que el usuario de verdad quiere modificar los datos de un
                        // personaje ya que esto supone una p??rdida de informaci??n
                        case "mod":
                            createAlertDialog().show();
                            break;
                        // Si a??ade el personaje creado y volvemos a la actividad anterior
                        case "add":
                            volver();
                            break;
                    }
                }
            }
        });

        // Esta parte del c??digo corresponde a la biblioteca FilePicker. Esta nos permite elegir
        // ficheros. En este caso en concreto nos permite a??adir o modificar la imagen del personaje
        // Nos creamos un objeto de la clase DialogProperties
        DialogProperties properties = new DialogProperties();
        // Configuramos las variables de dicho objeto
        // El modo de selecci??n ser?? de un ??nico fichero
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        // Solo se podr??n elegir ficheros
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
        dialog = new FilePickerDialog(DetailActivity.this, properties);
        // Modificamos su t??tulo
        dialog.setTitle("Eliga una imagen");
        // Asignamos un oyente al dialogo
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                // Cuando se elige un fichero obtenemos su uri local
                // NO HE PODIDO COMPROBAR A??N LO SUFICIENTE LA POSIBILIDAD DE EXCEPCIONES
                //try {
                    uri = Uri.fromFile(new File(files[0]));
                    // Asignamos la uri al imageView
                    imgPersonajeGrande.setImageURI(uri);
                    // Controlamos las posibles excepciones
                /*}catch(Exception e){
                    Toasty.error(DetailActivity.this,"Solo eliga ficheros con la " +
                            "extensi??n jpg o png").show();
                }*/
                // Modificamos nuestra variable booleana que registra cambios en las imagenes
                imagenNueva = true;
                // Comprobamos si se han modificado los datos para habilitar el boton de guardar al
                // cambiar la imagen
                update();
            }
        });

        // En funci??n de las preferencias se mostrar??n notificaciones informativas
        if(Preferences.notificationPreference(this)) {
            Toasty.info(DetailActivity.this, "Para poder guardar los cambios los campos" +
                    " no deben estar vacios", Toasty.LENGTH_LONG, true).show();
            Toasty.info(DetailActivity.this, "Puede modificar la imagen manteniendo " +
                    " el dedo pulsado sobre ella", Toasty.LENGTH_LONG, true).show();
        }

        // En funci??n del tipo de acci??n que se quiere realizar (a??adir un personaje o modificar
        // los datos de un personaje ya existente) rellenamos los campos con la informaci??n del
        // intent que llama a esta actividad o dejamos los campos vacios
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
                // En funci??n de los datos recibidos de la API se elige un estado u otro. En este
                // caso ambas APIs trabajan con valores diferentes: una utiliza un booleano y la
                // otra un "enumerado" con tres valores (Dead, Alive, Presumed Dead). Esto nos
                // obliga a unir diferentes casos de la siguiente forma
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
                // Colocamos una imagen por defecto
                imgPersonajeGrande.setImageResource(R.drawable.image_not_found);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Nos creamos el contextMenu y escribimos el t??tulo
        getMenuInflater().inflate(R.menu.flotante, menu);
        menu.setHeaderTitle("Elecci??n de imagen");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // En funci??n del elemento elegido del men?? de contexto
        switch (item.getItemId()){
            case R.id.elegirFichero:
                // Permitimos buscar una imagen en el propio dispositivo
                dialog.show();
                break;
            case R.id.elegirUri:
                // Mostramos un inputDialog que permita introducir la URI de una imagen
                createInputDialog().show();
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Volvemos a cargar las preferencias al volver a esta actividad
        Preferences.loadPreferences(this, constraintLayout);
    }

    // Sobreescribimos el metodo onCreateOptionsMenu para crearnos un menu personalizada
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Usamos un inflater para construir la vista pasandole el menu por defecto como par??metro
        // para colocarlo en la vista
        getMenuInflater().inflate(R.menu.simple, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // En funci??n del elemento elegido del men??
        switch (item.getItemId()){
            case R.id.item_preferencias:
                // Nos vamos a las preferencias
                Intent i = new Intent(DetailActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                // O nos vamos de vuelta a la ventana anterior
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //M??todos auxiliares
    /**
     * M??todo que comprueba que los campos de texto no est??n vacios
     * @return Devuelve verdadero si los campos est??n vacios y falso si lo campos no est??n vacios
     */
    private boolean comprobarCamposVacios() {
        boolean vacios = true;
        // Comprobamos que los campos est??n vacios, independientemente de espacios en blanco
        if(!txtNombrePersonaje.getText().toString().trim().equals("") &&
                !txtActorPersonaje.getText().toString().trim().equals("") &&
                !txtFechaNacimiento.getText().toString().trim().equals("")){
            vacios = false;
        }
        return vacios;
    }

    /**
     * M??todo que comprueba que los campos de texto sean diferentes
     * @return Devuelve falso si los campos son diferentes y verdadero si lo campos son iguales
     */
    private boolean comprobarCamposDiferentes(){
        boolean diferentes = true;
        // Si se pretende modificar un personaje
        if(accion.equals("mod")){
            // Comprobamos que los datos sean diferentes a los cargados inicialmente
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

    /**
     * M??todo encargado de comprobar que la fecha introducida sea correcta
     * @param stringFecha
     * @return Devuelva un String con la fecha comprobado
     * @throws ParseException Excepcion que se lanza cuando el formato de la fecha
     * introducido no corresponde al deseado
     */
    @NonNull
    private String comprobarFecha(String stringFecha) throws ParseException {
        Date fecha = null;
        // Nos creamos un formato de fecha v??lido en funci??n de como se guarden en la API
        //TODO si vuelve a funcionar la API de BB
        //SimpleDateFormat formato = new SimpleDateFormat("MM-dd-yyyy");
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        // Parseamos la fecha a Date
        fecha = formato.parse(stringFecha);
        // Volvemos a parsear la fecha a String
        stringFecha = formato.format(fecha);

        return stringFecha;
    }

    /**
     * M??todo encargado de comprobar que el campo de texto de la fecha sea v??lido y en el caso de
     * desconocer la fecha que se tenga que escribir "unknown". Ya que el formato de la fecha
     * puede llegar a ser poco intuitivo se informa al usuario sobre la correcta edici??n del campo
     * @return
     */
    private boolean comprobarCampoFecha(){
        boolean valid = false;
        // Aceptamos el String unknown, independientemente de que est?? en may??scula o min??scula
        if(txtFechaNacimiento.getText().toString().equalsIgnoreCase("unknown")){
            valid = true;
        }else{
            try {
                // Comprobamos la fecha indicada
                comprobarFecha(txtFechaNacimiento.getText().toString());
                valid = true;
            } catch (ParseException e) {
                // TODO si vuelve a funcionar la API de BB
                //Toasty.error(DetailActivity.this,"Introducza una fecha v??lida seg??n el " +
                        //"formato MM-dd-yyyy").show();
                Toasty.error(DetailActivity.this,"Introducza una fecha v??lida seg??n el " +
                        "formato dd-MM-yyyy o escribe unknown si desconoce la fecha").show();
            }
        }

        return valid;
    }

    /**
     * M??todo encargado de crear un AlertDialog para comprobar la modificaci??n de los datos de un
     * personaje
     * @return Devuelve un objeto de la clase AlertDialog
     */
    @NonNull
    private AlertDialog createAlertDialog(){
        // Obtenemos una instancia de un AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
        // Configuramos el mensaje y el t??tulo
        builder.setMessage("??De verdad quiere modificar los datos del personaje?").setTitle("Modificar");
        // Configuramos el bot??n de No
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Se cancela la operaci??n de eliminar el personaje
                Toasty.error(DetailActivity.this, "Modificaci??n cancelada",
                        Toasty.LENGTH_SHORT,true).show();
            }
        });
        // Configuramos el bot??n de Si
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Se finaliza la operaci??n de modificar los datos del personaje
                Toasty.success(DetailActivity.this, "Modificaci??n realizada",
                        Toasty.LENGTH_SHORT, true).show();
                // Iniciamos la vuelta a la ListActivity y guardamos los datos modificamos
                volver();
            }
        });
        // Devolvemos el objeto creado por el builder
        return builder.create();
    }

    /**
     * M??todo encargado de crear un AlertDialog que permite la entrada de informaci??n
     * @return Devuelve un objeto de la clase AlertDialog
     */
    private AlertDialog createInputDialog(){
        // Obtenemos una instancia de un AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Modificamos el t??tulo
        builder.setTitle("Introduzca la ruta de una imagen");
        // Declaramos e inicializamos un EditText
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        // Insertamos el EditText en el objeto de la clase AlertDialog que nos vamos a crear
        builder.setView(input);
        // Configuramos el bot??n de OK
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Parseamos el String a una uri
                uri = Uri.parse(input.getText().toString());
                // Cargamos la imagen
                Glide.with(DetailActivity.this)
                        .load(uri)
                        .placeholder(progressDrawable)
                        .error(R.drawable.image_not_found)
                        .into(imgPersonajeGrande);
                // Avisamos que la imagen se cambiado
                imagenNueva = true;
                update();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    /**
     * M??todo encargado de preparar el intent devuelta hacia la ListActivity desde la cual se
     * inicio esta actividad
     */
    private void volver(){
        // Configuramos el intent
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name", txtNombrePersonaje.getText() + "");
        returnIntent.putExtra("actor", txtActorPersonaje.getText() + "");
        returnIntent.putExtra("uri", uri.toString());
        returnIntent.putExtra("birthday", txtFechaNacimiento.getText() + "");
        returnIntent.putExtra("status",sbEstadoPersonaje.getSelectedItem().toString());
        // Damos la actividad como finalizada de forma correcta
        setResult(DetailActivity.RESULT_OK, returnIntent);
        // Terminamos esta actividad
        finish();
    }

    /**
     * M??todo encargado de comprobar que los campos de vista no est??n vacios y que hayan modificado
     * con respecto a la informaci??n cargada inicialmente
     */
    private void update(){
        // Una vez que hayamos comprobado que los campos no est??n vacios
        if(!comprobarCamposVacios()){
            // Comprobamos que los campos sean diferentes. Si son diferentes habilitamos el bot??n
            // de guardar
            if(comprobarCamposDiferentes()){
                btnGuardar.setEnabled(true);
                // Sino el bot??n se deshabilita
            }else{
                btnGuardar.setEnabled(false);
            }
        }else{
            btnGuardar.setEnabled(false);
        }
    }
}