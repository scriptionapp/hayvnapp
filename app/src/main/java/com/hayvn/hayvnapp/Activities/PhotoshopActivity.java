package com.hayvn.hayvnapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Helper.SurfaceViewExt;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.PhotoshopBinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PhotoshopActivity extends BaseParentActivity {

    final private String TAG = "PHOTOSHOP";
    ActionBar toolbar;
    public Bitmap imageBitmap;
    public String photoName;
    SurfaceViewExt esv;
    public String action;
    InputStream is = null;

    private PhotoshopBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PhotoshopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        toolbar = getSupportActionBar();
        String mCurrentPhotoPath = intent.getStringExtra("filepath");

        try {
            Uri uri = Uri.parse(mCurrentPhotoPath);
            is = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FILE NOT FOUND");
            e.printStackTrace();
        }

        if(is != null) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            action = "move";
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inPurgeable = true;
            imageBitmap = BitmapFactory.decodeStream(is, null, bmOptions);
            assert mCurrentPhotoPath != null;
            String[] parts = mCurrentPhotoPath.split("/");
            parts = (parts[parts.length - 1]).split("\\.");
            photoName = parts[0];

            if (imageBitmap != null) {
                esv = new SurfaceViewExt(this, imageBitmap.copy(Bitmap.Config.ARGB_8888, true),
                        imageBitmap.copy(Bitmap.Config.ARGB_8888, true));
                esv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                binding.toAddEsv.addView(esv, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                Toast.makeText(this,
                        TextConstants.AN_ERROR, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_complete_new_log) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

