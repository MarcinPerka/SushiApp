//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Vision-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.archu.sushiapp.OCR;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.archu.sushiapp.Db.DbSession;
import com.archu.sushiapp.model.DiscountCode;
import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperation;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperationResult;
import com.microsoft.projectoxford.vision.contract.HandwritingTextLine;
import com.microsoft.projectoxford.vision.contract.HandwritingTextWord;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class HandwritingRecognizeActivity extends Activity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private ImageView imageViewSelectImage;

    private CircularProgressButton placeOrderBtn;
    // The URI of the image selected to detect.
    private Uri imagUrl;

    // The image selected to detect.
    private Bitmap bitmap;

    // The edit to show status and result.
    private TextView textView;

    private VisionServiceClient client;

    //max retry times to get operation result
    private int retryCountThreshold = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_handwriting);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        placeOrderBtn = findViewById(R.id.placeOrderBtn);
        imageViewSelectImage = findViewById(R.id.buttonSelectImage);
        textView = findViewById(R.id.editTextResult);
        textView.setText("");
    }


    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        textView.setText("");

        Intent intent;
        intent = new Intent(HandwritingRecognizeActivity.this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                // If image is selected successfully, set the image URI and bitmap.
                imagUrl = data.getData();

                bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                        imagUrl, getContentResolver());
                if (bitmap != null) {
                    // Show the image on screen.
                    ImageView imageView = findViewById(R.id.selectedImage);
                    imageView.setImageBitmap(bitmap);

                    // Add detection log.
                    Log.d("AnalyzeActivity", "Image: " + imagUrl + " resized to " + bitmap.getWidth()
                            + "x" + bitmap.getHeight());

                    doRecognize();
                }
            }
        }
    }


    public void doRecognize() {
        imageViewSelectImage.setEnabled(false);
        placeOrderBtn.setEnabled(false);
        Log.d("Analyze", "Analyzing");
        textView.setText(R.string.ocr_analyze);
        try {
            new doRequest(this).execute();
        } catch (Exception e) {
            Log.d("Analyze", "Error encountered. Exception is: " + e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException, InterruptedException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray())) {
                //post image and got operation from API
                HandwritingRecognitionOperation operation = this.client.createHandwritingRecognitionOperationAsync(inputStream);

                HandwritingRecognitionOperationResult operationResult;
                //try to get recognition result until it finished.

                int retryCount = 0;
                do {
                    if (retryCount > retryCountThreshold) {
                        throw new InterruptedException("Can't get result after retry in time.");
                    }
                    Thread.sleep(1000);
                    operationResult = this.client.getHandwritingRecognitionOperationResultAsync(operation.Url());
                }
                while (operationResult.getStatus().equals("NotStarted") || operationResult.getStatus().equals("Running"));

                String result = gson.toJson(operationResult);
                Log.d("result", result);
                return result;

            }
        }

    }


    private static class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        private WeakReference<HandwritingRecognizeActivity> recognitionActivity;

        public doRequest(HandwritingRecognizeActivity activity) {
            recognitionActivity = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                if (recognitionActivity.get() != null) {
                    return recognitionActivity.get().process();
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (recognitionActivity.get() == null) {
                return;
            }
            // Display based on error existence
            if (e != null) {
                Log.d("Analyze", "Error: " + e.getMessage());
                recognitionActivity.get().textView.setText(R.string.ocr_error);
                this.e = null;
            } else {
                Gson gson = new Gson();
                HandwritingRecognitionOperationResult r = gson.fromJson(data, HandwritingRecognitionOperationResult.class);

                StringBuilder resultBuilder = new StringBuilder();
                //if recognition result status is failed. display failed
                if (r.getStatus().equals("Failed")) {
                    recognitionActivity.get().textView.setText(R.string.ocr_error);
                    Log.d("Analyze", "Error: Recognition Failed");
                } else {
                    for (HandwritingTextLine line : r.getRecognitionResult().getLines()) {
                        for (HandwritingTextWord word : line.getWords()) {
                            resultBuilder.append(word.getText());
                        }
                    }
                }
                DiscountCode discountCode = DbSession.lookForDiscountCode(resultBuilder.toString());
                if (discountCode == null) {
                    recognitionActivity.get().textView.setText(R.string.ocr_no_code);
                } else {
                    recognitionActivity.get().textView.setText(String.format("Otrzymałeś %s %% zniżki.", discountCode.getDiscount() * 100));
                    MainActivity.order.setDiscountCode(discountCode);
                }
            }
            recognitionActivity.get().placeOrderBtn.setEnabled(true);
            recognitionActivity.get().imageViewSelectImage.setEnabled(true);
        }
    }

    public void shoppingCart(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", 1);
        startActivity(intent);
    }

    public void menu(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", 2);
        startActivity(intent);
    }

    public void order(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", 3);
        startActivity(intent);
    }

    public void formFragment(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", 4);
        startActivity(intent);
    }
}
