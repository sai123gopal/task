package com.college.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class archived extends AppCompatActivity {

    private DatabaseReference uidref, boardref, cardref, due_dateref, fileref, descref, archived;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ArrayList<cards> cardlist;
    private archivedcards cardsadapter;
    private String boardname;
    private final String TITLE = "TITLE";
    private final String FILE = "FILE";
    private final String DUE = "DUE";
    private final String DESC = "DESC";
    private final String ARCHIVED = "ARCHIVED";
    private final String False = "false";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived);

        boardname = Objects.requireNonNull(getIntent().getStringExtra("board_name")).trim();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(boardname.toUpperCase());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        uidref = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        if (Connection.isInternetAvailable(archived.this)) {
            DatabaseReference rootref = uidref.child(boardname);

            rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    setData(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(recyclerView, "" + databaseError, 4000).show();
                }
            });

        } else {
            Snackbar.make(recyclerView, "No connection", 4000).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menulist, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            super.onBackPressed();
            return (true);
        }
        return true;
    }

    public class archivedcards extends RecyclerView.Adapter<archivedcards.holder> {
        ArrayList<cards> cardslist;

        archivedcards(ArrayList<cards> cardslist) {
            this.cardslist = cardslist;
        }

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards, parent, false);
            return new holder(view);

        }


        @Override
        public void onBindViewHolder(@NonNull final archivedcards.holder holder, final int position) {

            holder.desc.setText(cardslist.get(position).getDESC());
            holder.time.setText(cardslist.get(position).getDUE());
            holder.title.setText(cardslist.get(position).getTITLE());
            holder.downloadfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cardslist.get(position).getFILE())));
                }
            });
            holder.archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    progressDialog.setMessage("Please wait..");
                    boardref = uidref.child(Objects.requireNonNull(boardname)).child(cardslist.get(position).getTITLE());
                    cardref = boardref.child(TITLE);
                    due_dateref = boardref.child(DUE);
                    fileref = boardref.child(FILE);
                    descref = boardref.child(DESC);
                    archived = boardref.child(ARCHIVED);
                    cardlist.remove(position);
                    notifyItemRemoved(position);
                    cardsadapter.notifyDataSetChanged();
                    archived.setValue(False).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            recreate();
                        }
                    });
                }
            });


        }

        @Override
        public int getItemCount() {
            return cardslist.size();
        }

        class holder extends RecyclerView.ViewHolder {
            TextView desc, time, title;
            ImageButton archive;
            LinearLayout downloadfile;

            holder(@NonNull View itemView) {
                super(itemView);
                desc = itemView.findViewById(R.id.desc);
                time = itemView.findViewById(R.id.due);
                title = itemView.findViewById(R.id.title);
                archive = itemView.findViewById(R.id.archive);
                downloadfile = itemView.findViewById(R.id.download);
                archive.setImageResource(R.drawable.ic_unarchive_black_24dp);
            }
        }
    }

    private void setData(DataSnapshot dataSnapshot) {
        int childerncount = (int) dataSnapshot.getChildrenCount();
        cardlist = new ArrayList<>();
        if (childerncount <= 1) {
            Toast.makeText(archived.this, "No cards", Toast.LENGTH_SHORT).show();
        } else {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                String url = ds.child(FILE).getValue(String.class);
                String due = ds.child(DUE).getValue(String.class);
                String title = ds.child(TITLE).getValue(String.class);
                String desc = ds.child(DESC).getValue(String.class);
                String archived = ds.child(ARCHIVED).getValue(String.class);
                assert archived != null;
                if (title != null) {
                    String aTrue = "true";
                    if (archived.equals(aTrue)) {
                        cards card = new cards(url, due, title, desc);
                        cardlist.add(card);
                    }
                }
            }
            cardsadapter = new archivedcards(cardlist);
            recyclerView.setAdapter(cardsadapter);
        }
    }
}
