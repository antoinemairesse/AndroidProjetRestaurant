package com.example.restaurants.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FirebaseUtils {

    // Utility method to extract documents from Firestore Task
    public static List<DocumentSnapshot> getDocumentsFromTask(Context context, @NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            return task.getResult().getDocuments();
        } else {
            Toast.makeText(context, "Error while fetching data, please try again", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

}
