package bitirme1.bitirme1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Bids extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceWish;

    private ListView ListView_BidList;

    private String WishID;
    private String WishName;
    private List<BidInformation> BidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bids);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            this.finish();
            startActivity(new Intent(getApplicationContext(), Login.class));
        }

        WishID = getIntent().getStringExtra(MainActivity.WISHID);
        WishName = getIntent().getStringExtra(MainActivity.WISHNAME);
        databaseReferenceWish = FirebaseDatabase.getInstance().getReference("Wishes").child(WishID);

        ListView_BidList = (ListView) findViewById(R.id.ListView_BidList);

        BidList = new ArrayList<>();

        fillBidsFromDB();
    }

    private void fillBidsFromDB(){
        databaseReferenceWish.child("Bids").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BidList.clear();

                for (DataSnapshot bidSnapshot : dataSnapshot.getChildren()){
                    final BidInformation bidInformation = bidSnapshot.getValue(BidInformation.class);
                    bidInformation.databaseReferenceBid = bidSnapshot.getRef();
                    bidInformation.WishName = WishName;
                    BidList.add(bidInformation);
                }

                BidAdapter adapter = new BidAdapter(Bids.this,BidList);
                ListView_BidList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
