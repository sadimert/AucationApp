package bitirme1.bitirme1;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    public void onTokenRefresh() {

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        String refreshToken = FirebaseInstanceId.getInstance().getToken();

        databaseReference.child(user.getUid()).child("Token").setValue(refreshToken);
    }
}
