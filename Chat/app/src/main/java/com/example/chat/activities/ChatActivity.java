package com.example.chat.activities;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import com.infinityandroid.chatapp.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.R;
import com.example.chat.databinding.ActivityChat1Binding;
import com.example.chat.models.ChatMessage;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChat1Binding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChat1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmspFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );

        binding.chatRecyclerView.setAdapter(ChatAdapter);
        database = FirebaseFirestore.getInstance();
    }
     private void sendMessage() {
         HashMap<String, Object> message = new HashMap<>();
         message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
         message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
         message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
         message.put(Constants.KEY_TEMESTAMP, new Date());
         database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
         binding.inputMessage.setText(null);
     }
     private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

     }
     private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
         if (error != null) {
             return;
         }
         if (value != null) {
             int count = ChatMessage.size();
             for (DocumentChange documentChange : value.getDocumentChanges()) {
                 if (documentChange.getType() == DocumentChange.Type.ADDED) {
                     ChatMessage chatMessage = new ChatMessage();
                     ChatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                     ChatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                     chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                     ChatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                     chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                     chatMessage.add(chatMessage);
                 }
             }
             Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
             if (count == 0) {
                 chatAdapter.notifyDataSetChanged();
             } else {
                 chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                 binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() -1);
             }
                   binding.chatReclerView.setVisibility(View.VISIBLE);
         }
         binding.progressBar.setVisibility(View.GONE);
     };
    private Object getBitmspFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decoded(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.Decoder(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private void loadReceiverDetails() {
        User receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}