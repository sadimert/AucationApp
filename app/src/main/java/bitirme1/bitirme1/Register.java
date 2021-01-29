package bitirme1.bitirme1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Register extends AppCompatActivity implements View.OnClickListener{


    private EditText EditText_Name;
    private EditText EditText_Email;
    private EditText EditText_Password;
    private EditText EditText_ConfirmPassword;
    private EditText EditText_Phone;
    private Button button_Register;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        progressDialog = new ProgressDialog(this);

        EditText_Name = (EditText) findViewById(R.id.EditText_Name);
        EditText_Email = (EditText) findViewById(R.id.EditText_Email);
        EditText_Password = (EditText) findViewById(R.id.EditText_Password);
        EditText_ConfirmPassword = (EditText) findViewById(R.id.EditText_ConfirmPassword);
        EditText_Phone = (EditText) findViewById(R.id.EditText_Phone);
        button_Register = (Button) findViewById(R.id.button_Register);

        button_Register.setOnClickListener(this);
    }

    private void registerUser(){
        String name = EditText_Name.getText().toString();
        String email = EditText_Email.getText().toString();
        String phone = EditText_Phone.getText().toString();
        String password = EditText_Password.getText().toString();
        String confirmPassword = EditText_ConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please Enter phone", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Please Confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Please Confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8 || confirmPassword.length() < 8){
            Toast.makeText(this, "Password Needs to be at least 8 character", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registerring...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    saveUserInformation();
                }
                else{
                    Toast.makeText(Register.this, "Could not Register Please Check Your Email!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

    }

    private void saveUserInformation(){
        String name = EditText_Name.getText().toString();
        String phone = EditText_Phone.getText().toString();
        String token = FirebaseInstanceId.getInstance().getToken();

        UserInformation userInformation = new UserInformation(name,phone,token);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        databaseReference.child(user.getUid()).setValue(userInformation).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(Register.this, "Registered Succesfully", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                else{
                    Toast.makeText(Register.this, "Could not Register Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view == button_Register){
            registerUser();

        }

    }
}
