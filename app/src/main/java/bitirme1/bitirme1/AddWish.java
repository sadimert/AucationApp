package bitirme1.bitirme1;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class AddWish extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_WISH_IMAGE = 951;
    private static final int REQUEST_LOCATION = 156;
    private static final int REQUEST_LOCATION_SETTINGS = 6453;

    private AlertDialog.Builder builder;

    private ImageView WishImage;
    private EditText EditText_NameOfProduct, EditText_Brand, EditText_Model, EditText_AdditionalInfo, EditText_LinkOfProduct;
    private Button Btn_Upload;
    private Spinner Spinner_SelectCountryFrom;
    private Spinner Spinner_SelectCountryTo;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceWishes;
    private StorageReference storageReference;

    private LocationManager locationManager;
    private ProgressDialog progressDialog;
    private double Lattitude, Longitude;
    private Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wish);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(AddWish.this, Login.class));
        }

        databaseReferenceWishes = FirebaseDatabase.getInstance().getReference("Wishes");
        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        builder = new AlertDialog.Builder(this);

        WishImage = (ImageView) findViewById(R.id.ImageView_WishImage);
        EditText_NameOfProduct = (EditText) findViewById(R.id.EditText_NameOfProduct);
        EditText_Brand = (EditText) findViewById(R.id.EditText_Brand);
        EditText_Model = (EditText) findViewById(R.id.EditText_Model);
        EditText_AdditionalInfo = (EditText) findViewById(R.id.EditText_AdditionalInfo);
        EditText_LinkOfProduct = (EditText) findViewById(R.id.EditText_LinkOfProduct);
        Spinner_SelectCountryFrom = (Spinner) findViewById(R.id.Spinner_SelectCountryFrom);
        Spinner_SelectCountryTo = (Spinner) findViewById(R.id.Spinner_SelectCountryTo);
        Btn_Upload = (Button) findViewById(R.id.Btn_Upload);


        Btn_Upload.setOnClickListener(this);
        WishImage.setOnClickListener(this);

        FillCountriesToSpinner(Spinner_SelectCountryFrom);
        FillCountriesToSpinner(Spinner_SelectCountryTo);

        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        else{
            SelectCurrentCountry();
        }
    }

    private void UploadWish() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        String Name = EditText_NameOfProduct.getText().toString();
        String Brand = EditText_Brand.getText().toString();
        String Model = EditText_Model.getText().toString();
        String AdditionalInfo = EditText_AdditionalInfo.getText().toString();
        String LinkOfProduct = EditText_LinkOfProduct.getText().toString();
        String CountryFrom = Spinner_SelectCountryFrom.getSelectedItem().toString();
        String CountryTo = Spinner_SelectCountryTo.getSelectedItem().toString();
        String CountryFromTo = Spinner_SelectCountryFrom.getSelectedItem().toString() + "_" + Spinner_SelectCountryTo.getSelectedItem().toString();

        if (TextUtils.isEmpty(Name)) {
            Toast.makeText(this, "Please Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Brand)) {
            Toast.makeText(this, "Please Enter Brand", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Model)) {
            Toast.makeText(this, "Please Enter Model", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(AdditionalInfo)) {
            Toast.makeText(this, "Please Enter AdditionalInfo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(LinkOfProduct)) {
            LinkOfProduct = "No Link";
        }

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String formattedDate = df.format(c.getTime());
        final String image_name = user.getUid() + "_" + formattedDate;

        final String id = databaseReferenceWishes.push().getKey();
        WishInformation wishInformation = new WishInformation(id, Name, Brand, Model, AdditionalInfo, LinkOfProduct, CountryFrom, CountryTo,CountryFromTo, image_name, user.getUid());
        databaseReferenceWishes.child(id).setValue(wishInformation).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    UploadWishImage(image_name, id);
                } else {
                    Toast.makeText(AddWish.this, "Wish Could not Uploaded Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UploadWishImage(String ImageName, final String id) {

        if (selectedImage != null) {
            StorageReference riversRef = storageReference.child("images/" + ImageName + ".jpg");

            riversRef.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String ImageDownloadUrl = taskSnapshot.getDownloadUrl().toString();
                            databaseReferenceWishes.child(id).child("ImageDownloadLink").setValue(ImageDownloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddWish.this, "Wish Uploaded With Image", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(AddWish.this, MainActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    databaseReferenceWishes.child(id).child("ImageDownloadLink").setValue("NoImage");
                                    progressDialog.dismiss();
                                    Toast.makeText(AddWish.this, "Wish Uploaded Without Image", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(AddWish.this, MainActivity.class));
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            databaseReferenceWishes.child(id).child("ImageDownloadLink").setValue("NoImage");
                            progressDialog.dismiss();
                            Toast.makeText(AddWish.this, "Wish Uploaded Without Image", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(AddWish.this, MainActivity.class));
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage(((int) progress) + "% Uploaded....");
                        }
                    });
        } else {
            databaseReferenceWishes.child(id).child("ImageDownloadLink").setValue("NoImage");
            Toast.makeText(AddWish.this, "You didn't Add Any Image To Your Wish", Toast.LENGTH_SHORT).show();
            Toast.makeText(AddWish.this, "Wish Uploaded Without Image", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            finish();
            startActivity(new Intent(AddWish.this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ImageView_WishImage:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent, "Select an Image"), RESULT_WISH_IMAGE);
                break;
            case R.id.Btn_Upload:
                UploadWish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_WISH_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                WishImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_LOCATION_SETTINGS){
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this,"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
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
                Toast.makeText(this,"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void FillCountriesToSpinner(Spinner spinner) {

        final ArrayList<String> countries = new ArrayList<String>();
        Collections.addAll(countries,getResources().getStringArray(R.array.Countries));

        //Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, countries);
        spinner.setAdapter(adapter);
    }

    private void SelectCurrentCountry(){
        String CurrentCountry = getCurrentCountry();
        if (!TextUtils.isEmpty(CurrentCountry)){
            ArrayAdapter adapter = (ArrayAdapter) Spinner_SelectCountryTo.getAdapter();
            int position = adapter.getPosition(CurrentCountry);
            if (position != -1){
                Spinner_SelectCountryTo.setSelection(position);
            }
            else{
                builder.setMessage("We Couldn't Detect Your Current Country\nBut You Can Still Select It")
                        .setCancelable(false);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
        else {
            Toast.makeText(this,"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentCountry() {
        String CurrentCountry = "";

        boolean isLocationRecieved = getLocation();

        Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.ENGLISH);
        if (isLocationRecieved){
            try {
                CurrentCountry = geocoder.getFromLocation(Lattitude,Longitude,1).get(0).getCountryName();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error: " + e, Toast.LENGTH_LONG).show();
            }
        }
        else{
            buildAlertMessageNoGps();
        }

        return CurrentCountry;
    }

    private boolean getLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

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
                        Toast.makeText(getApplicationContext(),"We Couldn't Detect Your Current Location\nBut You Can Still Select It",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
