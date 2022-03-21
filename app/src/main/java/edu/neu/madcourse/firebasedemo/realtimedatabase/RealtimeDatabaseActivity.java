package edu.neu.madcourse.firebasedemo.realtimedatabase;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import edu.neu.madcourse.firebasedemo.R;
import edu.neu.madcourse.firebasedemo.realtimedatabase.models.User;

public class RealtimeDatabaseActivity extends AppCompatActivity {

    private static final String TAG = RealtimeDatabaseActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private TextView user1;
    private TextView score_user1;
    private TextView user2;
    private TextView score_user2;
    private RadioButton player;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_database);

        user1 = (TextView) findViewById(R.id.username1);
        user2 = (TextView) findViewById(R.id.username2);
        score_user1 = (TextView) findViewById(R.id.score1);
        score_user2 = (TextView) findViewById(R.id.score2);
        player = (RadioButton) findViewById(R.id.player1);

        // Connect with firebase
        //
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Update the score in realtime
        mDatabase.child("users").addChildEventListener(
                new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        showScore(dataSnapshot);
                        Log.e(TAG, "onChildAdded: dataSnapshot = " + dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        showScore(dataSnapshot);
                        Log.v(TAG, "onChildChanged: " + dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled:" + databaseError);
                        Toast.makeText(getApplicationContext()
                                , "DBError: " + databaseError, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Add 5 points Button
    public void addFivePoints(View view) {
        RealtimeDatabaseActivity.this.onAddScore(mDatabase, player.isChecked() ? "user1" : "user2");
    }

    // Reset USERS Button
    public void resetUsers(View view) {

        User user;
        user = new User("user1", "0");
        Task t1 = mDatabase.child("users").child(user.username).setValue(user);

        user = new User("user2", "0");
        Task t2 = mDatabase.child("users").child(user.username).setValue(user);

        if(!t1.isSuccessful() && !t2.isSuccessful()){
            Toast.makeText(getApplicationContext(),"Unable to reset players!",Toast.LENGTH_SHORT).show();
        }
        else if(!t1.isSuccessful() && t2.isSuccessful()){
            Toast.makeText(getApplicationContext(),"Unable to reset player1!",Toast.LENGTH_SHORT).show();
        }
        else if(t1.isSuccessful() && t2.isSuccessful()){
            Toast.makeText(getApplicationContext(),"Unable to reset player2!",Toast.LENGTH_SHORT).show();
        }


    }

    // Add data to firebase button
    public void doAddDataToDb(View view) {
        // Write a message to the database
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("message");

        Task t = myRef.setValue("Hello, World!");
        if(!t.isSuccessful()){
            Toast.makeText(getApplicationContext()
                    , "Failed to write value into firebase. " , Toast.LENGTH_SHORT).show();
            return;
        }

        // Read from the database by listening for a change to that item.
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
                TextView tv = (TextView) findViewById(R.id.dataUpdateTextView);
                tv.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                Toast.makeText(getApplicationContext()
                        , "Failed to write value into firebase. " , Toast.LENGTH_SHORT).show();
            }

        });

    }


    /**
     * Called on score_user1 add
     *
     * @param postRef
     * @param user
     */
    private void onAddScore(DatabaseReference postRef, String user) {
        postRef
                .child("users")
                .child(user)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {

                        User user = mutableData.getValue(User.class);
                        if (user == null) {
                            return Transaction.success(mutableData);
                        }

                        user.score = String.valueOf(Integer.valueOf(user.score) + 5);

                        mutableData.setValue(user);
                        int i =0 ;

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        // Transaction completed
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                        Toast.makeText(getApplicationContext()
                                , "DBError: " + databaseError, Toast.LENGTH_SHORT).show();
                    }



                });
    }


    private void showScore(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);

        if (dataSnapshot.getKey().equalsIgnoreCase("user1")) {
            score_user1.setText(String.valueOf(user.score));
            user1.setText(user.username);
        } else {
            score_user2.setText(String.valueOf(user.score));
            user2.setText(user.username);
        }
    }


}
