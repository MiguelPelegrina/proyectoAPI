package com.example.proyecto.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.proyecto.R;
import com.example.proyecto.io.UserDatabaseAccess;
import com.example.proyecto.model.User;

import es.dmoral.toasty.Toasty;

/**
 * Actividad que gestiona el login y el registro del usuario
 */
public class LoginActivity extends AppCompatActivity {
    //Declaracion de variables
    private Button btnLogin;
    private Button btnRegistro;
    private EditText txtUsuario;
    private EditText txtContrasena;
    private UserDatabaseAccess controladorDB;
    // Variables encargadas de guardar los datos de login del usuario que ha realizado un login
    // de forma exitosa
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializacion de variables
        // Asociamos los elemento del layout con el código
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegistro = (Button) findViewById(R.id.btnRegistro);
        txtUsuario = (EditText) findViewById(R.id.txtUsuario);
        txtContrasena = (EditText) findViewById(R.id.txtContrasena);
        // Instanciamos el controlador de la base de datos
        controladorDB = new UserDatabaseAccess(this);

        //
        saveLoginCheckBox = (CheckBox) findViewById(R.id.cbPreferencias);
        loginPreferences = getSharedPreferences("loginPreferences", MODE_PRIVATE);
        loginPreferencesEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin",false);
        if(saveLogin){
            txtUsuario.setText(loginPreferences.getString("username", ""));
            txtContrasena.setText(loginPreferences.getString("password",""));
            saveLoginCheckBox.setChecked(true);
        }

        Toasty.Config.getInstance()
                .setTextSize(20)
                .apply();

        Toasty.info(this,"Para poder hacer login debe registrarse primero",
                Toasty.LENGTH_LONG, true).show();

        // Oyente que gestiona el evento OnClick sobre el botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                if(comprobarCampos()) {
                    if (controladorDB.getUser(txtUsuario.getText().toString())) {
                        if (controladorDB.getUser(txtUsuario.getText().toString(), txtContrasena.getText().toString())) {
                            Toasty.success(LoginActivity.this,"Login realizado",
                                    Toasty.LENGTH_SHORT,true).show();
                                    if(saveLoginCheckBox.isChecked()){
                                        loginPreferencesEditor.putBoolean("saveLogin", true);
                                        loginPreferencesEditor.putString("username", txtUsuario.getText().toString());
                                        loginPreferencesEditor.putString("password", txtContrasena.getText().toString());
                                        loginPreferencesEditor.commit();
                                    }else{
                                        loginPreferencesEditor.clear();
                                        loginPreferencesEditor.commit();
                                    }
                                    startActivity(intent);
                        } else {
                            Toasty.error(LoginActivity.this,"No se ha podido logear, " +
                                            "compruebe el nombre y/o la contraseña",
                                    Toasty.LENGTH_LONG,true).show();
                        }
                    } else {
                        Toasty.error(LoginActivity.this,
                                "No se ha podido logear, debe registrarse primero",
                                Toasty.LENGTH_LONG,true).show();
                    }
                }else{
                    Toasty.error(LoginActivity.this,
                            "Debe introducidr datos válidos", Toasty.LENGTH_LONG,
                            true).show();
                }
            }
        });

        // Oyente que gestiona el evento OnClick sobre el botón de registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comprobarCampos()){
                    User user = new User(txtUsuario.getText().toString(), txtContrasena.getText().toString());
                    try{
                        long result = controladorDB.insert(user);
                        if (result != -1){
                            Toasty.success(LoginActivity.this,
                                    "Se ha registrado exitosamente, ya puede hacer Login",
                                    Toasty.LENGTH_SHORT,true).show();
                        }
                    }catch(SQLiteConstraintException e){
                        Toasty.error(LoginActivity.this,
                                "No se ha podido registrar, probablemente ya esté " +
                                        "registrado", Toasty.LENGTH_LONG,true).show();
                    }
                }else{
                    Toasty.error(LoginActivity.this,
                            "Debe introducidr datos válidos", Toasty.LENGTH_LONG,
                            true).show();
                }
            }
        });
    }

    /**
     * Método que comprueba que los campos de texto no estén vacios
     * @return Devuelve true si la longitud del texto de los campos de texto es mayor que 0 y sino
     * devuelve false
     */
    public boolean comprobarCampos(){
        boolean camposValidos = false;

        if(txtUsuario.getText().length() > 0 && txtContrasena.getText().length() > 0){
            camposValidos = true;
        }

        return camposValidos;
    }
}