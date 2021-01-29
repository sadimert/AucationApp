package bitirme1.bitirme1;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BrowseWish extends AppCompatActivity implements View.OnClickListener{

    private ImageView ImageView_WishImage;
    private TextView TextView_WishName,TextView_Brand,TextView_Model,TextView_AdditionalInfo,TextView_LinkOfProduct,TextView_FromCountry,TextView_ToCountry
                    ,TextView_OwnerName,TextView_OwnerPhone,TextView_Label3;
    private EditText EditText_BidAmount;
    private Button Btn_MakeBid;
    private LinearLayout LinearLayout_BidAmount;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser User;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceWishes;
    private DatabaseReference databaseReferenceNotifications;

    private String WishID;
    private String WishName;
    private String UserUid;
    private boolean CurrentUserOwnsTheWish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_wish);

        firebaseAuth = FirebaseAuth.getInstance();
        User = firebaseAuth.getCurrentUser();

        if (User == null){
            finish();
            startActivity(new Intent(BrowseWish.this, Login.class));
        }

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseReferenceWishes = FirebaseDatabase.getInstance().getReference("Wishes");
        databaseReferenceNotifications = FirebaseDatabase.getInstance().getReference("Notifications");

        ImageView_WishImage = (ImageView) findViewById(R.id.ImageView_WishImage);
        TextView_WishName = (TextView) findViewById(R.id.TextView_WishName);
        TextView_Brand = (TextView) findViewById(R.id.TextView_Brand);
        TextView_Model = (TextView) findViewById(R.id.TextView_Model);
        TextView_AdditionalInfo = (TextView) findViewById(R.id.TextView_AdditionalInfo);
        TextView_LinkOfProduct = (TextView) findViewById(R.id.TextView_LinkOfProduct);
        TextView_FromCountry = (TextView) findViewById(R.id.TextView_FromCountry);
        TextView_ToCountry = (TextView) findViewById(R.id.TextView_ToCountry);
        TextView_OwnerName = (TextView) findViewById(R.id.TextView_OwnerName);
        TextView_OwnerPhone = (TextView) findViewById(R.id.TextView_OwnerPhone);
        EditText_BidAmount = (EditText) findViewById(R.id.EditText_BidAmount);
        Btn_MakeBid = (Button) findViewById(R.id.Btn_MakeBid);
        LinearLayout_BidAmount = (LinearLayout) findViewById(R.id.LinearLayout_BidAmount);
        TextView_Label3 = (TextView) findViewById(R.id.TextView_Label3);

        Intent intent = getIntent();

        WishID = intent.getStringExtra(MainActivity.WISHID);

        Btn_MakeBid.setOnClickListener(this);

        TextView_AdditionalInfo.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReferenceWishes.child(WishID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView_WishName.setText(dataSnapshot.child("Name").getValue().toString());
                TextView_Brand.setText(dataSnapshot.child("Brand").getValue().toString());
                TextView_Model.setText(dataSnapshot.child("Model").getValue().toString());
                TextView_AdditionalInfo.setText(dataSnapshot.child("Additionalinfo").getValue().toString());
                TextView_LinkOfProduct.setText(dataSnapshot.child("LinkOfProduct").getValue().toString());
                TextView_FromCountry.setText(dataSnapshot.child("CountryFrom").getValue().toString());
                TextView_ToCountry.setText(dataSnapshot.child("CountryTo").getValue().toString());

                String ImageDownloadLink = dataSnapshot.child("ImageDownloadLink").getValue().toString();

                if (ImageView_WishImage.getDrawable() == null){
                    if (ImageDownloadLink.equals("NoImage") || ImageDownloadLink.length() <= 38 || !ImageDownloadLink.substring(0,38).equals("https://firebasestorage.googleapis.com")){
                        Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/android-dayi.appspot.com/o/no_image.png?alt=media&token=b458f95f-68e7-42af-8b37-c3df9c9fa5ac")
                                .placeholder(R.drawable.progress_animation).into(ImageView_WishImage);
                    }
                    else {
                        Picasso.with(getApplicationContext()).load(ImageDownloadLink).placeholder(R.drawable.progress_animation).into(ImageView_WishImage);
                    }
                }

                UserUid = dataSnapshot.child("UserUid").getValue().toString();
                if (UserUid.equals(User.getUid())){
                    LinearLayout_BidAmount.setVisibility(View.GONE);
                    TextView_Label3.setVisibility(View.INVISIBLE);
                    Btn_MakeBid.setText(R.string.activity_BrowseWish_BidButton2);
                    CurrentUserOwnsTheWish = true;
                }
                else{
                    CurrentUserOwnsTheWish = false;
                    if (dataSnapshot.hasChild("Bids") && dataSnapshot.child("Bids").hasChild(User.getUid())){
                        EditText_BidAmount.setText(dataSnapshot.child("Bids").child(User.getUid()).child("BidAmount").getValue().toString());
                    }
                }
                databaseReferenceUsers.child(UserUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        TextView_OwnerName.setText(dataSnapshot.child("Name").getValue().toString());
                        TextView_OwnerPhone.setText(dataSnapshot.child("Phone").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                WishName = TextView_WishName.getText().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == Btn_MakeBid){
            if (!CurrentUserOwnsTheWish){
                String BidAmount = EditText_BidAmount.getText().toString();

                BidInformation bidInformation = new BidInformation(BidAmount,User.getUid(),"false");

                databaseReferenceWishes.child(WishID).child("Bids").child(User.getUid()).setValue(bidInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(BrowseWish.this, "Bid Succesfully Uploaded!", Toast.LENGTH_SHORT).show();
                            sendBidNotification();
                        } else {
                            Toast.makeText(BrowseWish.this, "Bid Could not Uploaded Please Try Again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else{

                Intent intent = new Intent(getApplicationContext(),Bids.class);
                intent.putExtra(MainActivity.WISHID,WishID);
                intent.putExtra(MainActivity.WISHNAME,WishName);
                startActivity(intent);
            }
        }

    }

    private void sendBidNotification(){


        databaseReferenceWishes.child(WishID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String title = "New Bid!!";
                final String message = "You Have New Bid for " + dataSnapshot.child("Name").getValue().toString() ;
                getUserTokenFromUserUid(title,message,dataSnapshot.child("UserUid").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getUserTokenFromUserUid(final String Title, final String Message,String UserUid){
        databaseReferenceUsers.child(UserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String token = dataSnapshot.child("Token").getValue().toString();
                setNotificationOnDataBase(Title,Message,token);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setNotificationOnDataBase(String Title, String Message, String UserToken){
        final String id = databaseReferenceNotifications.push().getKey();
        NotificationInformation notificationInformation = new NotificationInformation(Title,Message, UserToken);
        if (UserToken != null){
            databaseReferenceNotifications.child(id).setValue(notificationInformation).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Notification Sended!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Notification Couldn't Sended!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "Notification Couldn't Sended!", Toast.LENGTH_SHORT).show();
        }
    }


}
