package com.lgcns.dcxandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.gson.Gson;
import com.lgcns.dcxandroid.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    ArrayList<Song> songList;
    ArrayList<Song> searchSongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        String time = StringUtil.getCurrentDateTime();
        activityMainBinding.timeText.setText(time);

        initializeDomesticArrayList();

        activityMainBinding.foreignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeOverseasArrayList();

                activityMainBinding.foreignButton.setTextColor(Color.RED);
                activityMainBinding.domesticButton.setTextColor(Color.BLACK);
            }
        });

        activityMainBinding.domesticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDomesticArrayList();

                activityMainBinding.domesticButton.setTextColor(Color.RED);
                activityMainBinding.foreignButton.setTextColor(Color.BLACK);
            }
        });

        activityMainBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSongList = new ArrayList<>();

                for(int i = 0 ; i<songList.size(); i++){
                    Song song = songList.get(i);

                    if(song.title.toLowerCase().contains(newText.toLowerCase()) ||
                            song.singer.toLowerCase().contains(newText.toLowerCase())){
                        searchSongList.add(song);
                    }
                }

                SongAdapter songAdapter = new SongAdapter();
                songAdapter.setSongArrayList(searchSongList);

                activityMainBinding.songChart.setAdapter(songAdapter);

                activityMainBinding.songChart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
                        int a = searchSongList.get(position).id;
                        detailIntent.putExtra("id", a);
                        detailIntent.putExtra("title",searchSongList.get(position).title);
                        startActivity(detailIntent);
                    }
                });

                return true;
            }
        });

    }


    public void initializeListView(){
        SongAdapter songAdapter = new SongAdapter();
        songAdapter.setSongArrayList(songList);

        activityMainBinding.songChart.setAdapter(songAdapter);

        activityMainBinding.songChart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
                detailIntent.putExtra("id", songList.get(position).id);
                detailIntent.putExtra("title",songList.get(position).title);
                startActivity(detailIntent);
            }
        });


    }

    private void initializeDomesticArrayList() {
        activityMainBinding.domesticButton.setTextColor(Color.RED);

        try{
            NetworkTask networkTask = new NetworkTask("http://10.0.2.2:3300/v1/chart/","domestic");
            networkTask.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeOverseasArrayList() {

        activityMainBinding.foreignButton.setTextColor(Color.RED);
        try{
            NetworkTask networkTask = new NetworkTask("http://10.0.2.2:3300/v1/chart/","overseas");
            networkTask.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public class NetworkTask extends AsyncTask<Void, Void, String> {

        String url;
        String values;
        Gson gson = new Gson();

        NetworkTask(String url, String values){
            this.url = url;
            this.values = values;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.HttpURLConnectionGet(url, values);
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        }

        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다.

            try{
                songList = new ArrayList<>();
                String json= result;

                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("chartList");

                int index = 0;
                while (index < jsonArray.length()){
                    Song song = gson.fromJson(jsonArray.get(index).toString(), Song.class);
                    songList.add(song);

                    index++;
                }

                initializeListView();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }




}