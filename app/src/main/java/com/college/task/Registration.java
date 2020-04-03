package com.college.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Registration extends AppCompatActivity {


    private EditText email;
    private EditText pass, cpass;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.paswd);
        cpass = findViewById(R.id.paswd2);
        Button create = findViewById(R.id.create);
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em = email.getText().toString();
                String ps = pass.getText().toString();
                String cps = cpass.getText().toString();
                if (em.isEmpty() || ps.isEmpty()) {
                    Toast.makeText(Registration.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                } else if (ps.length() <= 6) {
                    Toast.makeText(Registration.this, "Password must be more than 6", Toast.LENGTH_SHORT).show();
                } else {
                    if (ps.equals(cps)) {
                        createAccount(em, ps);
                    } else {
                        Toast.makeText(Registration.this, "Password not match", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Registration.this, Login.class);
                startActivity(i);
            }
        });


    }

    private void createAccount(String email, String password) {
        pd.show();
        pd.setMessage("Processing...");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Registration.this, "Authentication completed.", Toast.LENGTH_SHORT).show();
                            sendEmailVerification();
                        } else {
                            String exception = Objects.requireNonNull(task.getException()).toString();
                            if (exception.contains("badly formatted")) {
                                Toast.makeText(Registration.this, "Email address not correct.", Toast.LENGTH_SHORT).show();
                            } else if (exception.contains("already in use")) {
                                Toast.makeText(Registration.this, "Email address is already in use.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Registration.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }

                            pd.dismiss();

                        }

                    }
                });


    }

    private void sendEmailVerification() {
        pd.setMessage("Sending...");
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        mAuth.signOut();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Registration.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(Registration.this, Login.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(Registration.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }

                        pd.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
