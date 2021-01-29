package bitirme1.bitirme1;

import com.google.firebase.database.DatabaseReference;

public class BidInformation {
    public String BidAmount;
    public String UserUid;
    public String IsAccepted;
    public DatabaseReference databaseReferenceBid;
    public String WishName;

    public BidInformation(){

    }

    public BidInformation(String bidAmount, String userUid, String isAccepted) {
        BidAmount = bidAmount;
        UserUid = userUid;
        IsAccepted = isAccepted;
    }

    public BidInformation(String bidAmount, String userUid, String isAccepted, DatabaseReference databaseReferenceBid,String wishName){
        BidAmount = bidAmount;
        UserUid = userUid;
        IsAccepted = isAccepted;
        databaseReferenceBid = databaseReferenceBid;
        WishName = wishName;
    }
}
