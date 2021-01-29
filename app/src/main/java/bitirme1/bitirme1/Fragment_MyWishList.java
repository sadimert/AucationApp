package bitirme1.bitirme1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class Fragment_MyWishList extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceWishes;

    FirebaseUser User;

    private Button Btn_AddWish;
    private ListView listView_WishList;

    private List<WishInformation> WishList;

    public Fragment_MyWishList() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_wish_list,container,false);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            getActivity().finish();
            startActivity(new Intent(getActivity().getApplicationContext(), Login.class));
        }
        User = firebaseAuth.getCurrentUser();

        databaseReferenceWishes = FirebaseDatabase.getInstance().getReference("Wishes");

        Btn_AddWish = (Button) rootView.findViewById(R.id.Btn_AddWish);
        listView_WishList = (ListView) rootView.findViewById(R.id.ListView_WishList);

        WishList = new ArrayList<>();

        Btn_AddWish.setOnClickListener(this);
        listView_WishList.setOnItemClickListener(this);

        databaseReferenceWishes.orderByChild("UserUid").equalTo(User.getUid().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                WishList.clear();

                for (DataSnapshot wishSnapshot : dataSnapshot.getChildren()){
                    WishInformation wishInformation = wishSnapshot.getValue(WishInformation.class);
                    WishList.add(wishInformation);
                }

                MyWishList adapter = new MyWishList(getActivity(),WishList);
                listView_WishList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == Btn_AddWish){
            startActivity(new Intent(getActivity().getApplicationContext(),AddWish.class));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView == listView_WishList){

            WishInformation wishInformation = WishList.get(i);

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
}
