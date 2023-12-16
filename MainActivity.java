package com.example.cashrich;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CoinAdapter coinAdapter;

    private TextView itemCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        itemCountTextView = findViewById(R.id.itemCountTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        coinAdapter = new CoinAdapter();
        recyclerView.setAdapter(coinAdapter);

        // Execute the task to fetch coin data asynchronously
        new FetchCoinData().execute();
    }

    // AsyncTask to fetch coin data from CoinMarketCap API
    private class FetchCoinData extends AsyncTask<Void, Void, List<CoinInfo>> {

        @Override
        protected List<CoinInfo> doInBackground(Void... voids) {
            List<CoinInfo> coinInfoList = new ArrayList<>();

            // API call to fetch cryptocurrency data
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=BTC,ETH,LTC")
                    .addHeader("X-CMC_PRO_API_KEY", "27ab17d1-215f-49e5-9ca4-afd48810c149")
                    .build();

            try {
                // Execute the request and handle the response
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // Parse JSON response to extract coin information
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);

                    if (jsonObject.has("data")) {
                        JSONObject data = jsonObject.getJSONObject("data");

                        // Iterate through selected coins (BTC, ETH, LTC)
                        for (String symbol : new String[]{"BTC", "ETH", "LTC"}) {
                            JSONObject coinData = data.getJSONObject(symbol);
                            double change24h = coinData.getJSONObject("quote").getJSONObject("USD").getDouble("percent_change_24h");
                            int rank = coinData.getInt("cmc_rank");
                            double priceUSD = coinData.getJSONObject("quote").getJSONObject("USD").getDouble("price");

                            // Create a CoinInfo object and add it to the list
                            CoinInfo coinInfo = new CoinInfo(symbol, "24h Change: " + change24h + "%, Rank: " + rank + ", Price (USD): " + priceUSD, change24h);
                            coinInfoList.add(coinInfo);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return coinInfoList;
        }

        @Override
        protected void onPostExecute(List<CoinInfo> coinInfoList) {
            super.onPostExecute(coinInfoList);
            // Update the RecyclerView with the fetched data
            coinAdapter.setData(coinInfoList);

            // Update the item count TextView after setting the data
            itemCountTextView.setText("Count: " + coinInfoList.size());
        }
    }

    // RecyclerView Adapter for displaying coin data
    private static class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.CoinViewHolder> {

        private List<CoinInfo> coinList = new ArrayList<>();

        // Set data for the adapter
        public void setData(List<CoinInfo> coins) {
            coinList.clear();
            coinList.addAll(coins);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for a single coin item
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coin, parent, false);
            return new CoinViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
            // Bind data to the ViewHolder
            CoinInfo coin = coinList.get(position);
            holder.bind(coin);
        }

        @Override
        public int getItemCount() {
            // Return the number of items in the list
            return coinList.size();
        }

        // ViewHolder class for a single coin item
        static class CoinViewHolder extends RecyclerView.ViewHolder {
            private TextView textSymbol, textChange;
            private ImageView imageArrow;

            public CoinViewHolder(@NonNull View itemView) {
                super(itemView);
                // Initialize UI components for the ViewHolder
                textSymbol = itemView.findViewById(R.id.textSymbol);
                textChange = itemView.findViewById(R.id.textChange);
                imageArrow = itemView.findViewById(R.id.imageArrow);
            }

            // Bind data to the ViewHolder
            public void bind(CoinInfo coin) {
                textSymbol.setText(coin.getSymbol());
                textChange.setText(coin.getChange());

                // Set the arrow icon based on positive or negative change
                if (coin.getChangeDirection() > 0) {
                    imageArrow.setImageResource(R.drawable.ic_arrow_up);
                    imageArrow.setColorFilter(Color.parseColor("#00FF00"));
                } else {
                    imageArrow.setImageResource(R.drawable.ic_arrow_down);
                    imageArrow.setColorFilter(Color.parseColor("#FF0000"));
                }
            }
        }
    }

    // CoinInfo class represents your data model for each coin
    private static class CoinInfo {
        private String symbol;
        private String change;
        private double changeDirection; // Positive or negative change

        // Constructor for creating a CoinInfo object
        public CoinInfo(String symbol, String change, double changeDirection) {
            this.symbol = symbol;
            this.change = change;
            this.changeDirection = changeDirection;
        }

        // Getter methods for fields
        public String getSymbol() {
            return symbol;
        }

        public String getChange() {
            return change;
        }

        public double getChangeDirection() {
            return changeDirection;
        }
    }
}
