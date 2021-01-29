package bitirme1.bitirme1;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyBidAdapter extends ArrayAdapter<WishInformation> {

    private FirebaseUser User;
    private FirebaseAuth firebaseAuth;

    private Activity Context;
    private List<WishInformation> MyBidsList;
    private boolean IsBidAccepted;
    private String CurrentUserUid;

    public MyBidAdapter(Activity Context,List<WishInformation> MyBidsList){
        super(Context,R.layout.my_bid_list_layout,MyBidsList);
        this.Context = Context;
        this.MyBidsList = MyBidsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        firebaseAuth = FirebaseAuth.getInstance();
        User = firebaseAuth.getCurrentUser();
        if (User == null){
            Context.finish();
            Context.startActivity(new Intent(Context.getApplicationContext(), Login.class));
        }
        CurrentUserUid = User.getUid();

        LayoutInflater inflater = Context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.my_bid_list_layout,parent,false);

        IsBidAccepted = false;
        ImageView WishImage = (ImageView) listViewItem.findViewById(R.id.ImageView_WishImage);
        TextView WishName = (TextView) listViewItem.findViewById(R.id.TextView_WishName);
        TextView WishBrand = (TextView) listViewItem.findViewById(R.id.TextView_WishBrand);
        TextView WishModel = (TextView) listViewItem.findViewById(R.id.TextView_WishModel);
        final CheckBox CheckBox_IsBidAccepted = (CheckBox) listViewItem.findViewById(R.id.CheckBox_IsBidAccepted);

        WishInformation wishInformation = MyBidsList.get(position);

        if (wishInformation.ImageDownloadLink.equals("NoImage") || wishInformation.ImageDownloadLink.length() <= 38 || !wishInformation.ImageDownloadLink.substring(0,38).equals("https://firebasestorage.googleapis.com")){
            Picasso.with(Context).load("https://firebasestorage.googleapis.com/v0/b/android-dayi.appspot.com/o/no_image.png?alt=media&token=b458f95f-68e7-42af-8b37-c3df9c9fa5ac")
                    .resize(200,200).placeholder(R.drawable.progress_animation).into(WishImage);
        }
        else {
            Picasso.with(Context).load(wishInformation.ImageDownloadLink).resize(200,200).placeholder(R.drawable.progress_animation).into(WishImage);
        }


        FirebaseDatabase.getInstance().getReference("Wishes").child(wishInformation.Key).child("Bids").child(CurrentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("IsAccepted").getValue().toString().equals("true")){
                    IsBidAccepted = true;
                    CheckBox_IsBidAccepted.setChecked(IsBidAccepted);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        WishName.setText(wishInformation.Name);
        WishBrand.setText(wishInformation.Brand);
        WishModel.setText(wishInformation.Model);
        CheckBox_IsBidAccepted.setChecked(IsBidAccepted);

        return listViewItem;

    }
}
