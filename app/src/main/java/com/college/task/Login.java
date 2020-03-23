package com.college.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class Login extends AppCompatActivity {

    private EditText email;
    private EditText pass;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email = findViewById(R.id.email);
        pass = findViewById(R.id.paswd);
        Button login = findViewById(R.id.login);
        pd = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();


        findViewById(R.id.new_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Registration.class);
                startActivity(i);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em = email.getText().toString();
                String ps = pass.getText().toString();
                if (em.isEmpty() || ps.isEmpty()) {
                    Toast.makeText(Login.this, "Enter details", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(em, ps);
                }

            }
        });

        findViewById(R.id.f_passwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em = email.getText().toString();
                if (em.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter Email", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(em).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Email sent for verification", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, "Can't send Email", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });

    }


    private void signIn(String email, String password) {
        pd.setMessage("Processing...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            if (!user.isEmailVerified()) {

                                new MaterialAlertDialogBuilder(Login.this)
                                        .setMessage("Do you want to resend Verification email again?")
                                        .setTitle("Verification")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                sendEmailVerification();
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();

                                Toast.makeText(Login.this, "Email not verified\n\nCome back after verification", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent i = new Intent(Login.this, MainActivity.class);
                                startActivity(i);
                            }

                        } else {
                            Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }

                        pd.dismiss();

                    }
                });


    }

    private void sendEmailVerification() {
        pd.setMessage("Sending...");
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Login.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                        mAuth.signOut();
                        pd.dismiss();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
