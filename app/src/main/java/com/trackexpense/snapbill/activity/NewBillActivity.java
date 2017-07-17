package com.trackexpense.snapbill.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.trackexpense.snapbill.Config;
import com.trackexpense.snapbill.R;
import com.trackexpense.snapbill.adapter.CategoryRecyclerViewAdapter;
import com.trackexpense.snapbill.adapter.CurrencyRecyclerViewAdapter;
import com.trackexpense.snapbill.adapter.RecyclerViewAdapter;
import com.trackexpense.snapbill.cache.CurrencyCache;
import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.model.BillAction;
import com.trackexpense.snapbill.ui.DatePickerHack;
import com.trackexpense.snapbill.ui.ImageViewZoomDetectTextOverlay;
import com.trackexpense.snapbill.utils.Category;
import com.trackexpense.snapbill.utils.Constants;
import com.trackexpense.snapbill.utils.GUIUtils;
import com.trackexpense.snapbill.utils.Res;
import com.trackexpense.snapbill.utils.Utils;
import com.trackexpense.snapbill.utils.Validation;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.trackexpense.snapbill.activity.ReviewBillActivity.EXTRA_BILL_ACTION;
import static com.trackexpense.snapbill.activity.ReviewBillActivity.EXTRA_BILL_PARCEL;
import static com.trackexpense.snapbill.utils.Utils.getInputDateFormat;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class NewBillActivity extends AppCompatActivity {

    private static final int TAKE_IMAGE_FROM_GALLERY = 1;
    private static final int TAKE_IMAGE_FROM_CAMERA = 2;

    private static final String TAG = "NewBillActivity";

    @BindView(R.id.top_instruction_text_view)
    protected TextView topInstructionTextView;

    @BindView(R.id.bottom_layout)
    protected ViewGroup bottomLayout;

    @BindView(R.id.merchant_name_linear_layout)
    protected LinearLayout merchantNameLinearLayout;
    @BindView(R.id.bill_amount_linear_layout)
    protected LinearLayout billAmountLinearLayout;

    @BindView(R.id.merchant_name_edit_text)
    protected EditText merchantNameEditText;
    @BindView(R.id.bill_amount_edit_text)
    protected EditText billAmountEditText;
    @BindView(R.id.date_edit_text)
    protected EditText dateEditText;

    @BindView(R.id.image_view_zoom_overlay)
    protected ImageViewZoomDetectTextOverlay imageViewWithZoom;

    @BindViews({R.id.camera_button_parent_layout, R.id.gallery_button_parent_layout})
    protected List<LinearLayout> buttonParentLayouts;

    @BindView(R.id.top_instruction_progress)
    protected ProgressBar progressBar;

    @BindView(R.id.view_animator)
    protected ViewAnimator viewAnimator;

    private Animation slideInFromRight;
    private Animation slideInFromRightOvershoot;
    private Animation slideOutToLeft;
    private Animation slideOutToLeftOvershoot;
    private Animation slideInFromLeft;
    private Animation slideInFromLeftOvershoot;
    private Animation slideOutToRight;
    private Animation slideOutToRightOvershoot;

    private TextRecognizer textRecognizer;
    private SparseArray<TextBlock> detectedTextBlocks;
    private Dialog inputConfirmDialog;

    private EditText currentEditText;

    private String mCurrentPhotoPath;

    State state;

    @BindView(R.id.date_picker)
    DatePickerHack datePickerHack;

    @BindView(R.id.bill_recycler_view)
    RecyclerView categoryRecyclerView;

    @BindView(R.id.input_currency)
    protected TextView inputCurrency;

    private NewBillActivity instance;

    Double amountDouble = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_bill);
        ButterKnife.bind(this);

        instance = this;

        initLayout();
        initState();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                initTextRecognizer();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCapturedImage();
    }

    private void clearCapturedImage() {
        if (mCurrentPhotoPath != null) {
            File file = new File(mCurrentPhotoPath);
            file.delete();
        }
    }

    private void initLayout() {
//        bottomLayout.setVisibility(View.GONE);
        initDatePicker();
        initRecyclerView();
        initCurrency();
    }

    private void initCurrency() {
        inputCurrency.setText(CurrencyCache.getSelectedCurrency().getSymbol());
    }

    private void initDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        dateEditText.setText(getInputDateFormat(calendar));
        datePickerHack.setOnDateSelectedListener(new DatePickerHack.OnDateSelectedListener() {
            @Override
            public void onDateSelected(DatePicker datePicker, int year, int month, int dayOfMonth) {
                if (state == State.DATE) {
                    Calendar datePickerCalendar = new GregorianCalendar(year, month, dayOfMonth);
                    dateEditText.setText(getInputDateFormat(datePickerCalendar));
                }
            }
        });
    }

    private void initRecyclerView() {
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),
//                CategoryRecyclerViewAdapter.NO_OF_COLUMNS);
//        billRecyclerView.setLayoutManager(gridLayoutManager);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        categoryRecyclerView.setHasFixedSize(true);

        CategoryRecyclerViewAdapter adapter = new CategoryRecyclerViewAdapter(getApplicationContext());
        adapter.setOnItemSelectedListener(new RecyclerViewAdapter.OnItemSelectedListener<Category>() {
            @Override
            public void selected(Category category, int index) {
                if (state == State.CATEGORY) {
                    categoryEditText.setText(category.name());
                }
            }

            @Override
            public void hold(Category holdItem, int index) {

            }
        });
        categoryRecyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.input_currency)
    protected void OnCurrencyClick() {
        final Dialog currencyDialog;
        currencyDialog = new Dialog(NewBillActivity.this);
        currencyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currencyDialog.setContentView(R.layout.dialog_currency);

        CurrencyRecyclerViewAdapter adapter = new CurrencyRecyclerViewAdapter(getApplicationContext());
        adapter.setOnItemSelectedListener(new RecyclerViewAdapter.OnItemSelectedListener<Currency>() {
            @Override
            public void selected(Currency currency, int index) {
                CurrencyCache.setSelectedCurrency(currency);
                inputCurrency.setText(currency.getSymbol());
                if (amountDouble != null) {
                    billAmountEditText.setText(CurrencyCache.format(CurrencyCache.getSelectedCurrency(), amountDouble));
                }
                currencyDialog.dismiss();
            }

            @Override
            public void hold(Currency holdItem, int index) {

            }
        });

        RecyclerView currencyRecyclerView = (RecyclerView) currencyDialog.findViewById(R.id.recycler_view);
        currencyRecyclerView.setHasFixedSize(true);
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        currencyRecyclerView.setAdapter(adapter);

        TextView titleTextView = (TextView) currencyDialog.findViewById(R.id.title_textview);
        titleTextView.setText("Currency");

        currencyDialog.show();
    }

    protected void initTextRecognizer() {
        textRecognizer = new TextRecognizer.Builder(this).build();

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Low Storage");
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @OnClick({R.id.picture_from_camera_button, R.id.picture_from_gallery_button})
    protected void onButtonClick(ImageButton button) {
        switch (button.getId()) {
            case R.id.picture_from_camera_button:
                takePictureFromCamera();
                break;
            case R.id.picture_from_gallery_button:
                takePictureFromGallery();
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePictureFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Config.showToastMessage(this, "Not enough space!");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, TAKE_IMAGE_FROM_CAMERA);
            }
        } else {
            Config.showToastMessage(this, "Sorry, no camera app found!");
        }
    }

    @BindView(R.id.picture_from_gallery_button)
    protected ImageButton pictureFromGalleryButton;

    protected void takePictureFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, TAKE_IMAGE_FROM_GALLERY);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (GUIUtils.isXYWithinView(ev.getX(), ev.getY(), imageViewWithZoom) && isDetectingText()) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isDetectingText() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_IMAGE_FROM_GALLERY:
                    Uri selectedImageUri = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        setUpImageView(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Config.showToastMessage(getApplicationContext(), "Error in getting picture");
                    }
                    break;
                case TAKE_IMAGE_FROM_CAMERA:
                    // Get the dimensions of the View
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);

                    int targetW = metrics.widthPixels;
                    int targetH = metrics.heightPixels;

                    // Get the dimensions of the bitmap
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                    int photoW = bmOptions.outWidth;
                    int photoH = bmOptions.outHeight;

                    // Determine how much to scale down the image
                    int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                    // Decode the image file into a Bitmap sized to fill the View
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;

                    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                    setUpImageView(bitmap);

