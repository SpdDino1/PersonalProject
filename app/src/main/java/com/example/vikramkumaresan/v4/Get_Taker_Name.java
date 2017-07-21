package com.example.vikramkumaresan.v4;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Get_Taker_Name extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__taker__name);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    public void track(View view){
        EditText name = (EditText)findViewById(R.id.name);
        int flag = 0;
        for(int i=0;i<name.getText().toString().length();i++){
            if(!MainActivity.acceptedCharacters.contains(name.getText().toString().charAt(i))){
                Toast.makeText(this,"Only English Alphabets Allowed",Toast.LENGTH_SHORT).show();
                flag=1;
                break;
            }
        }
        if(name.getText().toString().equals("")){
            Toast.makeText(this,"Please Enter a Name",Toast.LENGTH_LONG).show();
            flag=1;
        }
        if(flag==0){
            MainActivity.taker_name = name.getText().toString();
            Intent go = new Intent(this,Live_Cycles.class);
            startActivity(go);
            finish();
        }
    }
}
