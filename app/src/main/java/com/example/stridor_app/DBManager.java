package com.example.stridor_app;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;


public final class DBManager {

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static String iid;
    private static DatabaseReference usersRef;
    private static DatabaseReference recordingsRef;
    private static DatabaseReference persistUsersRef;
    private static DatabaseReference persistRecordingsRef;

    private DBManager() {
    }

    public static void insertUser(String NAME, String MOBILE, String GENDER, String DOB, int HEIGHT, int WEIGHT, String ILLNESS) {
        String uID = UUID.randomUUID().toString().replace("-", "");
        HashMap<String,Object> upd = new HashMap<>();
        HashMap<String, Object> contentValue = new HashMap<>();
        contentValue.put("NAME", NAME);
        contentValue.put("MOBILE", MOBILE);
        contentValue.put("GENDER", GENDER);
        contentValue.put("DOB", DOB);
        contentValue.put("HEIGHT", HEIGHT);
        contentValue.put("WEIGHT", WEIGHT);
        contentValue.put("ILLNESS", ILLNESS);
        upd.put(uID,contentValue);
        usersRef.updateChildren(upd);
        persistUsersRef.updateChildren(upd);
    }

    public static void insertRecording(String UID,String time_stamp,String MEDIA, String PATH, String TASK, String duration, String rec_id) {
        recordingsRef = database.getReference("Recordings").child(iid + "/" + UID);
        recordingsRef.keepSynced(true);
        persistRecordingsRef = database.getReference("persistRecordings").child(iid + "/" + UID);

        HashMap<String,Object> upd = new HashMap<>();
        HashMap<String, Object> contentValue = new HashMap<>();
        contentValue.put("MEDIA", MEDIA);
        contentValue.put("PATH", PATH);
        contentValue.put("TASK", TASK);
        contentValue.put("DURATION", duration);
        contentValue.put("REC_ID", rec_id);
        upd.put(time_stamp,contentValue);
        recordingsRef.updateChildren(upd);
        persistRecordingsRef.updateChildren(upd);

        uploadFile(UID,PATH,time_stamp,MEDIA,TASK);
    }

    public interface DataSnapshotCallback {
        void onSuccess(DataSnapshot value);
    }

    public static void fetchUsers(DataSnapshotCallback callback) {
        FirebaseInstallations.getInstance().getId().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    iid = task.getResult();
                    usersRef = database.getReference("Users").child(iid);
                    persistUsersRef = database.getReference("persistUsers").child(iid);
                    usersRef.keepSynced(true);
                    ValueEventListener users_listener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            callback.onSuccess(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("Error", "loadPost:onCancelled", databaseError.toException());
                        }
                    };
                    usersRef.addValueEventListener(users_listener);
                }
            }
        });
    }

    public static void fetchUser(String _id,DataSnapshotCallback callback) {
        DatabaseReference userRef = usersRef.child(_id);
        ValueEventListener user_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Error", "loadPost:onCancelled", databaseError.toException());
            }
        };
        userRef.addListenerForSingleValueEvent(user_listener);
    }


    public static void fetchRecordings(String _id,DataSnapshotCallback callback) {
        recordingsRef = database.getReference("Recordings/" + iid + "/" + _id);
        recordingsRef.keepSynced(true);

        ValueEventListener recordings_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Error", "loadPost:onCancelled", databaseError.toException());
            }
        };
        recordingsRef.addValueEventListener(recordings_listener);
    }


    public static void deleteUser(String _id) {
        usersRef.child(_id).setValue(null);
        recordingsRef = database.getReference("Recordings").child(iid).child(_id);
        recordingsRef.setValue(null);
    }

    public static void deleteRecording(String _id, String rec_name) {
        recordingsRef = database.getReference("Recordings").child(iid).child(_id);
        recordingsRef.child(rec_name).setValue(null);
    }

    public static void uploadFile(String uid, String file_path,String time_stamp,String MEDIA, String TASK){
        StorageReference ref = FirebaseStorage.getInstance().getReference();

        Uri uri = Uri.fromFile(new File(file_path));
        if (uri != null) {
            String ext = FilenameUtils.getExtension(file_path);
            StorageReference storageRef = ref.child(iid + "/"+ uid + "/" + TASK + "/" + MEDIA).child(time_stamp + "." + ext);

            storageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Uploaded recording","saved");
                }
            });
        }
    }

}