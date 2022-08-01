package com.example.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.chat.ChatAdapter;
import com.example.chatapp.chat.ChatList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends Activity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chatapp-650ad-default-rtdb.firebaseio.com/");

    private final List<ChatList> chatLists = new ArrayList<>();
    private String chatKey;
    String getUserMobile = "";
    private RecyclerView chattingRecyclerView;
    private ChatAdapter chatAdapter;
    private boolean loadingFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final ImageView backBtn = findViewById(R.id.backBtn);
        final TextView nameTV = findViewById(R.id.name);
        final EditText messageEditTxt = findViewById(R.id.messageEditTxt);
        final CircleImageView profilePic = findViewById(R.id.profilePic);
        final ImageView sendBtn = findViewById(R.id.sendBtn);

        chattingRecyclerView = findViewById(R.id.chattingRecyclerView);

        //get data from messages adapter class
        final String getName = getIntent().getStringExtra("name");
        final String getProfilePic = getIntent().getStringExtra("profile_pic");
        chatKey = getIntent().getStringExtra("chat_key");
        final String getMobile = getIntent().getStringExtra("mobile");

        //get user mobile from memory
        getUserMobile = MemoryData.getData(Chat.this);
        nameTV.setText(getName);
        Picasso.get().load(getProfilePic).into(profilePic);


        chattingRecyclerView.setHasFixedSize(true);
        chattingRecyclerView.setLayoutManager(new LinearLayoutManager(Chat.this));

        chatAdapter = new ChatAdapter(chatLists, Chat.this);
        chattingRecyclerView.setAdapter(chatAdapter);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (chatKey.isEmpty()) {
                    //generate chat key. By default chatKey is 1
                    chatKey = "1";

                    if (snapshot.hasChild("chat")) {
                        chatKey = String.valueOf(snapshot.child("chat").getChildrenCount() + 1);

                    }
                }

                if (snapshot.hasChild("chat")){
                    chatLists.clear();
                    if (snapshot.child("chat").child(chatKey).hasChild("messages")){
                        for (DataSnapshot messagesSnapshot:snapshot.child("chat").child(chatKey).child("messages").getChildren()){

                            if (messagesSnapshot.hasChild("msg") && messagesSnapshot.hasChild("mobile")){
                                final String messagesTimestamps = messagesSnapshot.getKey();
                                final String getMobile =messagesSnapshot.child("mobile").getValue(String.class);
                                final String getMsg =messagesSnapshot.child("msg").getValue(String.class);

                                Timestamp timestamp = new Timestamp(Long.parseLong(messagesTimestamps));
                                Date date = new Date(timestamp.getTime());
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy", Locale.getDefault());
                                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
                                ChatList chatList = new ChatList(getMobile, getName, getMsg, simpleDateFormat.format(date), simpleTimeFormat.format(date));
                                chatLists.add(chatList);

                                if (loadingFirstTime || Long.parseLong(messagesTimestamps) > Long.parseLong(MemoryData.getLastMgsTS(Chat.this, chatKey))){

                                    loadingFirstTime = false;
                                    MemoryData.saveLastMgsTS(messagesTimestamps, chatKey, Chat.this);
                                    chatAdapter.updateChatList(chatLists);

                                    chattingRecyclerView.scrollToPosition(chatLists.size()-1);
                                }
                            }

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String getTxtMessage = messageEditTxt.getText().toString();

                // get current timestamps
                final String currentTimestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);


                databaseReference.child("chat").child(chatKey).child("user_1").setValue(getUserMobile);
                databaseReference.child("chat").child(chatKey).child("user_2").setValue(getMobile);
                databaseReference.child("chat").child(chatKey).child("messages").child(currentTimestamp).child("msg").setValue(getTxtMessage);
                databaseReference.child("chat").child(chatKey).child("messages").child(currentTimestamp).child("mobile").setValue(getUserMobile);

                //clear edit text
                messageEditTxt.setText("");
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

