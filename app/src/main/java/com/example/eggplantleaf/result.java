package com.example.eggplantleaf;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.eggplantleaf.ml.Eggplant;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Date;
public class result extends AppCompatActivity{


    ImageView imageview2;

    AppCompatButton exit,recapture2;
    TextView result, confidence, treatment, catg;
    int imageSize = 224;
    private Bitmap imagesToStore;
    String cercospora, flea_beetle;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        linearLayout = findViewById(R.id.recom);
        int cercos = getResources().getIdentifier("Cercospora","string",getPackageName());
        cercospora = getString(cercos);
        flea_beetle = getResources().getString(R.string.Flea_beetle);
        treatment = findViewById(R.id.treatrec);
        result = findViewById(R.id.result);
        imageview2 = (ImageView) findViewById(R.id.imageview2);
        confidence = findViewById(R.id.confidence);
        recapture2 = (AppCompatButton) findViewById(R.id.recapture2);
        exit = (AppCompatButton) findViewById(R.id.exit);
        catg = findViewById(R.id.catg);


        if (MainActivity.counter == 1) {
            accessCamera();
        } else {
            accessGallery();
        }

        recapture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(result.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        });

        /*
         exit.setOnClickListener(new View.OnClickListener() {
           @Override
          public void onClick(View v) {

               System.exit(0);
            }
         });

         */

    }
    public void Exit(View view){
        exit(this);
    }
    public static void exit(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to EXIT app ?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                activity.finishAffinity();
                System.exit(0);

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void accessGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    private void accessCamera() {
        Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCamera, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageview2.setImageBitmap(photo);
            photo = Bitmap.createScaledBitmap(photo, imageSize, imageSize, false);
            classifyImage(photo);
            store_Image(photo);

            //classifyImage(photo);
        } else if (requestCode == 101) {
            Uri dat = data.getData();
            try {

                imagesToStore = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                imageview2.setImageBitmap(imagesToStore);
                imagesToStore = Bitmap.createScaledBitmap(imagesToStore, imageSize, imageSize, false);
                classifyImage(imagesToStore);
              //  store_Image(imagesToStore);

                //classifyImage(imagesToStore);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private int classifyImage(Bitmap image) {
        try {
            Eggplant model = Eggplant.newInstance(getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; ++i) {
                for (int j = 0; j < imageSize; ++j) {
                    final int val = intValues[pixel++];

                    int IMAGE_MEAN = 0;
                    float IMAGE_STD = 255.0f;
                    byteBuffer.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    byteBuffer.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    byteBuffer.putFloat((((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);


                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Eggplant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; ++i) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Cercospora", "Flea Beetle", "Undefined"};
            String Cercospora = classes[0];
            String FleaBeetle = classes[1];
            String Undefined = classes[2];


            if (classes[maxPos].equals(Cercospora)) {
                result.setText("Cercospora");
                treatment.setText(cercospora);
            } else if (classes[maxPos].equals(FleaBeetle)) {
                result.setText("Flea Beetle");
                treatment.setText(flea_beetle);
            } else if (classes[maxPos].equals(Undefined)) {
                result.setText("Undefined");
                linearLayout.setVisibility(View.INVISIBLE);
            } else {
            }
            if (result.getText().toString().equals("Cercospora")) {
                catg.setText("Disease");

            } else if (result.getText().toString().equals("Flea Beetle"))
            {
                catg.setText("Infestation");
            }else { catg.setText("");}

            String s = "";
            for (int i = 0; i < classes.length; i++) {
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }
            confidence.setText(s);


            // Releases model resources if no longer used.
            // String CONFIDENCE_LEVEL = String.format("%.2f", maxConfidence * 100);
            //  txt_predict.setText(CONFIDENCE_LEVEL+"%");

            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }//---------------------------------------
        return 0;
    }


    public static int getCameraPhotoOrientation(ImageView context, String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private void store_Image(Bitmap image) {
        String image_name = result.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String timeStamp = dateFormat.format(new Date());
        ;


    }
}
