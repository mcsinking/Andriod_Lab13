package com.android.mcsin.lab13;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends Activity
{
    EditText searchText;

    TextView tv;

    SQLiteDatabase mydatabase;
    String bookInfos;

    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);







        tv = new TextView(this);



        ScrollView outerLayout = new ScrollView(this);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        searchText = new EditText(this);
        searchText.setWidth(200);

        Button searchButton = new Button(this);
        searchButton.setText("Search");
        searchButton.setOnClickListener(searchButtonListener);



        innerLayout.addView(searchText);
        innerLayout.addView(searchButton);
        innerLayout.addView(tv);

        outerLayout.addView(innerLayout);

        setContentView(outerLayout);
    }

    View.OnClickListener searchButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            doQuery(searchText.getText().toString());
        }
    };



    private void doQuery(String searchString)
    {

        String urlString = "";

        try
        {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        AsyncHttpClient client = new AsyncHttpClient();

        // AsyncHttpClient.get(String url, ResponseHandlerInterface responseHandler)
        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(JSONObject jsonObject)
                    {
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                        bookInfos=null;

                           // bookInfos=jsonObject.toString().length()+"";

                        if (jsonObject.toString().length()>6000){
                            bookInfos=jsonObject.toString().substring(0,6000);

                        }else {
                            bookInfos=jsonObject.toString().substring(0,jsonObject.toString().length()-1);
                        }

                        mydatabase = openOrCreateDatabase("myDatabase",MODE_PRIVATE,null);
                        mydatabase.execSQL("DROP TABLE IF EXISTS " + "Test");
                        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Test(booksdata VARCHAR(7000));");
                        mydatabase.execSQL("INSERT INTO Test VALUES('"+bookInfos+"');");
                       // mydatabase.execSQL("INSERT INTO Test VALUES('book1');");
                        //resultText.setText(jsonObject.toString());

                        Cursor resultSet = mydatabase.rawQuery("Select * from Test", null);

                        int rowCount = resultSet.getCount();
                        //Log.d("Count", Integer.toString(rowCount));
                        resultSet.moveToFirst();

                        String displayString = "";
                        for(int i = 0; i< rowCount; i++)
                        {
                            displayString = displayString + resultSet.getString(0);

                            displayString = displayString + "\n";
                            resultSet.moveToNext();
                        }
                        tv.setText(displayString);

                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error)
                    {
                        Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Query Failure", statusCode + " " + throwable.getMessage());
                    }
                });
    }
}