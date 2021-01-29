package bitirme1.bitirme1;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Fragment_SearchDeals extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener{

    private static final int REQUEST_LOCATION = 698;
    private static final int REQUEST_LOCATION_SETTINGS = 1235;

    private AlertDialog.Builder builder;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceWishes;

    private Spinner Spinner_CountriesFrom;
    private Spinner Spinner_CountriesTo;
    private ListView listView_Deals;

    private List<WishInformation> DealList;

    private LocationManager locationManager;
    private double Lattitude, Longitude;

    private Context Context;

    public Fragment_SearchDeals() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_deals,container,false);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            getActivity().finish();
            startActivity(new Intent(getActivity().getApplicationContext(), Login.class));
        }
        Context = getActivity();

        databaseReferenceWishes = FirebaseDatabase.getInstance().getReference("Wishes");

        builder = new AlertDialog.Builder(Context);

        Spinner_CountriesTo = (Spinner) rootView.findViewById(R.id.Spinner_CountriesTo);
        Spinner_CountriesFrom = (Spinner) rootView.findViewById(R.id.Spinner_CountriesFrom);
        listView_Deals = (ListView) rootView.findViewById(R.id.ListView_Deals);

        DealList = new ArrayList<>();

        listView_Deals.setOnItemClickListener(this);
        Spinner_CountriesTo.setOnItemSelectedListener(this);
        Spinner_CountriesFrom.setOnItemSelectedListener(this);

        FillCountriesToSpinner(Spinner_CountriesFrom);
        FillCountriesToSpinner(Spinner_CountriesTo);

        locationManager = (LocationManager) Context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        else{
            SelectCurrentCountry();
        }

        listView_Deals();

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView == listView_Deals){

            WishInformation wishInformation = DealList.get(i);

            Intent intent = new Intent(getActivity().getApplicationContext(), BrowseWish.class);
            intent.putExtra(MainActivity.WISHID,wishInformation.Key);

            ImageView ImageView_WishImage = (ImageView) view.findViewById(R.id.ImageView_WishImage);
            ImageView_WishImage.buildDrawingCache();
            Bitmap bitmap = ImageView_WishImage.getDrawingCache();
            Bundle extras = new Bundle();
            extras.putParcelable(MainActivity.WISHIMAGEBITMAP,bitmap);
            intent.putExtras(extras);

            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_SETTINGS){
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(Context,"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
            }
            else{
                SelectCurrentCountry();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                SelectCurrentCountry();
            }
            else {
                Toast.makeText(Context,"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView == Spinner_CountriesTo){
            listView_Deals();
        }
        else if (adapterView == Spinner_CountriesFrom){
            listView_Deals();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void listView_Deals(){
        String CountryFromTo = Spinner_CountriesFrom.getSelectedItem().toString() + "_" + Spinner_CountriesTo.getSelectedItem().toString();
        databaseReferenceWishes.orderByChild("CountryFromTo").equalTo(CountryFromTo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DealList.clear();

                for (DataSnapshot wishSnapshot : dataSnapshot.getChildren()){
                    WishInformation DealInformation = wishSnapshot.getValue(WishInformation.class);
                    DealList.add(DealInformation);
                }

                GetDeals adapter = new GetDeals(getActivity(),DealList);
                listView_Deals.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FillCountriesToSpinner(Spinner spinner) {

        final ArrayList<String> countries = new ArrayList<String>();
        Collections.addAll(countries,getResources().getStringArray(R.array.Countries));

        //Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Context, android.R.layout.simple_spinner_item, countries);
        spinner.setAdapter(adapter);
    }

    private void SelectCurrentCountry(){
        String CurrentCountry = getCurrentCountry();
        if (!TextUtils.isEmpty(CurrentCountry)){
            ArrayAdapter adapter = (ArrayAdapter) Spinner_CountriesFrom.getAdapter();
            int position = adapter.getPosition(CurrentCountry);
            if (position != -1){
                Spinner_CountriesFrom.setSelection(position);
            }
            else{
                Toast.makeText(Context,"We Couldn't Select Your Current Country\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getCurrentCountry() {
        String CurrentCountry = "";

        boolean isLocationRecieved = getLocation();

        Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(),Locale.ENGLISH);
        if (isLocationRecieved){
            try {
                CurrentCountry = geocoder.getFromLocation(Lattitude,Longitude,1).get(0).getCountryName();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Context,"Error: " + e, Toast.LENGTH_LONG).show();
            }
        }

        return CurrentCountry;
    }

    private boolean getLocation() {
        if (ActivityCompat.checkSelfPermission(Context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (Context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }
        else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                Lattitude = location.getLatitude();
                Longitude = location.getLongitude();
                return true;
            }
            else  if (location1 != null) {
                Lattitude = location1.getLatitude();
                Longitude = location1.getLongitude();
                return true;
            }
            else  if (location2 != null) {
                Lattitude = location2.getLatitude();
                Longitude = location2.getLongitude();
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),REQUEST_LOCATION_SETTINGS);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Toast.makeText(Context,"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
