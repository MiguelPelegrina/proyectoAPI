package com.example.proyecto.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.proyecto.R;
import com.example.proyecto.model.DBAccess;
import com.example.proyecto.model.User;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //Declaracion de variables
    private Button btnLogin;
    private Button btnRegistro;
    private EditText txtUsuario;
    private EditText txtContrasena;
    private DBAccess controladorDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializacion de variables
        // Asociamos los elemento del layout con el código
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegistro = (Button) findViewById(R.id.btnRegistro);
        txtUsuario = (EditText) findViewById(R.id.txtUsuario);
        txtContrasena = (EditText) findViewById(R.id.txtContrasena);
        // Instanciamos el controlador de la base de datos
        controladorDB = new DBAccess(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent i = new Intent(MainActivity.this,);
                if(comprobarCampos()) {
                    ArrayList<User> users = new ArrayList<>();
                    users = controladorDB.getAllUser();
                    User user = new User(txtUsuario.getText().toString(), txtContrasena.getText().toString());
                    if (users.contains(user)) {
                        for (User u : users) {
                            if (u.getName().equals(user.getName())) {
                                if (u.getPassword().equals(user.getPassword())) {
                                    Toast("Login realizado");
                                    //startActivity();
                                } else {
                                    Toast("No se ha podido logear, comprueba el nombre y/o la contraseña");
                                }
                            }
                        }
                    } else {
                        Toast("No se ha podido logear, debe registrarse primero");
                    }
                }else{
                    Toast("Debe introducidr datos válidos");
                }
            }
        });

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comprobarCampos()){
                    User user = new User(txtUsuario.getText().toString(), txtContrasena.getText().toString());
                    long result = controladorDB.insert(user);
                    if (result != -1){
                        Toast("Se ha registrado exitosamente");
                    }else{
                        Toast("No se ha podido registrar, probablemente ya esté registrado");
                    }
                }else{
                    Toast("Debe introducidr datos válidos");
                }
            }
        });
    }

    /**
     * Método para lanzar un toast con solo el contenido del mensaje como parámetro
     * @param msg Mensaje que se muestra al usuario
     */
    public void Toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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