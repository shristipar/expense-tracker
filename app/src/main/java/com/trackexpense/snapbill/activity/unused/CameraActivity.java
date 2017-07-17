package com.trackexpense.snapbill.activity.unused;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.camera.CameraSource;
import com.trackexpense.snapbill.camera.CameraSourcePreview;
import com.trackexpense.snapbill.camera.GraphicOverlay;
import com.trackexpense.snapbill.camera.OcrDetectorProcessor;
import com.trackexpense.snapbill.camera.OcrGraphic;
import com.trackexpense.snapbill.ui.TextViewWithFont;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends Activity implements DatePickerDialog.OnDateSetListener {

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private CameraSource mCameraSource;

    private GestureDetector gestureDetector;
    private EditText inputAmount;

    @BindView(R.id.bottom_layout) public LinearLayout bottomLayout;
    private EditText currentInputValue;

    private final static String TAG="CAMERA_ACTIVITY";

    public static final List<String> INPUT_NAMES =
            Collections.unmodifiableList(Arrays.asList("MERCHANT NAME", "BILL AMOUNT", "BILL DATE"));
    private Map<String,String> inputNameValueMap;
    private int inputNameIndex = -1;

    private Dialog inputConfirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        inputNameValueMap = new HashMap<>();

        initLayout();
    }

    private void initLayout() {
//        currentInputName = (TextView) findViewById(R.id.current_input_name);
        initCamera();

        nextState();
    }

    private void nextState() {
        ++inputNameIndex;
        if(inputNameIndex>=INPUT_NAMES.size()) {
            openBillReviewActivity();
            return;
        }

        String name=INPUT_NAMES.get(inputNameIndex);
        if("BILL DATE".equals(name)) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                CameraActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getFragmentManager(),"DateTimePicker");
            ((ViewGroup)mPreview.getParent()).removeView(mPreview);
            return;
        }

//        LinearLayout inputLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.input_layout_view,null);
//        TextViewWithFont currentInputName = (TextViewWithFont) inputLayout.findViewById(R.id.current_input_name);

//        currentInputName.setText(name);

//        currentInputValue = (EditText) inputLayout.findViewById(R.id.current_input_value);

//        bottomLayout.addView(inputLayout);
//        currentInputName.setText(INPUT_NAMES.getAllOrderBy(inputNameIndex));
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = "You picked the following date: "+dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        //dateTextView.setText(date);
    }

    private void openBillReviewActivity() {
    }

    private void initCamera() {
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = false;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        boolean b = scaleGestureDetector.onTouchEvent(e);
        boolean b = false;
        boolean c = gestureDetector.onTouchEvent(e);
        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void stopCameraSource() {
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopCameraSource();
    }

    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Log.d(TAG, "text data is being spoken! " + text.getValue());
//                 Speak the string.
//                tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
//                inputMerchantName.setText(text.getValue());
                showDialogInputConfirm(text.getValue());
                return true;
            }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }
        return text != null;
    }

    private void showDialogInputConfirm(final String text) {
        inputConfirmDialog = new Dialog(this,R.style.DialogSlideAnim);
        inputConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputConfirmDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        inputConfirmDialog.setCanceledOnTouchOutside(false);
        inputConfirmDialog.setContentView(R.layout.dialog_input);

        TextView titleTextView = (TextView) inputConfirmDialog.findViewById(R.id.title_textview);
        titleTextView.setText(INPUT_NAMES.get(inputNameIndex));

        final EditText valueEditText = (EditText) inputConfirmDialog.findViewById(R.id.value_textview);
        valueEditText.setText(text);

        ImageButton confirmButton = (ImageButton) inputConfirmDialog.findViewById(R.id.confirm_button);
        ImageButton clearButton = (ImageButton) inputConfirmDialog.findViewById(R.id.clear_button);
//        ImageButton editButton = (ImageButton) inputConfirmDialog.findViewById(R.id.edit_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialogInputConfirm();

                String value = valueEditText.getText().toString();
                inputNameValueMap.put(INPUT_NAMES.get(inputNameIndex),value);
                currentInputValue.setText(value);

                nextState();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialogInputConfirm();
            }
        });
        /*editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopCameraSource();

                valueEditText.setEnabled(true);
                valueEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(valueEditText, InputMethodManager.SHOW_IMPLICIT);
                valueEditText.setSelection(valueEditText.getText().length());
            }
        });*/

        stopCameraSource();
        inputConfirmDialog.show();
    }

    private void hideDialogInputConfirm() {
        inputConfirmDialog.dismiss();
        startCameraSource();
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }
}
