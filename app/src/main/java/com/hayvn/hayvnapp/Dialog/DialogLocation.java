package com.hayvn.hayvnapp.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.NumberUtils;
import com.google.android.gms.maps.model.LatLng;
import com.hayvn.hayvnapp.Activities.MapActivity;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Helper.NetworkStateChangeBroadcaster;
import com.hayvn.hayvnapp.Helper.SharedPrefHelper;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.ViewModel.UserViewModel;
import com.hayvn.hayvnapp.databinding.DialogLocationBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.IntentConstants.LAT_LNG;
import static com.hayvn.hayvnapp.Constant.TextConstants.INTERNET_LOCATION;
import static com.hayvn.hayvnapp.Constant.TextConstants.LOCATION_OPTIONS;


public class DialogLocation  extends DialogFragment {
    private static final String TAG = "LOC_DIAL";

    private String[] options = LOCATION_OPTIONS;
    private static final int GET_COORDS = 22;
    Context context;
    String raw_location;
    boolean show_site = true;
    ArrayList<String> sites = new ArrayList<String>(Arrays.asList(options));
    ArrayList<String> countries = new ArrayList<String>();
    UserViewModel mViewModel;
    String site = "",
            country = "",

            district = "",
            city = "",
            upazila = "",
            union = "",
            village = "",

            address = "",
            coord_lon = "",
            coord_lat = "";
    int site_n = 0,
            country_n = 1,
            district_n = 2,

            city_n = 3,
            upazila_n = 4,
            union_n = 5,
            village_n = 6,

            address_n = 7,
            coord_lon_n = 8,
            coord_lat_n = 9;

    private DialogLocationBinding binding;

    public interface EditedLocation {
        void onLocationEdited(String location);
    }

    public DialogLocation(){

    }

    public DialogLocation(String location){
        this.raw_location = location;
    }

