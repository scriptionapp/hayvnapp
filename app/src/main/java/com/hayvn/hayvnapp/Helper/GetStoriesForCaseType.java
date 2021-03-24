package com.hayvn.hayvnapp.Helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Repository.StoryRepo;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.FirebaseConstants.SHARED_COLLECTION_PATH;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.SHARED_DATA_PATH;

/*This loads certain templates
* temporarily disabled
* */
public class GetStoriesForCaseType implements CallbackObjectUpdated {
    private ArrayList<String> spinnerItems;
    private CallbackObjectUpdated callback_updated;
    private final static String TAG = "PULL_CREATE_CASE";

    public GetStoriesForCaseType(){
        this.callback_updated = this;
    }
    public ArrayAdapter<String> pullDataForSpinner(Activity context, FirebaseFirestore firestore) {
        spinnerItems = new ArrayList<>();
        ArrayAdapter<String> case_type_adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerItems);
        case_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        firestore
                .collection(SHARED_COLLECTION_PATH)
                .document(SHARED_DATA_PATH)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    if(doc.exists()){
                        ArrayList<String> caseTypes = (ArrayList<String>) Objects.requireNonNull(doc.getData())
                                                                            .get("caseTypes");
                        assert caseTypes != null;
                        spinnerItems.addAll(caseTypes);
                        case_type_adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return case_type_adapter;
    }

    private Story createStoryByTitle(String title, Case newCase, String caseType, StoryRepo storyRepo){
        Story story = new Story();
        story.setCid(newCase.getCid());
        story.setCaseFbId(newCase.getFbId());
        story.setUserId(newCase.getUserId());
        story.setTypeEntry(caseType);
        story.setTitle(title);
        story = storyRepo.setUpdateCreate(story, true, true);
        return story;
    }

    public void pullAndCreateCaseStories(Activity context, FirebaseFirestore firestore, Case newCase, String caseType, StoryRepo storyRepo) {
        DocumentReference docRef = firestore.collection("shared").document("data");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    if (doc.exists()) {
                        Map<String, Object> caseTypeMap = (Map<String, Object>) Objects.requireNonNull(doc.getData()).get("caseTypeMap");
                        switch (caseType) {
                            case "Unfair Dismissal": {
                                assert caseTypeMap != null;
                                ArrayList<String> questions = (ArrayList<String>) caseTypeMap.get(caseType);
                                for (String title: questions) {
                                    Story story = createStoryByTitle(title, newCase, caseType, storyRepo);
                                    storyRepo.updateInsert(story, callback_updated, false, true); //.insertWithId(story, context, "main");
                                }
                            }
                            break;
                            case "Workplace Issue":
                                //TODO Logic Here
                                break;
                            case "Other":
                                //TODO Logic Here also
                                break;

                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        //do nothing
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {
        //do nothing
    }
}
