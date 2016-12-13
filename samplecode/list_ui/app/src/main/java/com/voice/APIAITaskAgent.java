package com.voice;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.dialogGator.DBHelper;
import com.dialogGator.DetailActivity;
import com.dialogGator.ListenerTask;
import com.dialogGator.MainFragment;
import com.dialogGator.PostTaskListener;
import com.dialogGator.Product;
import com.dialogGator.ProductAttributes;
import com.dialogGator.ReaderTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import ai.api.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import ai.api.ui.AIDialog;

/**
 * Created by yash on 11/11/16.
 */

@Singleton
public class APIAITaskAgent {
    private final AIDialog aiDialog;
    public int flag_ground=0;
    public int correctASR = 0;
    public int incorrectASR = 0;
    public String prevAttrValue= "";
    @Inject
    public APIAITaskAgent(final Activity activity){
        AIConfiguration aiConfiguration =  new AIConfiguration(
                "ca9ddb0dbbb64343a90b2ea4e75a41a6",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDialog = new AIDialog(activity, aiConfiguration);
        Log.i("HashMap", String.valueOf(ProductAttributes.productMap));

//        final PostTaskListener<ArrayList<Product>> postTaskListener = new PostTaskListener<ArrayList<Product>>() {
//            @Override
//            public void onPostTask(ArrayList<Product> result, Context context ) {
//                Log.i("Result", result.toString());
//            }
//        };

        aiDialog.setResultsListener(new AIDialog.AIDialogListener() {
            @Override
            public void onResult(AIResponse response) {
                try {
                    if (!response.isError()) {
//                        final ReaderTask readerTask = new ReaderTask(activity.getApplicationContext(),postTaskListener);
                        Result result = response.getResult();
                        String speech = result.getFulfillment().getSpeech();
                        if (result.getParameters().get("help") != null) {
                            TTS.speak("Hello. You can say \"I want a shirt\" or you can choose from Pant, Jean, Short, " +
                                    "Shirt, Jacket, Skirt, Dress or Legging. You can start over by saying \"start over\".");
                        } else if (result.getParameters().get("purpose") != null) {
                            TTS.speak("Hi, I am Reena. I am your shopping assistant and can assist you in buying clothes. " +
                                    "I can show you our collection of Pants, Jeans, Shorts, Shirts, Jackets, Skirts, Dresses and Leggings.");
                        } else {
                            switch (result.getAction()) {
                                case "open.prompt":
                                    if(flag_ground==1 && result.getResolvedQuery().toString().equals("yes")){
                                        flag_ground=0;
                                        correctASR= correctASR+1;
                                        setAttrValue(prevAttrValue);
                                        TTS.speak(getNextDialogue());
                                    }
                                    else if (flag_ground==1 && result.getResolvedQuery().toString().equals("no")){
                                        flag_ground=0;
                                        incorrectASR = incorrectASR+1;
                                        TTS.speak(findDialogue(ProductAttributes.productMap.get("prevDialog").toString()));
                                    }
                                    else if(result.getResolvedQuery().toString().equals("no")){
                                        setAttrValue("");
                                    }
                                    else {
                                        PostTaskListener postTaskListener = init(activity);
                                        final ReaderTask readerTask = new ReaderTask(activity.getApplicationContext(), postTaskListener);
                                        if (!result.getParameters().isEmpty() && ProductAttributes.productMap.get("open_done") != "1") {
                                            String[] queryItemsList = getQueryItems(result.getParameters());
                                            if (queryItemsList != null) {
                                                ProductAttributes.productMap.put("open_done", "1");
                                                HashMap resultMap = DBHelper.getInstance(activity.getApplicationContext()).populateMapOnOpenPrompt(queryItemsList);
                                                if (checkIfItemExists(resultMap)){
                                                    //readerTask.execute(ProductAttributes.productMap);
                                                    speech = getNextDialogue();
                                                    TTS.speak(speech);
                                                }

                                            } else {
                                                speech = getRandomUtterance();
                                                TTS.speak(speech);
                                            }
                                        } else if (ProductAttributes.productMap.get("open_done") == "1") {
                                            speech = getRandomUtterance();
                                            //speech = getNextDialogue();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                case "clothes.product":
                                    PostTaskListener postTaskListener_prod = init(activity);
                                    final ReaderTask readerTask_prod = new ReaderTask(activity.getApplicationContext(), postTaskListener_prod);
                                    if (!result.getParameters().isEmpty()) {
                                        if (result.getParameters().get("items").toString().contains("\""))
                                            ProductAttributes.productMap.put("category", result.getParameters().get("items").toString().replaceAll("\"", ""));
                                        else
                                            ProductAttributes.productMap.put("category", result.getParameters().get("items").toString());
                                        TTS.speak(speech);
                                        readerTask_prod.execute(ProductAttributes.productMap);
                                    } else {
                                        speech = getRandomUtterance();
                                        TTS.speak(speech);
                                    }

                                    break;
                                case "clothes.gender":
                                    if(flag_ground==0){
                                        groundDialog(result.getParameters().get("gender").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_gender = init(activity);
                                        final ReaderTask readerTask_gender = new ReaderTask(activity.getApplicationContext(), postTaskListener_gender);
                                        if (!result.getParameters().isEmpty()) {
                                            if (result.getParameters().get("gender").toString().contains("\""))
                                                ProductAttributes.productMap.put("gender", result.getParameters().get("gender").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("gender", result.getParameters().get("gender").toString());
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                            readerTask_gender.execute(ProductAttributes.productMap);
                                        } else {
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                case "clothes.size":
                                    if(flag_ground==0){
                                        groundDialog(result.getParameters().get("size").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_size = init(activity);
                                        final ReaderTask readerTask_size = new ReaderTask(activity.getApplicationContext(), postTaskListener_size);
                                        if (!result.getParameters().isEmpty()) {
                                            if (result.getParameters().get("size").toString().contains("\""))
                                                ProductAttributes.productMap.put("size", result.getParameters().get("size").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("size", result.getParameters().get("size").toString());
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                            readerTask_size.execute(ProductAttributes.productMap);
                                        } else {
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                case "clothes.color":
                                    if(flag_ground==0){
                                        groundDialog(result.getParameters().get("color").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_color = init(activity);
                                        final ReaderTask readerTask_color = new ReaderTask(activity.getApplicationContext(), postTaskListener_color);
                                        if (!result.getParameters().isEmpty()) {
                                            if (result.getParameters().get("color").toString().contains("\""))
                                                ProductAttributes.productMap.put("color", result.getParameters().get("color").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("color", result.getParameters().get("color").toString());
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                            readerTask_color.execute(ProductAttributes.productMap);
                                        } else {
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;

                                case "clothes.pricevalue":
                                        PostTaskListener postTaskListener_price = init(activity);
                                        final ReaderTask readerTask_price = new ReaderTask(activity.getApplicationContext(), postTaskListener_price);
                                        if (!result.getParameters().isEmpty()) {
                                            if (result.getParameters().get("price") != null) {
                                                if (result.getParameters().get("price").toString().contains("\""))
                                                    ProductAttributes.productMap.put("priceStart", result.getParameters().get("price").toString().replaceAll("\"", ""));
                                                else
                                                    ProductAttributes.productMap.put("priceStart", result.getParameters().get("price").toString());

                                                if (result.getParameters().get("price").toString().contains("\""))
                                                    ProductAttributes.productMap.put("priceEnd", result.getParameters().get("price").toString().replaceAll("\"", ""));
                                                else
                                                    ProductAttributes.productMap.put("priceEnd", result.getParameters().get("price").toString());
                                            } else {
                                                if (result.getParameters().get("rangestart") == null) {
                                                    ProductAttributes.productMap.put("priceStart", "0");
                                                } else if (result.getParameters().get("rangestart").toString().contains("\""))
                                                    ProductAttributes.productMap.put("priceStart", result.getParameters().get("rangestart").toString().replaceAll("\"", ""));
                                                else
                                                    ProductAttributes.productMap.put("priceStart", result.getParameters().get("rangestart").toString());

                                                if (result.getParameters().get("rangeend") == null) {
                                                    ProductAttributes.productMap.put("priceEnd", "9999999");
                                                } else if (result.getParameters().get("rangeend").toString().contains("\""))
                                                    ProductAttributes.productMap.put("priceEnd", result.getParameters().get("rangeend").toString().replaceAll("\"", ""));
                                                else
                                                    ProductAttributes.productMap.put("priceEnd", result.getParameters().get("rangeend").toString());
                                            }
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                            readerTask_price.execute(ProductAttributes.productMap);
                                        } else {
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                        TTS.speak(speech);

                                    break;

                                case "clothes.brand":
                                    if(flag_ground==0){
                                        groundDialog(result.getParameters().get("brand").toString());
                                    }
                                    else {
                                        PostTaskListener postTaskListener_brand = init(activity);
                                        final ReaderTask readerTask_brand = new ReaderTask(activity.getApplicationContext(), postTaskListener_brand);
                                        if (!result.getParameters().isEmpty()) {
                                            if (result.getParameters().get("brand").toString().contains("\""))
                                                ProductAttributes.productMap.put("brand", result.getParameters().get("brand").toString().replaceAll("\"", ""));
                                            else
                                                ProductAttributes.productMap.put("brand", result.getParameters().get("brand").toString());
                                            speech = getNextDialogue();
                                            TTS.speak(speech);
                                            readerTask_brand.execute(ProductAttributes.productMap);
                                        } else {
                                            speech = getRandomUtterance();
                                            TTS.speak(speech);
                                        }
                                    }
                                    break;
                                ///////////////////////////////////////////
                                case "clothes.repeatdialog":
                                    TTS.speak(findDialogue(ProductAttributes.productMap.get("prevDialog").toString()));
                                    break;
                                case "clothes.add":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.orders":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.remove":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.viewlogs":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.more":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.filters":
                                    TTS.speak(speech);
                                    break;
                                case "clothes.start-over":
                                    clearFilters();
                                    TTS.speak("Ok, I have cleared all filters. Please start with selecting " +
                                            "an item from Pant, Jean, Short, Shirt, Jacket, Skirt, Dress or Legging.");
                                    break;
                                case "clothes.product-number":
                                    PostTaskListener postTaskListener_prodNum = init(activity);
                                    final ReaderTask readerTask_prodNum = new ReaderTask(activity.getApplicationContext(), postTaskListener_prodNum);
                                    if (!result.getParameters().isEmpty()) {
                                        if (result.getParameters().get("productnum").toString().contains("\""))
                                            ProductAttributes.productMap.put("itemId", result.getParameters().get("productnum").toString().replaceAll("\"", ""));
                                        else
                                            ProductAttributes.productMap.put("itemId", result.getParameters().get("productnum").toString());
                                        speech = "Ok. Sure.";
                                        TTS.speak(speech);
                                        readerTask_prodNum.execute(ProductAttributes.productMap);
                                    } else {
                                        speech = getRandomUtterance();
                                        TTS.speak(speech);
                                    }
                                break;
                                case "clothes.startwithname":
                                    TTS.speak(speech);
                                    break;
                                default:
                                    TTS.speak(speech);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("DialogError", e.toString());
                    aiDialog.close();
                }
            }


            @Override
            public void onError(AIError error) {
                Log.e("DialogError", error.toString());
//                Toast.makeText(activity, error.toString(), Toast.LENGTH_SHORT).show();
                aiDialog.close();
            }

            @Override
            public void onCancelled() {
                Log.i("tag", "here");
            }
        });
    }
    public void startRecognition(){
        aiDialog.showAndListen();
    }

    public PostTaskListener<ArrayList<Product>> init (Activity activity){
       // ListenerTask lt = new ListenerTask();
        PostTaskListener<ArrayList<Product>> postTaskListener = ((ListenerTask) activity.getApplication()).getPostTaskListener();

        return postTaskListener;
    }

    public String getRandomUtterance(){
        String [] arr = {"I couldn't understand what you just said. Please say it again?",
                "I couldn't find the answer you just said. Can you repeat it?",
                "That is not a valid option. For help, please say \"help\".",
                "I am lost. Can you please repeat that? If you are stuck, please say \"help\". To start over, say \"start over\""};
        Random random = new Random();

        // randomly selects an index from the arr
        int select = random.nextInt(arr.length);

        // prints out the value at the randomly selected index
       return arr[select];
    }

    public String[] getQueryItems(HashMap queryMap){
        if(queryMap!=null) {
            String queryItems = queryMap.get("query").toString();
            String[] queryItemsList = queryItems.split(" ");
            return queryItemsList;
        }
        else return null;
    }

    public String getNextDialogue(){
        String utterance = "";
        ProductAttributes.productMap.put("category", "shirt");
        ProductAttributes.productMap.put("color", "blue");
        HashMap productMap = ProductAttributes.productMap;
        if(productMap.get("category")==null){
            utterance = findDialogue("1");
            ProductAttributes.productMap.put("prevDialog", "1");
        }
        else if(productMap.get("gender")==null){
            utterance = findDialogue("2");
            ProductAttributes.productMap.put("prevDialog", "2");
        }
        else if(productMap.get("size")==null){
            utterance = findDialogue("3");
            ProductAttributes.productMap.put("prevDialog", "3");
        }
        else if(productMap.get("color")==null){
            utterance = findDialogue("4");
            ProductAttributes.productMap.put("prevDialog", "4");
        }
        else if(productMap.get("price")==null && productMap.get("priceStart")==null){
            utterance = findDialogue("5");
            ProductAttributes.productMap.put("prevDialog", "5");
        }
        else if(productMap.get("brand")==null){
            utterance = findDialogue("6");
            ProductAttributes.productMap.put("prevDialog", "6");
        }
        else{
            utterance = findDialogue("7");
            ProductAttributes.productMap.put("prevDialog", "7");
        }
        return utterance;
    }

    public void clearFilters(){
        ProductAttributes.productMap.clear();
        ProductAttributes.productMap.put("open_done", "0");
        prevAttrValue = "";
        flag_ground = 0;
    }

    public String findDialogue(String value){
        String utterance = "";
        switch (value){
            case "1":
                utterance = "Next, what product do you want? You can choose from shirts, " +
                        "shorts, pants, jeans, dresses, skirts, jackets or leggings.";
                break;
            case "2":
                utterance = "Ok. Who do you want to buy it for? Men or Women?";
                break;
            case "3":
                utterance = "Great, What size do you want it in?";
                break;
            case "4":
                utterance = "Next, can you tell me the color you want it in?";
                break;
            case "5":
                utterance = "Ok. What price range are you looking for?";
                break;
            case "6":
                utterance = "Ok. Do you have any brand in mind?";
                break;
            case "7":
                utterance = "These are the filtered items. Please select a product number.";
                break;

        }
        return utterance;
    }

    public boolean checkIfItemExists(HashMap modifiedHashMap){
        if (modifiedHashMap==null){
            TTS.speak("Sorry, I couldn't find that item. Please select an item from " +
                    "Pants, Jeans, Shorts, Shirts, Jackets, Skirts, Dresses and Leggings. ");
            return false;
        }
        else {
            populateModifiedHashMap(modifiedHashMap);
            return true;
        }

    }
    public void populateModifiedHashMap(HashMap modifiedHashMap){
        Iterator it = modifiedHashMap.entrySet().iterator();
        if(!it.hasNext()){
            TTS.speak(getRandomUtterance());
        }
        else {
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                ProductAttributes.productMap.put(pair.getKey(), pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    public void setAttrValue(String value){
        if(ProductAttributes.productMap.get("prevDialog") != null) {
            if (ProductAttributes.productMap.get("prevDialog") == "2") {
                ProductAttributes.productMap.put("gender", value);
            } else if (ProductAttributes.productMap.get("prevDialog") == "3") {
                ProductAttributes.productMap.put("size", value);
            } else if (ProductAttributes.productMap.get("prevDialog") == "4") {
                ProductAttributes.productMap.put("color", value);
            } else if (ProductAttributes.productMap.get("prevDialog") == "5") {
                ProductAttributes.productMap.put("priceStart", value);
                ProductAttributes.productMap.put("priceEnd", value);
            } else if (ProductAttributes.productMap.get("prevDialog") == "6") {
                ProductAttributes.productMap.put("brand", value);
            }
        }
    }

    public void groundDialog(String value){
        flag_ground=1;
        prevAttrValue = value;
        TTS.speak("Did you say "+ value + " ?");
    }
}