    public DialogLocation(String location, boolean show_site){
        this.raw_location = location;
        this.show_site = show_site;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//onCreateDialog(Bundle savedInstanceState) {

        binding =DialogLocationBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        context = getContext();
        builder.setView(binding.getRoot());

        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow())
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        countries = SharedPrefHelper.getStringList(context, SharedprefConstants.COUNTRY_LIST_FULL);
        this.prepareLocations(raw_location);
        setViews();
        binding.listCountries.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null){
                    String new_sel = s.toString();
                    if(!countries.contains(new_sel)){
                        binding.listCountries.setTextColor( Color.RED);
                    }else{
                        binding.listCountries.setTextColor( Color.BLACK);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnLaterDialogLoc.setOnClickListener(v -> {
            dismiss();
        });

        binding.btnOkDialogLoc.setOnClickListener(v -> {
            prepareStringClose();
        });

        binding.btnAddcoordDialogLoc.setOnClickListener((v ->{
            boolean isNetworkAvialable = NetworkStateChangeBroadcaster.getIsConnected();
            if(isNetworkAvialable) {
                openMapsAndGetCoord();
            } else {
                androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
                alertDialog.setTitle("!");
                alertDialog.setMessage(INTERNET_LOCATION);
                alertDialog.setButton(
                        androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.okok), (dialog, which) -> {
                    openMapsAndGetCoord();
                });
                alertDialog.show();
            }
        }));


        return binding.getRoot();
    }


    private void setViews(){
        if(!sites.contains(site)){
            sites.add(site);
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                sites);
        binding.listSites.setAdapter(spinnerArrayAdapter);
        binding.listSites.setSelection(Math.max(sites.indexOf(site), 0 ));
        ArrayAdapter<String> countries_adapter = new ArrayAdapter<String>
                (context, android.R.layout.select_dialog_item, countries);
        binding.listCountries.setThreshold(0);//will start working from first character
        binding.listCountries.setAdapter(countries_adapter);//setting the adapter data into the AutoCompleteTextView
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            binding.listCountries.setTextColor( getResources().getColor(R.color.colorAccent, null));
        }
        binding.listCountries.setText(country);
        if(!countries.contains(country)){
            binding.listCountries.setTextColor( Color.RED);
        }else{
            binding.listCountries.setTextColor( Color.BLACK);
        }

        binding.districtDialogLoc.setText(district);
        binding.cityDialogLoc.setText(city);
        binding.upazilaDialogLoc.setText(upazila);
        binding.unionDialogLoc.setText(union);
        binding.villageDialogLoc.setText(village);
        binding.addressDialogLoc.setText(address);
        binding.coordsDialogLoc.setText(editCoords());

        if(!show_site){

            binding.siteSelect.setVisibility(View.GONE);
            binding.listSites.setSelection(sites.indexOf("Patient's home")); //
        }

        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mViewModel.getSettings().observe(this, setting -> {
            assert setting != null;
            if(country==null || country.equals("")){
                binding.listCountries.setText(setting.getStringTwo());
            }
        });
    }

    private void prepareLocations(String raw_location){
        ArrayList<String> locations = new ArrayList<String>();
        if(raw_location != null){
            locations = new ArrayList<String>( Arrays.asList(raw_location.split(";", -1)) );
        }
        if(raw_location!=null) Log.d(TAG, "SZ: "+raw_location.split(";", -1).length);

        if(locations.size() > site_n) {
            site = locations.get(site_n);
            if (!sites.contains(site) && !site.equals("")) {
                sites.add(site);
            }
        }
        if(locations.size() > country_n) {
            country = locations.get(country_n);
            if (!countries.contains(site) && !countries.equals("")) {
                countries.add(country);
            }
        }
        if(locations.size() > city_n) {
            city = locations.get(city_n);
            if(isNumeric(city)){city="";}
        }
        if(locations.size() > district_n) {
            district = locations.get(district_n);
            if(isNumeric(district)){district="";}
        }
        if(locations.size() > upazila_n) {
            upazila = locations.get(upazila_n);
            if(isNumeric(upazila)){upazila="";}
        }
        if(locations.size() > union_n) {
            union = locations.get(union_n);
            if(isNumeric(union)){union="";}
        }
        if(locations.size() > village_n) {
            village = locations.get(village_n);
            if(isNumeric(village)){village="";}
        }
        if(locations.size() > address_n) {
            address = locations.get(address_n);
            if(isNumeric(address)){village="";}
        }
        if(locations.size() > coord_lon_n) {
            coord_lon = locations.get(coord_lon_n);
        }
        if(locations.size() > coord_lat_n) {
            coord_lat = locations.get(coord_lat_n);
        }
        //check potential problems with lon lat
        if(!isNumeric(coord_lon) && !isNumeric(coord_lat)){
            Log.d(TAG, "small problem with location");
            for(int i=0;i<(locations.size()-1);i++){
                if(isNumeric(locations.get(i)) && isNumeric(locations.get(i+1))){
                    String temp = coord_lon;
                    coord_lon = locations.get(i);
                    address = address + " " + temp;
                    temp = coord_lat;
                    coord_lat = locations.get(i+1);
                    address = address + " " + temp;
                }
            }
        }

    }

    private boolean isNumeric(String s){
        return s.matches("^-?\\d+(?:\\.\\d+)?");
    }

    private void openMapsAndGetCoord(){
        //THIS METHOD SHOULD CALL MAPS AND RETURN COORD
//        coord_lon = "50.05";
//        coord_lat = "-57.88";
//        coords_dialog_loc.setText(editCoords());

        Intent myIntent = new Intent(context, MapActivity.class);
        if(coord_lon != null && coord_lat != null &&
                !coord_lon.equals("") && !coord_lat.equals("")){
            try{
                double d1 = Double.parseDouble(coord_lat);
                double d2 = Double.parseDouble(coord_lon);
                myIntent.putExtra(LAT_LNG, new LatLng(d1, d2));
            }catch(Exception e){

            }
        }
        startActivityForResult(myIntent, GET_COORDS);
    }

    private String editCoords(){
        if(nullOrEmpty(coord_lon).equals("")){return "";}
        return nullOrEmpty(coord_lon) + "; " + nullOrEmpty(coord_lat);
    }

    private String replaceSpecial(String s){
        if(s==null){return "";}
        s = s.replace(";", ",");
        return s;
    }

    private void prepareStringClose(){
        site = nullOrEmpty((Objects.requireNonNull(binding.listSites.getSelectedItem().toString())));
        country = nullOrEmpty(Objects.requireNonNull(binding.listCountries.getText().toString()));
        district = nullOrEmpty(Objects.requireNonNull(binding.districtDialogLoc.getText().toString()));
        city = nullOrEmpty(Objects.requireNonNull(binding.cityDialogLoc.getText().toString()));
        upazila = nullOrEmpty(Objects.requireNonNull(binding.upazilaDialogLoc.getText().toString()));
        union = nullOrEmpty(Objects.requireNonNull(binding.unionDialogLoc.getText().toString()));
        village = nullOrEmpty(Objects.requireNonNull(binding.villageDialogLoc.getText().toString()));
        address = nullOrEmpty(Objects.requireNonNull(binding.addressDialogLoc.getText().toString()));

        if(!countries.contains(country)){
            Toast.makeText(context, "Please specify the country from the list", Toast.LENGTH_LONG).show();
        }else{
            String out = replaceSpecial(site) + ";" +
                    replaceSpecial(country) + ";" +
                    replaceSpecial(district) + ";" +
                    replaceSpecial(city) + ";" +
                    replaceSpecial(upazila) + ";" +
                    replaceSpecial(union) + ";" +
                    replaceSpecial(village) + ";" +
                    replaceSpecial(address) + ";" +
                    replaceSpecial(nullOrEmpty(coord_lon)) + ";" +
                    replaceSpecial(nullOrEmpty(coord_lat));
            DialogLocation.EditedLocation mListener = (DialogLocation.EditedLocation) context;
            mListener.onLocationEdited(out);
            mViewModel.getSettings().removeObservers(this);
            dismiss();
        }

    }

    private String nullOrEmpty(String st){
        if(st != null){
            return st;
        }else{
            return "";
        }
    }

    @Override
    public void onActivityResult(int requestCode , int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_COORDS && resultCode == Activity.RESULT_OK) {
            if(data != null){
                LatLng location = (LatLng) Objects.requireNonNull(data.getExtras()).get("LatLng");
                assert location != null;
                coord_lat = String.valueOf(location.latitude);
                coord_lon = String.valueOf(location.longitude);
                binding.coordsDialogLoc.setText(editCoords());
            }
        }
    }

}
