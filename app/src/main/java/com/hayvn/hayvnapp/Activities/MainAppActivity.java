package com.hayvn.hayvnapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.navigation.NavigationView;
import com.hayvn.hayvnapp.Constant.Constant;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.MethodConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Constant.TextConstants;
import com.hayvn.hayvnapp.Dialog.DialogPushPullData;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Dialog.DialogHelloAgain;
import com.hayvn.hayvnapp.Fragment.FragmentCases;
import com.hayvn.hayvnapp.Fragment.FragmentWelcomeDialog;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.PullData;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.ProdDev.Modes;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Utilities.DateUtils;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.hayvn.hayvnapp.databinding.ActivityMainappBinding;
import com.hayvn.hayvnapp.databinding.AppBarBinding;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.hayvn.hayvnapp.Constant.Constant.MODIFY_SUMMARY;
import static com.hayvn.hayvnapp.Constant.SharedprefConstants.FOR_WELCOME_DIALOG_VAL_LOGIN;
import static com.hayvn.hayvnapp.Constant.SharedprefConstants.MEDIA_NOT_SYNCED;

public class MainAppActivity extends BaseParentActivity implements
        FragmentWelcomeDialog.WelcomeDialogListener,View.OnClickListener{

    public static final String TAG = "firebase_user";

    private Context context;
    Fragment fragment;
    FirebaseAuth mAuth;
    String outstnadingFilesToPull;
    CaseRepo cRepo = null;
    private ActivityMainappBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainappBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarLayout.toolbar);
        getSupportActionBar().setLogo(scaleImage( getResources().getDrawable(R.drawable.ic_intro),0.05f));
        getSupportActionBar().setTitle("");
        context = this;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.appBarLayout.toolbar
                , R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        selectFragment(Constant.CASE, null);
        showWelcomeDialog();

        UserViewModel mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            assert setting != null;
        });

        mAuth = FirebaseAuth.getInstance();

        if(Modes.getDashboard().equals("disabled")){
            binding.drawer.navDashboard.setVisibility(View.GONE);
        }
        if(Modes.getFaq().equals("disabled")){
            binding.drawer.navFaq.setVisibility(View.GONE);
        }
        if(Modes.getMedical().equals("medical")){
            binding.drawer.gettingLegalHelpSection1.setVisibility(View.GONE);
        }

        binding.drawer.navAddCase.setOnClickListener(this);
        binding.drawer.navAccountSettings.setOnClickListener(this);
        binding.drawer.navFaq.setOnClickListener(this);
        binding.drawer.navDashboard.setOnClickListener(this);
        binding.drawer.syncData.setOnClickListener(this);
        binding.drawer.navSignOut.setOnClickListener(this);
        binding.drawer.hamburgerDrawer.setOnClickListener(v -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
        checkLaunchSyncDialog();


    }

    private Drawable scaleImage (Drawable image, float scaleFactor) {

        if ((image == null) || !(image instanceof BitmapDrawable)) {
            return image;
        }

        Bitmap b = ((BitmapDrawable)image).getBitmap();

        int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
        int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);

        image = new BitmapDrawable(getResources(), bitmapResized);

        return image;

    }

    String what_to_sync = null;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Fired permission in mainapp");
        if(requestCode == PullData.WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(what_to_sync == null){
                    String have_synced_all = SharedPrefHelper.readString(context, SharedprefConstants.SYNCED_ALL_ALREADY);
                    what_to_sync = MethodConstants.PULL_WHAT_OTHERS;
                    if(have_synced_all == null || !have_synced_all.equals(SharedprefConstants.SYNCED_ALL_ALREADY)){
                        what_to_sync = MethodConstants.PULL_WHAT_ALL_RECENT;
                    }
                }
                sendIntent("Restart_files");
            } else if(grantResults.length > 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this,
                        TextConstants.PERMISSION_DENIED,
                        Toast.LENGTH_LONG).show();
                DialogPushPullData.getInstance().dismissPullPushDialog();
            }
        }
    }

    private void sendIntent(String val) {
        Intent intent = new Intent();
        intent.setAction(IntentConstants.SYNC_TO_FIRESTORE_ACTION);
        intent.putExtra(IntentConstants.SYNC_BROADCAST_MESSAGE, val);
        this.sendBroadcast(intent);
    }

    private void checkLaunchSyncDialog(){
        String last_updated = SharedPrefHelper.readString(context, SharedprefConstants.TIME_LAST_SYNC);
        String have_synced_all = SharedPrefHelper.readString(context, SharedprefConstants.SYNCED_ALL_ALREADY);
        if( last_updated == null || DateUtils.hoursToNow(Long.parseLong(last_updated))>24){
            what_to_sync = MethodConstants.PULL_WHAT_OTHERS;
            if(have_synced_all == null || !have_synced_all.equals(SharedprefConstants.SYNCED_ALL_ALREADY)){
                what_to_sync = MethodConstants.PULL_WHAT_ALL_RECENT;
            }
            if(NetworkStateChangeBroadcaster.getIsConnected()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    DialogPushPullData.getInstance().launchPushPullDataDialog(this, this.getApplication(), context, true, what_to_sync);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showResync();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    public void showResync(){
        outstnadingFilesToPull = SharedPrefHelper.readString(context, Constant.FIREBASESYNCSTATUS);
        if(outstnadingFilesToPull==null){outstnadingFilesToPull="";}
        if(outstnadingFilesToPull.equals(MEDIA_NOT_SYNCED)){
            Drawable img = context.getResources().getDrawable( R.drawable.ic_red_get_files );
            binding.drawer.navAccountSettings.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
            binding.drawer.navAccountSettings.setText("Sync Data & Settings");
        }else{
            Drawable img = context.getResources().getDrawable( R.drawable.ic_settings_black );
            binding.drawer.navAccountSettings.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
            binding.drawer.navAccountSettings.setText(R.string.settings);
        }
    }

    public void selectFragment(int type, Bundle bundle) {
        boolean isAdd = true;

        switch (type) {
            case Constant.CASE:
                fragment = new FragmentCases();
                isAdd = false;
                break;
            default:
                return;
        }
        if (bundle != null)
            fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (isAdd) {
            fragmentTransaction.add(R.id.fragment_container, fragment);
            fragmentTransaction.addToBackStack(null);
        } else
            fragmentTransaction.replace(R.id.fragment_container, fragment);

        fragmentTransaction.commit();
    }

    public void showWelcomeDialog() {
        String login_status = SharedPrefHelper.readString(context, SharedprefConstants.FOR_WELCOME_DIALOG);
        if (login_status == null || (!login_status.equals(FOR_WELCOME_DIALOG_VAL_LOGIN) && !login_status.equals(SharedprefConstants.WELCOME_DIALOG_NOT_NEEDED)) ){
            SharedPrefHelper.writeString(context, SharedprefConstants.FOR_WELCOME_DIALOG, SharedprefConstants.WELCOME_DIALOG_NOT_NEEDED);
            DialogFragment dialog = new FragmentWelcomeDialog();
            dialog.show(getSupportFragmentManager(), "WelcomeDialog");
        }else if(login_status.equals("login")){
            SharedPrefHelper.writeString(context, SharedprefConstants.FOR_WELCOME_DIALOG, SharedprefConstants.WELCOME_DIALOG_NOT_NEEDED);
            DialogFragment dialog = new DialogHelloAgain();
            dialog.show(getSupportFragmentManager(), "HelloAgainDialog");
        }
    }

    @Override
    public void onDialogPositiveClick1() {
        //DialogView.getInstance().showCreateDialog(this, getString(R.string.assign_a_name));
    }

    @Override
    public void onDialogNegativeClick1() {
    }

    @Override
    public void onBackPressed() {

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MODIFY_SUMMARY && resultCode == Activity.RESULT_OK) {
            if (((FragmentCases) fragment).adapter.case_ != null) {
                ((FragmentCases) fragment).adapter.case_.setSummary(data.getStringExtra("NEW_SUMMARY_INSTANCE"));
                cRepo = new CaseRepo(getApplication());
                cRepo.update(((FragmentCases) fragment).adapter.case_);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        if (view == binding.drawer.navAddCase){
            DialogView.getInstance().showCreateDialog(this, getString(R.string.add_the_case));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawer.navDashboard){
            startActivity(new Intent(context, DashboardActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawer.navAccountSettings){
            startActivity(new Intent(context, SettingActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawer.navFaq){
            startActivity(new Intent(context, FaqActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else if (view == binding.drawer.navSignOut){
            PullData pd = new PullData();
            pd.eraseDB(context, "MainApp");// will call onDBErasedBelow
        }else if (view == binding.drawer.syncData){
            DialogPushPullData.getInstance().launchPushPullDataDialog(this, this.getApplication(), context, true, MethodConstants.PULL_WHAT_OTHERS);
        }
    }


    public void onDBErased(){
        FirebaseAuth.getInstance().signOut();
        context.startActivity(new Intent(context, LoginActivity.class));
    }
}
