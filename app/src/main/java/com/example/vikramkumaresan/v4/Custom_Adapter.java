package com.example.vikramkumaresan.v4;

//Custom adapter for list view in Live_Cycles

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Custom_Adapter extends ArrayAdapter {
    Context ctx;
    ArrayList<ArrayList<String>> data;

    public static Intent intent;

    public Custom_Adapter(@NonNull Context context, ArrayList<ArrayList<String>> data) {
        super(context, R.layout.custom_row,data);
        ctx=context;
        this.data=data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater inflator = LayoutInflater.from(getContext());
        View custom_view = inflator.inflate(R.layout.custom_row,parent,false);

        final ArrayList<String> row_data = data.get(position);

        TextView name = (TextView)custom_view.findViewById(R.id.name);
        TextView location = (TextView)custom_view.findViewById(R.id.location);
        TextView cycle = (TextView)custom_view.findViewById(R.id.cycle);

        name.setText(row_data.get(0));
        location.setText(row_data.get(2));
        cycle.setText(row_data.get(1));

        custom_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateTrackingOnServer obj = new UpdateTrackingOnServer(row_data.get(0));
                obj.execute();
                intent = new Intent(ctx,Pointer.class);
                intent.putExtra("giver",row_data.get(0));   //Sends taker name chosen
                MainActivity.publish_name=row_data.get(0);
                ctx.startActivity(intent);
                Live_Cycles.ctx.finish();
                Log.d("TAG","Clicked!");
            }
        });

        return custom_view;
    }

    private class UpdateTrackingOnServer extends AsyncTask<Void,Void,Void>{ //Updates the 'tracking' variable on live_cycles
        String giver;

        public UpdateTrackingOnServer(String selected) {
            giver =selected;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://cycleshare.atwebpages.com/CloseTracking_version4.php");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                OutputStream outStream=connection.getOutputStream();
                OutputStreamWriter outwrite = new OutputStreamWriter(outStream);
                BufferedWriter buffwrite = new BufferedWriter(outwrite);

                String output = URLEncoder.encode("Name","UTF-8")+"="+URLEncoder.encode(giver,"UTF-8")+"&"+URLEncoder.encode("TakerName","UTF-8")+"="+
                        URLEncoder.encode(MainActivity.taker_name,"UTF-8");

                buffwrite.write(output);
                buffwrite.flush();
                buffwrite.close();

                InputStream instream = connection.getInputStream();

            } catch (MalformedURLException e) {
                Log.d("TAG","Malformed URL");
            } catch (ProtocolException e) {
                Log.d("TAG","Protocol Exception");
            } catch (IOException e) {
                Log.d("TAG","IOException "+e.toString());
            } catch (NullPointerException e){

            }
            return null;
        }
    }
}
