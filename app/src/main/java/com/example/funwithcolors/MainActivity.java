package com.example.funwithcolors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;


    private static final int CAMERA_REQUEST = 100;                       //For camera permissions popup
    public  HashMap<String, Integer> myColor  = new HashMap<String, Integer>();        //To store all colors on the screen, I selected hashMap , Because to find and/or add a color, the speed will be O(n)
    public List<String> keys;                                           //,help withdraw the top 5 colors


    PreviewView previewView;

    private Button firstBtn,secondBtn,thirdBtn,fourthBtn,fifthBtn; //I use buttons to represent the colors, the thought of adding filters for these colors, from the thought that in the future I will added the option of filters
    private TextView firstText,secondText,thirdText,fourthText,fifthText;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);

        firstBtn = findViewById(R.id.firstBtn);
        secondBtn = findViewById(R.id.secondBtn);
        thirdBtn = findViewById(R.id.thirdBtn);
        fourthBtn = findViewById(R.id.fourthBtn);
        fifthBtn = findViewById(R.id.fifthBtn);

        firstText = findViewById(R.id.firstText);
        secondText = findViewById(R.id.secondText);
        thirdText = findViewById(R.id.thirdText);
        fourthText = findViewById(R.id.fourthText);
        fifthText = findViewById(R.id.fifthText);


        // camera permissions popup
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(this::run, getExecutor());


    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    /*
        I have never developed an app with the use of a camera
        so use as a base, this code
        https://github.com/Faisal-FS/CameraX-In-Java

        Just for activating the camera on the screen,
        because I saw these are pretty travel functions, I left them as they are
     */

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview,imageAnalysis);
        //imageColor(preview);
    }
    //I selected the CameraX because according to the Android site - ameraX resolves device compatibility issues for you so that you don't have to add device-specific code to your app.

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void analyze(@NonNull ImageProxy image) {


        // the displayed on the screen is converted to a bitmap, for simpler work
        Bitmap bitmap = previewView.getBitmap();
        image.close();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                myColor.clear();    //Cleans the hashMap so the colors will not accumulate from the frames before
                imageColor(bitmap);

                //geting the top 5 variables from the hashmap
                keys = myColor.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(5).map(Map.Entry::getKey).collect(Collectors.toList());


                if (keys.size()==5) {
                    firstBtn.setText(keys.get(0));
                    secondBtn.setText(keys.get(1));
                    thirdBtn.setText(keys.get(2));
                    fourthBtn.setText(keys.get(3));
                    fifthBtn.setText(keys.get(4));



                    float percentage = ((float) myColor.get(keys.get(0))/ myColor.size()*100);
                    String str = String.format("%2.02f", percentage);


                    firstText.setText(str+"%");

                    percentage = ((float) myColor.get(keys.get(1))/ myColor.size() *100);
                     str = String.format("%2.02f", percentage);

                    secondText.setText(str+"%");

                    percentage = ((float) myColor.get(keys.get(2))/ myColor.size() *100);
                    str = String.format("%2.02f", percentage);

                    thirdText.setText(str+"%");


                    percentage = ((float) myColor.get(keys.get(3))/ myColor.size()*100);
                    str = String.format("%2.02f", percentage);

                    fourthText.setText(str+"%");


                    percentage = ((float) myColor.get(keys.get(4))/ myColor.size()*100);
                    str = String.format("%2.02f", percentage);

                    fifthText.setText(str+"%");

                }

            }
        });

    }


    private void run() {
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            startCameraX(cameraProvider);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void imageColor(Bitmap bmpOriginal) {
        if(bmpOriginal!=null) {
            for (int y = 0; y < bmpOriginal.getHeight(); y=y+10) {
                for (int x = 0; x < bmpOriginal.getWidth(); x=x+10) {

                    /*
                    To speed up running times,I came from the assumption that a group of pixels next to each other, have the color , so the jumps in the loop are 10.
                    I tested my theory , from 3600 colors in a full loop,
                    to 3400 colors with jumps, it's 5% decrease in the amount of colors, but 100 times faster
                     */



                    //adding to the hashmap
                    colorData tempcolorData = groupPixelColor(bmpOriginal,x,y);

                    if (myColor.get(tempcolorData.getName())!=null) {

                        myColor.put(tempcolorData.getName(), myColor.get(tempcolorData.getName()) + 1);

                    }
                    else {
                        myColor.put(tempcolorData.getName(),1);

                    }
                }
            }

        }

    }

    /*
    In Here I use a similar assumption,Running on every 100 pixels,
    versus running on only the 10 centers,Reduced the running time, and the decrease in the amount of colors was not significant, less than 5 percent
     */
    private colorData groupPixelColor(Bitmap bmpOriginal , int x , int y){

        int r=0;
        int g=0;
        int b=0;


        for(int i=0 ; i<10 ; i++) {

            int pix = bmpOriginal.getPixel(x+i,y+i);
            r += Color.red(pix);
            g += Color.green(pix);
            b += Color.blue(pix);
        }

        r = r/10;
        g = g/10;
        b = b/10;

        r=roundMyNumber(r);
        g=roundMyNumber(g);
        b=roundMyNumber(b);



        /*
        In order not to get similar colors, because let's admit it, we will not see the difference between
        255,255,255 to 250,255,255
        so the color range will be wider
        */
        colorData returncolorData = new colorData(r,g,b);

        return returncolorData;

    }

    private int roundMyNumber(int num) {


        if(num%5==1) {
            num--;
        }
        else if(num%5==2) {
            num-=2;
        }
        else if(num%5==3) {
            num-=3;
        }
        else if(num%5==4) {
            num-=4;
        }

        return num;


    }

}