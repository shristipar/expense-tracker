package com.trackexpense.snapbill.activity.unused;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.trackexpense.snapbill.Config;
import com.trackexpense.snapbill.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddBillActivity extends AppCompatActivity {

    private static final String TAG = "AddBillActivity";
    private static final String TITLE = "Add BillRecord";
    private static final int PICK_IMAGE_FROM_GALLERY = 123;

    @BindView(R.id.view_animator)
    public View bottomSheet;
    @BindView(R.id.close_bottom_sheet)
    public ImageButton closeBottomSheet;

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);
        setTitle(TITLE);

        ButterKnife.bind(this);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @OnClick(R.id.merchant_name_camera_image_button)
    public void OnClick(ImageButton button) {
        switch (button.getId()) {
            case R.id.merchant_name_camera_image_button:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            default:
                Config.showToastMessage(this, "ImageButton clicked");
        }
    }

    @OnClick(R.id.close_bottom_sheet)
    public void closeBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @OnClick(R.id.picture_from_gallery_button)
    protected void takePictureFromGallery() {
//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(pickIntent, PICK_IMAGE_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_FROM_GALLERY:
                    Uri selectedImageUri = data.getData();

//                    Intent intent = new Intent(getApplicationContext(),PictureActivity.class);

//                    Log.e(TAG,selectedImageUri.toString());
//                    intent.putExtra(PictureActivity.DATA_IMAGE_PATH,selectedImageUri.toString());
//                    startActivity(intent);
            }
        }
    }
}
