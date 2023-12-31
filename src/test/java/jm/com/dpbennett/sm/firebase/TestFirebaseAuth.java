/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jm.com.dpbennett.sm.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett
 */
public class TestFirebaseAuth {

    //private FirebaseAuth mAuth;

    @Test
    public void TestFirebaseAuth1() throws FileNotFoundException, IOException {
      FileInputStream serviceAccount = 
              new FileInputStream("C:\\Projects\\system-management-lib\\google-services.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://jmts-5302b-default-rtdb.firebaseio.com")
            .build();

        FirebaseApp.initializeApp(options);
        
     
   }

}
