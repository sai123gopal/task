package com.college.task;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference uereidref;
    private ListView board_name;
    List<String> key;
    ArrayAdapter<String> arrayAdapter;
    private ProgressDialog progressDialog;
    private String board_name_text;
    private boolean isfirst;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        uereidref = databaseReference.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        board_name = findViewById(R.id.list);

        ImageView userimageview = findViewById(R.id.profile);
        Picasso.get().load(mAuth.getCurrentUser().getPhotoUrl()).into(userimageview);

        findViewById(R.id.profile_card).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        uereidref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                key = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() != 0) {
                    isfirst = false;
                    progressDialog.dismiss();
                    String names;
                    Map<String, Object> users = (Map<String, Object>) dataSnapshot.getValue();
                    for (Map.Entry<String, Object> entry : Objects.requireNonNull(users).entrySet()) {
                        names = entry.getKey();
                        key.add(names);
                    }
                } else {
                    isfirst = true;
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "No boards", Toast.LENGTH_SHORT).show();
                }

                arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.board_list, R.id.textView, key);
                board_name.setAdapter(arrayAdapter);
                board_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String board_name = key.get(position);
                        Intent intent = new Intent(MainActivity.this, Lists.class);
                        intent.putExtra("board_name", board_name);
                        startActivity(intent);

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        findViewById(R.id.add_board).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Connection.isInternetAvailable(MainActivity.this)) {
            Snackbar.make(board_name, "No connection", 3000).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();

    }


    private void update(final String type, final String board_name_text) {
        progressDialog.show();
        DatabaseReference boardref = uereidref.child(board_name_text);
        DatabaseReference boardtyperef = boardref.child("BOARD TYPE");
        boardtyperef.setValue(type).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    isfirst = false;
                    key.add(board_name_text);
                    arrayAdapter.notifyDataSetChanged();
                    board_name.invalidate();
                    board_name.refreshDrawableState();
                } else {
                    Toast.makeText(MainActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });

    }

    private boolean checkname(String name) {
        if (!isfirst) {
            for (int i = 0; i < key.size(); i++) {
                if (name.equals(key.get(i))) {
                    return true;
                }
            }

        }
        return false;
    }

    private void showDialog() {
        final Dialog builder = new Dialog(MainActivity.this);
        builder.setCancelable(false);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.setContentView(R.layout.board);
        final EditText ed = builder.findViewById(R.id.board_name);
        Button submit = builder.findViewById(R.id.submit);
        ImageButton cancel = builder.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                board_name_text = ed.getText().toString().trim();
                if (board_name_text.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter board name", Toast.LENGTH_SHORT).show();
                } else if (board_name_text.length() < 3) {
                    Toast.makeText(MainActivity.this, "Board name should at least 3 letters", Toast.LENGTH_SHORT).show();
                } else if (checkname(board_name_text)) {
                    Toast.makeText(MainActivity.this, "Board name is already exist", Toast.LENGTH_SHORT).show();
                } else {
                    builder.dismiss();
                    String[] winner = {"PERSONAL", "TEAM"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Select type");
                    builder.setCancelable(false);
                    builder.setItems(winner, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (which == 0) {
                                update("PERSONAL", board_name_text);
                            } else if (which == 1) {
                                update("TEAM", board_name_text);
                            }

                        }
                    });
                    builder.show();
                }
            }


        });
        builder.show();
    }

    private void signOut() {
        Snackbar.make(board_name, "SIGN OUT", 5000)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth.signOut();
                        Intent i = new Intent(MainActivity.this, home.class);
                        startActivity(i);
                    }
                })
                .setActionTextColor(Color.YELLOW)
                .show();
    }

}


