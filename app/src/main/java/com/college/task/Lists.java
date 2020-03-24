package com.college.task;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Lists extends AppCompatActivity {

    private DatabaseReference uidref, boardref, cardref, due_dateref, fileref, descref, archived;
    private Button add_acttachment;
    private FirebaseStorage Storage;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser User;
    private String path;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ArrayList<cards> cardlist;
    private cardsadapter cardsadapter;
    private String boardname;
    private final String TITLE = "TITLE";
    private final String FILE = "FILE";
    private final String DUE = "DUE";
    private final String DESC = "DESC";
    private final String ARCHIVED = "ARCHIVED";
    private final String True = "true";
    private final String False = "false";
    private int archivedcount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        boardname = Objects.requireNonNull(getIntent().getStringExtra("board_name")).trim();


        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        assert User != null;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        Storage = FirebaseStorage.getInstance();
        storageRef = Storage.getReference(User.getUid());

        final TextView Archived_count = findViewById(R.id.Archived_count);
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        uidref = FirebaseDatabase.getInstance().getReference().child(User.getUid());


        DatabaseReference rootref = uidref.child(boardname);
        rootref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Getting number of items already have
                int childerncount = (int) dataSnapshot.getChildrenCount();


                cardlist = new ArrayList<>();
                if (childerncount <= 1) {
                    Toast.makeText(Lists.this, "No cards", Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String url = ds.child(FILE).getValue(String.class);
                        String due = ds.child(DUE).getValue(String.class);
                        String title = ds.child(TITLE).getValue(String.class);
                        String desc = ds.child(DESC).getValue(String.class);
                        String archived = ds.child(ARCHIVED).getValue(String.class);
                        assert archived != null;
                        //Removing null objects
                        if (title != null) {
                            //Checking if archived or not
                            if (!archived.equals(True)) {
                                cards card = new cards(url, due, title, desc);
                                cardlist.add(card);
                            } else {
                                //Archived count
                                archivedcount++;
                            }
                        }
                    }
                    Archived_count.setText("Archived (" + archivedcount + ")");
                    cardsadapter = new cardsadapter(cardlist);
                    recyclerView.setAdapter(cardsadapter);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        Archived_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (archivedcount == 0) {
                    Toast.makeText(Lists.this, "No archived found", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Lists.this, archived.class);
                    intent.putExtra("board_name", boardname);
                    startActivity(intent);
                }
            }

        });

        final MaterialDatePicker.Builder datepicker = MaterialDatePicker.Builder.datePicker();

        datepicker.setTitleText("Select Date");
        final MaterialDatePicker materialDatePicker = datepicker.build();


        findViewById(R.id.add_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog builder = new Dialog(Lists.this);
                builder.setCancelable(false);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

                builder.setContentView(R.layout.add_card);

                final int[] hours = new int[1];
                final int[] min = new int[1];
                final EditText card_name = builder.findViewById(R.id.card_name);
                final EditText description = builder.findViewById(R.id.card_desc);
                final LinearLayout datepicker = builder.findViewById(R.id.date_picker_actions);
                final TextView datetext = builder.findViewById(R.id.date);
                add_acttachment = builder.findViewById(R.id.add_attachment);
                Button done = builder.findViewById(R.id.done);
                final ImageButton cancle = builder.findViewById(R.id.cancel_button);

                cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
                //Date and time picker
                datepicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
                    }

                });
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        //Time picker
                        final String date = "Due : " + materialDatePicker.getHeaderText();
                        Calendar mcurrentTime = Calendar.getInstance();
                        final int[] hour = {mcurrentTime.get(Calendar.HOUR_OF_DAY)};
                        int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker = new TimePickerDialog(Lists.this, new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                datetext.setText(date + " " + selectedHour + ":" + selectedMinute);
                                hours[0] = selectedHour;
                                min[0] = selectedMinute;
                            }

                        }, hour[0], minute, false);
                        mTimePicker.setTitle("Select Time");
                        mTimePicker.show();

                    }
                });

                //select attachment
                add_acttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialFilePicker()
                                .withActivity(Lists.this)
                                .withRequestCode(1)
                                .withFilterDirectories(true)
                                .withHiddenFiles(true)
                                .start();
                    }
                });

                done.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        final String card_na = card_name.getText().toString().trim().toUpperCase();
                        String due_time = datetext.getText().toString();
                        final String desc = description.getText().toString().trim();
                        boardref = uidref.child(Objects.requireNonNull(boardname)).child(card_na);
                        cardref = boardref.child(TITLE);
                        due_dateref = boardref.child(DUE);
                        fileref = boardref.child(FILE);
                        descref = boardref.child(DESC);
                        archived = boardref.child(ARCHIVED);
                        if (card_na.isEmpty()) {
                            Toast.makeText(Lists.this, "Please enter card name", Toast.LENGTH_SHORT).show();
                        } else if (!due_time.contains("Due")) {
                            Toast.makeText(Lists.this, "Please select due time", Toast.LENGTH_SHORT).show();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                createnotification();
                            }
                            due_time = due_time.replace("Due : ", "").trim();
                            progressDialog.show();

                            try {
                                final Uri file = Uri.fromFile(new File(path));
                                final StorageReference storagefileref = storageRef.child(boardname + "/" + card_na + "/" + file.getLastPathSegment());
                                final UploadTask uploadTask = storagefileref.putFile(file);
                                final String finalDue_time = due_time;
                                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        progress = ((int) progress);
                                        progressDialog.setMessage("Upload is " + progress + "% done");

                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        storagefileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(final Uri uri) {
                                                fileref.setValue(uri.toString());
                                                due_dateref.setValue(finalDue_time);
                                                descref.setValue(desc);
                                                broadcast(finalDue_time);
                                                archived.setValue(False);
                                                cardref.setValue(card_na.trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        recreate();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(Lists.this, "Please select attachment", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });


                builder.show();


            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            File file = new File(Objects.requireNonNull(path));
            String strFileName = file.getName();
            add_acttachment.setText(strFileName);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Connection.isInternetAvailable(Lists.this)) {
            Snackbar.make(recyclerView, "No connection", 3000).show();
        }
    }

    //Adapter for recycler view
    public class cardsadapter extends RecyclerView.Adapter<cardsadapter.holder> {
        ArrayList<cards> cardslist;

        cardsadapter(ArrayList<cards> cardslist) {
            this.cardslist = cardslist;
        }

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards, parent, false);
            return new holder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull final holder holder, final int position) {

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
                    archived = boardref.child(ARCHIVED);
                    cardlist.remove(position);
                    notifyItemRemoved(position);
                    cardsadapter.notifyDataSetChanged();
                    Snackbar.make(recyclerView, "Archived", 3000).show();
                    archived.setValue(True).addOnSuccessListener(new OnSuccessListener<Void>() {
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
            }
        }
    }


    //Notification intent
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createnotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Task";
            String desc = "Chanel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notify", name, importance);
            channel.setDescription(desc);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

    }

    //Broadcast
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void broadcast(String finalDue_time1) {
        Intent intent = new Intent(Lists.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Lists.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Date mDate = null;
        try {
            mDate = sdf.parse(finalDue_time1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert mDate != null;
        long timeInMilliseconds = mDate.getTime();
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
    }

}
