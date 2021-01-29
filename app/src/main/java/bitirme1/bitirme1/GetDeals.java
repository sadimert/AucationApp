package bitirme1.bitirme1;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class GetDeals extends ArrayAdapter<WishInformation> {

    private Activity Context;
    private List<WishInformation> WishList;

    public GetDeals(Activity Context, List<WishInformation> WishList){
        super(Context,R.layout.wish_list_layout,WishList);
        this.Context = Context;
        this.WishList = WishList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = Context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.wish_list_layout,parent,false);

        ImageView WishImage = (ImageView) listViewItem.findViewById(R.id.ImageView_WishImage);
        TextView WishName = (TextView) listViewItem.findViewById(R.id.TextView_WishName);
        TextView WishBrand = (TextView) listViewItem.findViewById(R.id.TextView_WishBrand);
        TextView WishModel = (TextView) listViewItem.findViewById(R.id.TextView_WishModel);

        WishInformation wishInformation = WishList.get(position);

        if (wishInformation.ImageDownloadLink.equals("NoImage")){
            Picasso.with(Context).load("https://firebasestorage.googleapis.com/v0/b/android-dayi.appspot.com/o/no_image.png?alt=media&token=b458f95f-68e7-42af-8b37-c3df9c9fa5ac")
                    .resize(200,200).placeholder(R.drawable.progress_animation).into(WishImage);
        }
        else {
            Picasso.with(Context).load(wishInformation.ImageDownloadLink).resize(200,200).placeholder(R.drawable.progress_animation).into(WishImage);
        }

        WishName.setText(wishInformation.Name);
        WishBrand.setText(wishInformation.Brand);
        WishModel.setText(wishInformation.Model);

        return listViewItem;
    }
}
