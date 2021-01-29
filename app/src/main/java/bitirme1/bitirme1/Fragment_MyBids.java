package bitirme1.bitirme1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_MyBids extends Fragment implements AdapterView.OnItemClickListener {

    private FirebaseUser User;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceWishes;

    private ListView listView_MyBidsList;

    private List<WishInformation> MyBidsList;
    private Context Context;


    public Fragment_MyBids(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_bids,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        User = firebaseAuth.getCurrentUser();
        if (User == null){
            getActivity().finish();
            startActivity(new Intent(getActivity().getApplicationContext(), Login.class));
        }
        Context = getActivity();

        databaseReferenceWishes = FirebaseDatabase.getInstance().getReference("Wishes");

        listView_MyBidsList = (ListView) rootView.findViewById(R.id.ListView_MyBidsList);
        MyBidsList = new ArrayList<>();

        listView_MyBidsList.setOnItemClickListener(this);
        fillListWithWishesUserMakeBid();

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView == listView_MyBidsList){

            WishInformation wishInformation = MyBidsList.get(i);

            Intent intent = new Intent(Context, BrowseWish.class);
            intent.putExtra(MainActivity.WISHID,wishInformation.Key);

            startActivity(intent);
        }
    }

    private void fillListWithWishesUserMakeBid(){
        databaseReferenceWishes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MyBidsList.clear();
                for (DataSnapshot wishSnapshot : dataSnapshot.getChildren()){
                    if (wishSnapshot.hasChild("Bids") && wishSnapshot.child("Bids").hasChild(User.getUid())){
                        WishInformation wishInformation = wishSnapshot.getValue(WishInformation.class);
                        MyBidsList.add(wishInformation);
                    }
                }
                MyBidAdapter adapter = new MyBidAdapter(getActivity(),MyBidsList);
                listView_MyBidsList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
