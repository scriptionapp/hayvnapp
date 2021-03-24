package com.hayvn.hayvnapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toolbar;

import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.DrawerFaqBinding;

/**
 * Created by Olia on 06/01/2018.
 */
public class FaqActivity extends BaseParentActivity {


    String str = "";
    private DrawerFaqBinding binding;
    private ActionBar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DrawerFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        toolbar = getSupportActionBar();

        binding.faqTextView.setText(Html.fromHtml(str));
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
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


