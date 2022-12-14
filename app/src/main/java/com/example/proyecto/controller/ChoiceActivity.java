package com.example.proyecto.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.proyecto.R;
import com.example.proyecto.uilities.Preferences;

import es.dmoral.toasty.Toasty;

public class ChoiceActivity extends AppCompatActivity {
    private ConstraintLayout constraintLayout;
    private ImageView imageViewBB;
    private ImageView imageViewHP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout_choice);

        imageViewBB = findViewById(R.id.imageViewBB);
        imageViewHP = findViewById(R.id.imageViewHP);

        // Activamos el icono de "Volver"(flecha atrás)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent i = new Intent(ChoiceActivity.this, ListActivity.class);
        imageViewBB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("selection","bb");
                startActivity(i);
            }
        });

        imageViewHP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("selection", "hp");
                startActivity(i);
            }
        });

        // En función de las preferencias informamos al usuario de las opciones que tiene
        if(Preferences.notificationPreference(this)) {
            Toasty.info(this, "Elija una de las dos bibliotecas que quiera consultar",
                    Toasty.LENGTH_LONG, true).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargamos las preferencias
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
                Intent i = new Intent(ChoiceActivity.this, SettingActivity.class);
                startActivity(i);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}