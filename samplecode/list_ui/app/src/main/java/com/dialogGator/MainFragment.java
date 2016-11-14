package com.dialogGator;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.voice.APIAITaskAgent;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
    }

    public static final String PRODUCT_ID = "PRODUCT_ID";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Inflate the layout for this fragment
        //String[] items = getResources().getStringArray(R.array.clothing);

        PostTaskListener<ArrayList<Product>> postTaskListener = new PostTaskListener<ArrayList<Product>>() {
            @Override
            public void onPostTask(final ArrayList<Product> result, Context mContext) {
                //products = result;
                ((ListenerTask) getActivity().getApplication()).setProductList(result);
                ProductListAdapter adapter = new ProductListAdapter(
                        getActivity(), R.layout.list_item, result);
                ListView lv = (ListView) rootView.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(MainFragment.this.getContext(), DetailActivity.class);
                        Product product = result.get(position);
                        intent.putExtra(PRODUCT_ID, product.getProductId());
                        startActivity(intent);
                    }
                });
            }
        };

//        ListenerTask lt = new ListenerTask();
//        lt.setPostTaskListener(postTaskListener);
        ((ListenerTask) getActivity().getApplication()).setPostTaskListener(postTaskListener);
        DataProvider dp = new DataProvider(this.getActivity());
        dp.getProducts();
        //postTaskListener.onPostTask(DataProvider.getFilteredList("1"),this.getContext());
        /*ProductListAdapter adapter = new ProductListAdapter(
                this.getContext(), R.layout.list_item, products);
        ListView lv = (ListView) rootView.findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainFragment.this.getContext(), DetailActivity.class);
                Product product = products.get(position);
                intent.putExtra(PRODUCT_ID, product.getProductId());
                startActivity(intent);
            }
        });*/
        return rootView;


    }




}
