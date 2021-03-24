package com.hayvn.hayvnapp.FirebaseHelper;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hayvn.hayvnapp.Constant.FirebaseConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserCounter {

    public class Counter {
        public int numShards;
        public Counter(int numShards) {
            this.numShards = numShards;
        }
    }

    public Task<Void> incrementCounter(final FirebaseFirestore mFirestore, final int num_shards) {
        int shardId = (int) Math.floor(Math.random() * num_shards);
        DocumentReference shardRef = mFirestore
                                .collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                                .document(FirebaseConstants.USERS_DIST_COUNTER)
                                .collection(FirebaseConstants.SHARDS_COLLECTION_PATH)
                                .document(String.valueOf(shardId));
        return shardRef.update(FirebaseConstants.SHARDS_FIELD_COUNT, FieldValue.increment(1));
    }

    public Task<Integer> getCount(final FirebaseFirestore mFirestore) {
        // Sum the count of each shard in the subcollection
        return mFirestore.collection(FirebaseConstants.SHARED_COLLECTION_PATH)
                .document(FirebaseConstants.USERS_DIST_COUNTER)
                .collection(FirebaseConstants.SHARDS_COLLECTION_PATH)
                .get()
                .continueWith(new Continuation<QuerySnapshot, Integer>() {
                    @Override
                    public Integer then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        int count = 0;
                        for (DocumentSnapshot snap : Objects.requireNonNull(task.getResult())) {
                            FirestoreShard shard = snap.toObject(FirestoreShard.class);
                            count += shard.getCount();
                        }
                        return count;
                    }
            });
    }
}
