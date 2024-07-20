package com.example.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chat.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
        binding.buttonSignIn.setOnClickListener(view -> {
            addDataToFirestore();
        });
    }

    private void addDataToFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a new user with a first and last name
            Map<String, Object> user = new HashMap<>();
            user.put("name", "Dhiraj");
            user.put("email", "dhirajgovindvira@gmail.com");
            user.put("time", new Date());

            try {
                Log.d("Name", "" + Objects.requireNonNull(user.get("name")));
                Log.d("Email", "" + Objects.requireNonNull(user.get("email")));
                Log.d("Time", "" + Objects.requireNonNull(user.get("time")));
            }
            catch (Exception e) {
                Log.d("ExceptionHere", String.valueOf(e));
            }
            // Add a new document with a generated ID
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(documentReference -> Log.d("Adding to Firestore", "DocumentSnapshot added with ID: " + documentReference.getId()))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Adding to Firestore Error", "Error adding document", e);
                        }
                    });

            Toast.makeText(getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();

            db.collection("users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Data", document.getId() + " => " + document.getData());
                                Log.d("Name", Objects.requireNonNull(document.getString("name")));
                                Log.d("Email", Objects.requireNonNull(document.getString("email")));
                                Log.d("Date", Objects.requireNonNull(document.getString("date")));
                            }
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                        }
                    });

        }
        catch (Exception e) {
            Log.d("Exception", String.valueOf(e));
        }
    }
}
