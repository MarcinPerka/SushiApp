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
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class RecognizeActivity extends Activity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private ImageView mImageViewSelectImage;

    private CircularProgressButton placeOrderBtn;
    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    // The edit to show status and result.
    private TextView mTextView;

    private VisionServiceClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        placeOrderBtn = findViewById(R.id.placeOrderBtn);
        mImageViewSelectImage = findViewById(R.id.buttonSelectImage);
        mTextView = findViewById(R.id.editTextResult);
        mTextView.setText("");
    }


    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        mTextView.setText("");

        Intent intent;
        intent = new Intent(RecognizeActivity.this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                // If image is selected successfully, set the image URI and bitmap.
                mImageUri = data.getData();

                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                        mImageUri, getContentResolver());
                if (mBitmap != null) {
                    // Show the image on screen.
                    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                    imageView.setImageBitmap(mBitmap);

                    // Add detection log.
                    Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                            + "x" + mBitmap.getHeight());

                    doRecognize();
                }
            }
        }
    }


    public void doRecognize() {
        mImageViewSelectImage.setEnabled(false);
        placeOrderBtn.setEnabled(false);
        mTextView.setText(R.string.ocr_analyze);
        Log.d("Analyze", "Analyzing");
        try {
            new doRequest().execute();
        } catch (Exception e) {
            Log.d("Analyze", "Error encountered. Exception is: " + e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                Log.d("Analyze", "Error: " + e.getMessage());
                mTextView.setText(R.string.ocr_error);
                this.e = null;
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                StringBuilder result = new StringBuilder();
                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            result.append(word.text);
                        }
                    }
                }
                DiscountCode discountCode = DbSession.lookForDiscountCode(result.toString());
                if (discountCode == null) {
                    mTextView.setText(R.string.ocr_no_code);
                } else {
                    mTextView.setText(String.format("Otrzymałeś %s %% zniżki.", discountCode.getDiscount() * 100));
                    MainActivity.order.setDiscountCode(discountCode);
                }
            }
            mImageViewSelectImage.setEnabled(true);
            placeOrderBtn.setEnabled(true);
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
