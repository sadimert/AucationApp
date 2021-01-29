package bitirme1.bitirme1;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class BidAdapter extends ArrayAdapter<BidInformation> {

    private Activity Context;
    private List<BidInformation> BidList;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceBid;
    private DatabaseReference databaseReferenceNotifications;
    private String WishName;
    private String BidUserUid;

    public BidAdapter(Activity Context,List<BidInformation> BidList){
        super(Context,R.layout.bid_list_layout,BidList);
        this.Context = Context;
        this.BidList = BidList;;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater infilater = Context.getLayoutInflater();

        View listViewItem = infilater.inflate(R.layout.bid_list_layout,parent,false);

        final TextView NameOfBidder = (TextView) listViewItem.findViewById(R.id.TextView_NameOfBidder);
        TextView BidAmount = (TextView) listViewItem.findViewById(R.id.TextView_BidAmount);
        final CheckBox Checkbox_Ok = (CheckBox) listViewItem.findViewById(R.id.CheckBox_Ok);

        final BidInformation bidInformation = BidList.get(position);

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseReferenceNotifications = FirebaseDatabase.getInstance().getReference("Notifications");

        databaseReferenceUsers.child(bidInformation.UserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NameOfBidder.setText(dataSnapshot.child("Name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        BidAmount.setText(bidInformation.BidAmount);
        databaseReferenceBid = bidInformation.databaseReferenceBid;
        WishName = bidInformation.WishName;
        BidUserUid = bidInformation.UserUid;
        if (bidInformation.IsAccepted.equals("true")){
            Checkbox_Ok.setChecked(true);
        }
        else{
            Checkbox_Ok.setChecked(false);
        }
        Checkbox_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean isChecked = Checkbox_Ok.isChecked();
                databaseReferenceBid = bidInformation.databaseReferenceBid;
                BidUserUid = bidInformation.UserUid;
                if (isChecked){
                    databaseReferenceBid.child("IsAccepted").setValue("true");
                    SendBidAcceptedNotification(WishName,BidUserUid);
                }
                else{
                    databaseReferenceBid.child("IsAccepted").setValue("false");
                }
            }
        });

        return listViewItem;
    }

    private void SendBidAcceptedNotification(String WishName, String BidUserUid) {
        final String title = "New Deal!!";
        final String message = "Your Bid is Accepted for " + WishName ;
        getUserTokenFromUserUid(title,message,BidUserUid);
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
            databaseReferenceNotifications.child(id).setValue(notificationInformation).addOnCompleteListener(Context, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Context, "Notification Sended!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Context, "Notification Couldn't Sended!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(Context, "Notification Couldn't Sended!", Toast.LENGTH_SHORT).show();
        }
    }
}
