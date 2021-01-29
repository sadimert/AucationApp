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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText EditText_Email;
    private EditText EditText_Password;
    private Button button_Login;
    private TextView TextView_RegisterPageLink;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            finish();
            Intent intent = new Intent(Login.this, MainActivity.class);
            Login.this.startActivity(intent);
        }

        progressDialog = new ProgressDialog(this);

        EditText_Email = (EditText) findViewById(R.id.EditText_Email);
        EditText_Password = (EditText) findViewById(R.id.EditText_Password);
        button_Login = (Button) findViewById(R.id.button_Login);
        TextView_RegisterPageLink = (TextView) findViewById(R.id.TextView_RegisterPageLink);

        button_Login.setOnClickListener(this);
        TextView_RegisterPageLink.setOnClickListener(this);
    }

    private void userLogin(){
        String email = EditText_Email.getText().toString();
        String password = EditText_Password.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if (task.isSuccessful()){
                    finish();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    Login.this.startActivity(intent);
                }
                else{
                    Toast.makeText(Login.this, "Could not Login Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view == button_Login){
            userLogin();
        }
        else if (view == TextView_RegisterPageLink ){
            startActivity(new Intent(this,Register.class));
        }
    }
}
