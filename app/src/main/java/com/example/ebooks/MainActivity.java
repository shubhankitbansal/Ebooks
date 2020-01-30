package com.example.ebooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private EditText book;
    private TextView title;
    private TextView author;

    class FetchBook extends AsyncTask<String,Void,String>{

        private TextView title;
        private TextView author;

        FetchBook(TextView title, TextView author) {
            this.title = title;
            this.author = author;
        }


        @Override
        protected void onPostExecute(String s) {

            try {
                JSONObject object = new JSONObject(s);
                JSONArray itemArray = object.getJSONArray("items");

                int i=0;
                String title1=null,author1=null;
                while (i < itemArray.length() && (author1 == null || title1 == null)) {
                    JSONObject book = itemArray.getJSONObject(i);
                    JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                    try {
                        title1 = volumeInfo.getString("title");
                        author1 = volumeInfo.getString("authors");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                if (title1 != null && author1 != null) {
                    title.setText(title1);
                    author.setText(author1);
                } else {
                    title.setText(R.string.no_results);
                    author.setText("");
                }

            } catch (Exception e) {
                title.setText(R.string.no_results);
                author.setText("");
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            return NetworkUtils.getBookInfo(strings[0]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        book = findViewById(R.id.bookInput);
        title = findViewById(R.id.titleText);
        author = findViewById(R.id.authorText);

    }

    public void searchBooks(View view) {
        String queryString = book.getText().toString();

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null ) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()
                && queryString.length() != 0) {
            new FetchBook(title, author).execute(queryString);
            author.setText("");
            title.setText(R.string.loading);
        } else {
            if (queryString.length() == 0) {
                author.setText("");
                author.setText(R.string.no_search_term);
            } else {
                author.setText("");
                title.setText(R.string.no_network);
            }
        }
    }
}
