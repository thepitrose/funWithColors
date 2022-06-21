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
        myColor.clear();    //Cleans the hashMap so the colors will not accumulate from the frames before

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                imageColor(bitmap);

                //geting the top 5 variables from the hashmap
                int colorSise =  myColor.size();
                keys = myColor.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(5).map(Map.Entry::getKey).collect(Collectors.toList());

                if (keys.size()==5) {
                    firstBtn.setText(keys.get(0));
                    secondBtn.setText(keys.get(1));
                    thirdBtn.setText(keys.get(2));
                    fourthBtn.setText(keys.get(3));
                    fifthBtn.setText(keys.get(4));


                    float percentage = ((float) myColor.get(keys.get(0))/ myColor.size());
                    String str = String.format("%2.02f", percentage);

                    /*
                    In case the camera fails to read the colors correctly
                    For example when pushing something in front of her, the percentage calculation is incorrect
                    Therefore the user gets an error
                     */
                    if(percentage>100) {
                        firstText.setText("Error");
                        secondText.setText("Error");
                        thirdText.setText("Error");
                        fourthText.setText("Error");
                        fifthText.setText("Error");
                    }
                    else {
                        firstText.setText(str + "%");

                        percentage = ((float) myColor.get(keys.get(1)) / myColor.size());
                        str = String.format("%2.02f", percentage);

                        secondText.setText(str + "%");

                        percentage = ((float) myColor.get(keys.get(2)) / myColor.size());
                        str = String.format("%2.02f", percentage);

                        thirdText.setText(str + "%");


                        percentage = ((float) myColor.get(keys.get(3)) / myColor.size());
                        str = String.format("%2.02f", percentage);

                        fourthText.setText(str + "%");


                        percentage = ((float) myColor.get(keys.get(4)) / myColor.size());
                        str = String.format("%2.02f", percentage);

                        fifthText.setText(str + "%");
                    }
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

            /*
            I rounded the width and length of the image
            Because on different devices the previewView size is different, which can cause an error with the loop
             */

            int bmpHeight = bmpOriginal.getHeight();
            bmpHeight = bmpHeight/10;
            bmpHeight=bmpHeight*10;

            int bmpWidth = bmpOriginal.getWidth();
            bmpWidth = bmpWidth/10;
            bmpWidth=bmpWidth*10;

            for (int y = 0; y < bmpHeight; y++) {
                for (int x = 0; x < bmpWidth; x+=3) {

                    /*
                    To speed up running times,I came from the assumption that in a group of pixels that are next to each other, thay most likely have the color ,
                    so the jumps in the loop are 3, so im sample only a third of the pixels.
                    I tested my theory , from 3327 colors in a full loop,
                    to 2750 colors with jumps, it's 18% decrease in the amount of colors, but the improvement in performance was significant.
                    Even on old instruments like the xiaomi note 4x, Response time was reasonable.
                     */

                    /*
                    adding to the hashmap
                    I adding mode 3 of Y to X, To get a chessboard layout style
                     */
                    colorData tempcolorData = groupPixelColor(bmpOriginal,x+y%3,y);


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


    private colorData groupPixelColor(Bitmap bmpOriginal , int x , int y){

        int r=0;
        int g=0;
        int b=0;

        int pix = bmpOriginal.getPixel(x,y);

        r += Color.red(pix);
        g +=  Color.green(pix);
        b += Color.blue(pix);

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