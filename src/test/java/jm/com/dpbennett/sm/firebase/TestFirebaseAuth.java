/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jm.com.dpbennett.sm.firebase;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import com.google.cloud.firestore.annotation.DocumentId;

class User {
    @DocumentId
    private String userId; // Use @DocumentId annotation for the document ID
    private String username;
    private int age;

    // Default constructor (required by Firestore)
    public User() {
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}


/**
 *
 * @author Desmond Bennett
 */
public class TestFirebaseAuth {

    //private FirebaseAuth mAuth;

    @Test
    public void TestFirebaseAuth1() throws FileNotFoundException, IOException {
      FileInputStream serviceAccount = 
              new FileInputStream("{use system otion to get the file path}");

        FirebaseOptions options = new FirebaseOptions.Builder() // tk find out what to use instead.
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://jmts-5302b-default-rtdb.firebaseio.com") // tk use system option.
            .build();

        FirebaseApp defaultApp = FirebaseApp.initializeApp(options);
        
        // Access Firestore
        Firestore db = FirestoreClient.getFirestore(defaultApp);
        
         // Create a new User object
        User user = new User();
        user.setUserId("user123");
        user.setUsername("desh");
        user.setAge(11);

        // Get a reference to the Firestore collection and document
        DocumentReference docRef = db.collection("test").document(user.getUserId());

        // Set the object as the document data
        ApiFuture<WriteResult> result = docRef.set(user);

        System.out.println("User stored in Firestore: " + docRef.getId());
        
        // Add data to Firestore
//        DocumentReference docRef = db.collection("test").document("str");
//        String data = "{hello: Hello}";
//        ApiFuture<WriteResult> result = docRef.set(data);
//
        // Handle result
        try {
            System.out.println("Update time: " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            //e.printStackTrace();
            System.out.println("Error updating...: " + e);
        }
        
        //System.out.println("The default App: " + defaultApp.getName());
     
   }

}