//                    Intent intent = new Intent(getApplicationContext(),PictureActivity.class);
//                    Log.e(TAG,selectedImageUri.toString());
//                    intent.putExtra(PictureActivity.DATA_IMAGE_PATH,selectedImageUri.toString());
//                    startActivity(intent);
            }
        }
    }

    @BindView(R.id.top_view_animtor)
    ViewAnimator topViewAnimator;

    private void setUpImageView(Bitmap bitmap) {

        imageViewWithZoom.setOnImageViewUpdateListener(new ImageViewZoomDetectTextOverlay.OnImageViewUpdatedListener() {
            @Override
            public void onUpdate(Bitmap updatedBitmap) {
                getTextBlocksFromBitmap(updatedBitmap);
            }

            @Override
            public void onTouch(Point imageViewPoint) {
                if (!isDetectingText())
                    checkTextBlock(imageViewPoint);
            }
        });
        imageViewWithZoom.setImageBitmap(bitmap);

        nextState();
    }

    private void getTextBlocksFromBitmap(Bitmap bitmap) {
        new DetectTextAsync(bitmap).execute();
    }

    private void startProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        topInstructionTextView.setText(getResources().getString(R.string.newbill_instruction_2_loading));
    }

    private void stopProgress() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        topInstructionTextView.setText(getResources().getString(R.string.newbill_instruction_2));
    }

    private class DetectTextAsync extends AsyncTask<Void, Integer, SparseArray<TextBlock>> {
        Bitmap bitmap;

        public DetectTextAsync(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            startProgress();
        }

        @Override
        protected SparseArray<TextBlock> doInBackground(Void... voids) {
            Frame imageFrame = new Frame.Builder()
                    .setBitmap(bitmap)
                    .build();

            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
            return textBlocks;
        }

        @Override
        protected void onPostExecute(SparseArray<TextBlock> textBlockSparseArray) {
            super.onPostExecute(textBlockSparseArray);

            detectedTextBlocks = textBlockSparseArray;
            Log.e(TAG, "Detected Text Blocks count: " + detectedTextBlocks.size());

            imageViewWithZoom.setTextBlocks(textBlockSparseArray);
            stopProgress();
        }
    }

    private void checkTextBlock(Point imageViewPoint) {
        if (detectedTextBlocks != null) {
            for (int i = 0; i < detectedTextBlocks.size(); ++i) {
                TextBlock textBlock = detectedTextBlocks.valueAt(i);
                if (textBlock.getBoundingBox().contains(imageViewPoint.x, imageViewPoint.y)) {
                    List<? extends Text> texts = textBlock.getComponents();
                    for (Text text : texts) {
                        if (text.getBoundingBox().contains(imageViewPoint.x, imageViewPoint.y)) {
//                            Config.showToastMessage(this, text.getValue());
                            showDialogInputConfirm(text.getValue(), "You tapped on...");
                        }
                    }

                    Log.e(TAG, "TextBlock: " + textBlock.getValue());
                    return;
                }
            }
        }
    }

    private void showDialogInputConfirm(final String text, final String title) {
        inputConfirmDialog = new Dialog(this, R.style.DialogSlideAnim);
        inputConfirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputConfirmDialog.setCanceledOnTouchOutside(false);
        inputConfirmDialog.setContentView(R.layout.dialog_input);

        GUIUtils.roundedCornerDialog(inputConfirmDialog);

        TextView titleTextView = GUIUtils.getView(inputConfirmDialog, R.id.title_textview);
        titleTextView.setText(title);

        final EditText valueEditText = (EditText) inputConfirmDialog.findViewById(R.id.value_textview);
        switch (state) {
            case MERCHANT:
                valueEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case AMOUNT:
                valueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            default:
                return;
        }
        valueEditText.setText(text);

        ImageButton confirmButton = GUIUtils.getView(inputConfirmDialog, R.id.confirm_button);
        ImageButton clearButton = GUIUtils.getView(inputConfirmDialog, R.id.clear_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = valueEditText.getText().toString().trim();

                switch (state) {
                    case MERCHANT:
                        currentEditText.setText(value);
                        break;
                    case AMOUNT:
                        value = Utils.sanitizeAmount(value);
                        if (!Validation.isNumber(value)) {
                            GUIUtils.showSnackBar(valueEditText, "Amount should be a number");
                            return;
                        }

                        amountDouble = Double.parseDouble(value);
                        currentEditText.setText(CurrencyCache.format(
                                CurrencyCache.getSelectedCurrency(), value));
                        break;
                    default:
                        return;
                }

                GUIUtils.hideKeyboard(instance, valueEditText);
                hideDialogInputConfirm();
                nextState();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialogInputConfirm();
            }
        });
    }

    @OnClick({R.id.merchant_name_edit_text, R.id.bill_amount_edit_text})
    protected void onInputClick() {
        String input;
        if (state == State.MERCHANT) {
            input = currentEditText.getText().toString();
        } else {
            input = amountDouble == null ? "" : amountDouble.toString();
        }

        showDialogInputConfirm(input, state.name());
    }

    private void hideDialogInputConfirm() {
        inputConfirmDialog.dismiss();
    }

    @BindView(R.id.left_image_button)
    ImageButton leftImageButton;

    @BindView(R.id.right_image_button)
    ImageButton rightImageButton;

    private void initState() {
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

        slideInFromRight = Res.getAnimFromCache(R.anim.slide_in_from_right);
        slideInFromRight.setInterpolator(decelerateInterpolator);
        slideInFromRightOvershoot = slideInFromRight;

        slideOutToLeft = Res.getAnimFromCache(R.anim.slide_out_to_left);
        slideOutToLeft.setInterpolator(decelerateInterpolator);
        slideOutToLeftOvershoot = slideOutToLeft;

        slideInFromLeft = Res.getAnimFromCache(R.anim.slide_in_from_left);
        slideInFromLeft.setInterpolator(decelerateInterpolator);
        slideInFromLeftOvershoot = slideInFromLeft;

        slideOutToRight = Res.getAnimFromCache(R.anim.slide_out_to_right);
        slideOutToRight.setInterpolator(decelerateInterpolator);
        slideOutToRightOvershoot = slideOutToRight;

        state = State.GET_IMAGE;
        setupState();
    }

    private void setupState() {
        if (state.isAfter(State.GET_IMAGE)) {
            leftImageButton.setVisibility(View.VISIBLE);
            rightImageButton.setVisibility(View.VISIBLE);
//            bottomLayout.setVisibility(View.VISIBLE);
//            typeIt.setVisibility(View.GONE);
        } else {
            leftImageButton.setVisibility(View.GONE);
            rightImageButton.setVisibility(View.GONE);
//            bottomLayout.setVisibility(View.GONE);
//            typeIt.setVisibility(View.VISIBLE);
        }

        /*if (state == State.MERCHANT) {
            leftImageButton.setVisibility(View.INVISIBLE);
        } else {
            leftImageButton.setVisibility(View.VISIBLE);
        }*/

        switch (state) {
            case GET_IMAGE:
                topInstructionTextView.setText(getString(R.string.newbill_instruction_1));
                break;
            case MERCHANT:
                currentEditText = merchantNameEditText;
                topInstructionTextView.setText(getString(R.string.newbill_instruction_2));
                break;
            case AMOUNT:
                currentEditText = billAmountEditText;
                topInstructionTextView.setText(getString(R.string.newbill_instruction_2));
                break;
            case DATE:
                currentEditText = dateEditText;
                topInstructionTextView.setText(getString(R.string.newbill_instruction_3));
                break;
            case CATEGORY:
                currentEditText = categoryEditText;
                topInstructionTextView.setText(getString(R.string.newbill_instruction_4));
        }
    }

    private void prevState() {
        topViewAnimator.setOutAnimation(slideOutToRight);
        topViewAnimator.setInAnimation(slideInFromLeft);
        viewAnimator.setInAnimation(slideInFromLeftOvershoot);
        viewAnimator.setOutAnimation(slideOutToRightOvershoot);

        switch (state) {
            case MERCHANT:
                topViewAnimator.showPrevious();
                viewAnimator.showPrevious();
                break;
            case AMOUNT:
                viewAnimator.showPrevious();
                break;
            case DATE:
                viewAnimator.showPrevious();
                topViewAnimator.showPrevious();
                break;
            case CATEGORY:
                viewAnimator.showPrevious();
                topViewAnimator.showPrevious();
        }

        state = state.prev();
        setupState();
    }

    @BindView(R.id.enter_manually)
    protected LinearLayout typeIt;

    @BindView(R.id.category_edit_text)
    protected EditText categoryEditText;

    private void nextState() {
        topViewAnimator.setOutAnimation(slideOutToLeft);
        topViewAnimator.setInAnimation(slideInFromRight);
        viewAnimator.setInAnimation(slideInFromRightOvershoot);
        viewAnimator.setOutAnimation(slideOutToLeftOvershoot);

        switch (state) {
            case GET_IMAGE:
                topViewAnimator.showNext();
                viewAnimator.showNext();
                break;
            case MERCHANT:
                viewAnimator.showNext();
                break;
            case AMOUNT:
                viewAnimator.showNext();
                topViewAnimator.showNext();
                break;
            case DATE:
                viewAnimator.showNext();
                topViewAnimator.showNext();
                break;
            case CATEGORY:
                gotoBillReviewActivity();
        }

        state = state.next();
        setupState();
    }

    private void gotoBillReviewActivity() {
        Bill bill = new Bill();
        String merchant = GUIUtils.getText(merchantNameEditText);
        String amount = GUIUtils.getText(billAmountEditText);

        bill.setCurrency(CurrencyCache.getSelectedCurrency().getCurrencyCode());
        String category = GUIUtils.getText(categoryEditText);

        if (!Validation.isEmpty(merchant)) {
            bill.setMerchant(merchant);
        }
        if (!Validation.isEmpty(amount)) {
            bill.setAmount(amountDouble);
        }
        bill.setDate(getSelectedDate());
        if (!Validation.isEmpty(category)) {
            bill.setCategory(Category.valueOf(category));
        }

        startBillReviewActivity("Review", bill);
    }

    @OnClick(R.id.enter_manually)
    protected void onClickTypeItOut() {
        startBillReviewActivity("New", null);
    }

    private void startBillReviewActivity(String action, Bill bill) {
        Intent intent = new Intent(getApplicationContext(), ReviewBillActivity.class);
        if (!Utils.isNull(bill)) {
            intent.putExtra(EXTRA_BILL_PARCEL, Parcels.wrap(bill));
            intent.putExtra(EXTRA_BILL_ACTION, BillAction.ADD);
        }
        startActivity(intent);
        finish();
    }

    private Date getSelectedDate() {
        final Calendar c = Calendar.getInstance();
        c.set(datePickerHack.getYear(), datePickerHack.getMonth(), datePickerHack.getDayOfMonth());
        return c.getTime();
    }

    @OnClick({R.id.left_image_button, R.id.right_image_button})
    protected void onArrowButtonClick(ImageButton view) {
        switch (view.getId()) {
            case R.id.left_image_button:
                prevState();
                break;
            case R.id.right_image_button:
                nextState();
        }
    }

    @Override
    public void onBackPressed() {
        if (state != State.GET_IMAGE) {
            prevState();
        } else {
            super.onBackPressed();
        }
    }

    private enum State {
        GET_IMAGE() {
            @Override
            public State prev() {
                return GET_IMAGE;
            }
        },
        MERCHANT(),
        AMOUNT(),
        DATE(),
        CATEGORY {
            @Override
            public State next() {
                return CATEGORY; // see below for options for this line
            }
        };

        public State prev() {
            return values()[ordinal() - 1];
        }

        public State next() {
            return values()[ordinal() + 1];
        }

        public boolean isAfter(State state) {
            return this.ordinal() > state.ordinal();
        }
    }


}
