package com.mvhackthon.mvhacks2019app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


public class TensorHelper {
    private static final String TAG = TensorHelper.class.getName();

    TensorFlowInferenceInterface inferenceInterface;
    //final String PB_ADDRESS = "frozen_strawberry.pb";
    String PB_ADDRESS = "frozenmalaria_model.pb";
    //final String[] LABELS = {"Strawberry___Leaf_scorch","Strawberry___healthy"};
    //2 classifications Labels(1,0)
    String[] LABELS = {"not infected", "infected"};
    String input = "conv2d_13_input";
    String output = "dense_8/Sigmoid" ;
    final int WIDTH = 50,HEIGHT = 50;
    final int NUM_OUTPUT_CLASSES = 2;
    TextView textView;

    public String classifyPicture(Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), PB_ADDRESS);
        int bit_map_ids[] = {R.drawable.test_picture_1,
                R.drawable.test_picture_2,
                R.drawable.test_picture_3,
                R.drawable.test_picture_4,
                R.drawable.test_picture_5};
        for (int i = 0; i < bit_map_ids.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), bit_map_ids[i]);
            bitmap = bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
            String prediction = predictFromBitmap(bitmap);
        }
        return "";
    }
    public String classifyPicture(Context context, Bitmap bitmap){
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), PB_ADDRESS);
        return predictFromBitmap(bitmap);
    }

    private String predictFromBitmap(Bitmap bmp){
        assert bmp.getWidth() == bmp.getHeight() && bmp.getWidth() == WIDTH;

        int[] pixels = new int[WIDTH*HEIGHT];
        float[] brightness = new float[WIDTH*HEIGHT];
        float[] r = new float[WIDTH*HEIGHT];
        float[] g = new float[WIDTH*HEIGHT];
        float[] b = new float[WIDTH*HEIGHT];

        bmp.getPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < pixels.length; i++) {
            r[i] = ((pixels[i]) >> 16 & 0xff)/255.0f;
            g[i] = ((pixels[i]) >> 8 & 0xff)/255.0f;
            b[i] = ((pixels[i]) & 0xff)/255.0f;

            //brightness[i] = (0.2126f*r[i] + 0.7152f*g[i] + 0.0722f*b[i]);
        }

        //float[][][] img = {r,g,b};
        //float[][][][] imgBatch = {img};
        float[] inputArray = new float[3*WIDTH*HEIGHT];

        for (int i=0;i<WIDTH*HEIGHT;i++) {
            inputArray[(3*i)] = r[i];
            inputArray[(3*i)+1] = g[i];
            inputArray[(3*i)+2] = b[i];
        }

        // order dependent on model/classes
        float[] prediction = predict(inputArray);

        // form prediction from labels
        float max = 0.0f;
        int maxI = 0;

        for (int i=0;i<prediction.length;i++){
            Log.d(TAG, "Prediction val " + i + " " + prediction[i]);
            max = (max > prediction[i]) ? max : prediction[i];
            maxI = (max > prediction[i]) ? maxI : i;
        }
        String diagnosis = LABELS[0];
        if (prediction[0] >= 0.5) {
            diagnosis = LABELS[1];
        }

        String message = new String();
        if (diagnosis.equals(LABELS[0])) {
            message = "NOT Infected with Malaria";
        }
        else if (diagnosis.equals(LABELS[1])) {
            message = "Infected with Malaria";
        }

        Log.d(TAG, "Prediction was " + message);

        return LABELS[maxI];

    }

    private float[] predict(float[] inputArray){
        float outputArray[] = new float[NUM_OUTPUT_CLASSES];

        // feed network with 4d input
        inferenceInterface.feed(input, inputArray, 1, WIDTH, HEIGHT, 3);
        inferenceInterface.run(new String[] {output});
        inferenceInterface.fetch(output, outputArray);

        // return prediction
        return outputArray;
    }
}
