package com.voice;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dialogGator.DBHelper;
import com.dialogGator.ListenerTask;
import com.dialogGator.PostTaskListener;
import com.dialogGator.Product;
import com.dialogGator.ProductAttributes;
import com.dialogGator.ReaderTask;

import java.util.ArrayList;

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
                    if (!response.isError()){
//                        final ReaderTask readerTask = new ReaderTask(activity.getApplicationContext(),postTaskListener);
                        Result result =  response.getResult();
                        String speech = result.getFulfillment().getSpeech();
                        switch (result.getAction()) {
                            case "clothes.product":
                                PostTaskListener postTaskListener = init(activity);
                                final ReaderTask readerTask = new ReaderTask(activity.getApplicationContext(),postTaskListener);
                                if (!result.getParameters().isEmpty()) {
                                    if (result.getParameters().get("items").toString().contains("\""))
                                        ProductAttributes.productMap.put("category", result.getParameters().get("items").toString().replaceAll("\"", ""));
                                    else
                                        ProductAttributes.productMap.put("category", result.getParameters().get("items").toString());
                                    TTS.speak(speech);
                                    readerTask.execute(ProductAttributes.productMap);
                                }
                                else{
                                    speech= "I couldn't understand the item you said. What would you like to buy?";
                                    TTS.speak(speech);
                                }
                                speech = speech;// + "Who would like to buy it for? Men or Women?";

                                break;
                            case "clothes.startwithname":
                                TTS.speak(speech);
                                //TTS.speak();
                                break;
                            case "clothes.color":
                                ProductAttributes.productMap.put("color",result.getParameters().values());
                                speech = speech + "Are you looking for any specific brand? If yes what brand?";
                                TTS.speak(speech);
                                break;
                            case "clothes.size":
                                ProductAttributes.productMap.put("size",result.getParameters().values());
                                //ProductAttributes.productMap.put("pricestart",result.getParameters().values());
                                speech = speech + "What price range are you looking for?";
                                TTS.speak(speech);
                                break;
                            case "clothes.brand":
                                ProductAttributes.productMap.put("brand",result.getParameters().values());
                                TTS.speak(speech);
                                TTS.stopSpeaking();
                                break;
                            case "clothes.priceless":
                                ProductAttributes.productMap.put("priceStart","0");
                                ProductAttributes.productMap.put("priceEnd",result.getParameters().values());
                                speech = speech + "Do you have any colour in mind? If yes what color?";
                                TTS.speak(speech);
                                break;
                            case "clothes.pricegreater":
                                ProductAttributes.productMap.put("priceStart",result.getParameters().values());
                                ProductAttributes.productMap.put("priceEnd","999999999999");
                                speech = speech + "Do you have any colour in mind? If yes what color?";
                                TTS.speak(speech);
                                break;
                            case "clothes.pricevalue":
                                ProductAttributes.productMap.put("priceStart",result.getParameters().values());
                                ProductAttributes.productMap.put("priceEnd",result.getParameters().values());
                                speech = speech + "Do you have any colour in mind? If yes what color?";
                                TTS.speak(speech);
                                break;
                            case "clothes.pricebetween":
                                ProductAttributes.productMap.put("priceStart",result.getParameters().get("rangestart"));
                                ProductAttributes.productMap.put("priceEnd",result.getParameters().get("rangeend"));
                                speech = speech + "Do you have any colour in mind? If yes what color?";
                                TTS.speak(speech);
                                break;
                            case "clothes.gender":
                                ProductAttributes.productMap.put("gender",result.getParameters().values());
                                speech = speech + "What size would you like to see?";
                                TTS.speak(speech);
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
                            case "clothes.clearfilters":
                                TTS.speak(speech);
                                break;
                            case "clothes.product-number":
                                TTS.speak(speech);
                                break;
                            default:
                                TTS.speak(speech);
                        }
                    }
                }
                catch (Exception e){
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
//        ArrayList<Product> searchList = new ArrayList<>();
//        Product item = new Product("vest101",
//                "Thermal vest",
//                "Our thermal vest, made from organic bamboo with recycled plastic down filling, is a favorite of both men and women. You’ll help the environment, and have a wear-easy piece for many occasions.",
//                95);
//        searchList.add(item);
////        postTaskListener.onPostTask(searchList,activity.getApplicationContext());
    }
}
