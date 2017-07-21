package com.example.vikramkumaresan.v4;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Get_Cycle_Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__cycle__info);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    public void send_data(View view){
        EditText name = (EditText)findViewById(R.id.name);
        EditText cycle = (EditText)findViewById(R.id.cycle);
        EditText location = (EditText)findViewById(R.id.location);

        MainActivity.go.putExtra("name",name.getText().toString());
        MainActivity.go.putExtra("cycle",cycle.getText().toString());
        MainActivity.go.putExtra("location",location.getText().toString());
        setResult(102);
        finish();
    }
}
