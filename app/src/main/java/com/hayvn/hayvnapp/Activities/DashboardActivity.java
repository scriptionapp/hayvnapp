package com.hayvn.hayvnapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hayvn.hayvnapp.Adapter.CommentsAdapter;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Interfaces.CallbackLocalStories;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hayvn.hayvnapp.Repository.StoryRepo;
import com.hayvn.hayvnapp.Utilities.MapComparator;
import com.hayvn.hayvnapp.databinding.DrawerDashboardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.Constant.MODIFY_STORY;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.CID;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.COMMENTS;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.SID;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.TO_USER_ID;
import static com.hayvn.hayvnapp.Constant.FirebaseConstants.UPDATED_AT;
import static com.hayvn.hayvnapp.Constant.IntentConstants.STORY_TO_MODIFY;
import static com.hayvn.hayvnapp.Constant.TextConstants.TITLE_DASHBOARD;

public class DashboardActivity extends BaseParentActivity implements CallbackLocalStories {

    private final String TAG = "DASHBOARD";
    Context context;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    List<Map> all_comments = new ArrayList<Map>();
    List<Map<String, String>> comments_to_display = new ArrayList<Map<String, String>>();

    CommentsAdapter adapter;
    StoryRepo s_repo;
    List<Integer> sids = new ArrayList<Integer>();
    List<StoryFileCount> sfc_list = new ArrayList<StoryFileCount>();
    List<StoryFileCount> sfc_list_alloc = new ArrayList<StoryFileCount>();
    private CallbackLocalStories callback_comments;
    private DrawerDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DrawerDashboardBinding.inflate(getLayoutInflater());
        View views = binding.getRoot();
        setContentView(views);
        context = this;
        callback_comments = this;
        s_repo  = new StoryRepo(getApplication());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(TITLE_DASHBOARD);
        adapter = new CommentsAdapter(context);
        binding.mComments.setAdapter(adapter);

        binding.mComments.setOnItemClickListener((parent, view, position, id) -> {
            Intent myIntent = new Intent(getBaseContext(), NewStoryActivity.class); //getBaseContext
            Map<String, String> map = (Map<String, String>)adapter.getItem(position);
            int sid = Integer.parseInt(Objects.requireNonNull(map.get(CID)));
            int cid = Integer.parseInt(Objects.requireNonNull(map.get(CID)));
            myIntent.putExtra(STORY_TO_MODIFY, findSFCBySidCid(cid, sid, sfc_list, false)
                    .getStory());
            startActivityForResult(myIntent, MODIFY_STORY);
        });

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(COMMENTS)
                .whereEqualTo(TO_USER_ID, Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map comment;
                            HashMap<String, String> comment_hash;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                comment = document.getData();
                                all_comments.add(comment);
                                sids.add(Integer.parseInt(Objects.requireNonNull(comment.get(SID)).toString()));
                            }
                            Collections.sort(all_comments, new MapComparator(CID,SID,UPDATED_AT));
                            s_repo.getAllStoriesByQuery(
                                    sids,
                                    callback_comments,
                                    null,
                                    RoomConstants.QUERY_STORY_FILE_BY_SID,
                                    null);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, getString(R.string.internet_problem), Toast.LENGTH_LONG);
                    }
                });
    }


    public void callbackGetLocalStories(List<StoryFileCount> result, List<Story> result2) {
        sfc_list = result;
        sfc_list_alloc = new ArrayList<StoryFileCount>(result);
        comments_to_display = new ArrayList<Map<String, String>>();
        Map map; //sid title
        Map <String, String> hm;
        for (int i=0; i<all_comments.size();i++) {
            map = all_comments.get(i);
            String txt = Objects.requireNonNull(map.get("text")).toString();
            String upd = Objects.requireNonNull(map.get(UPDATED_AT)).toString();
            int sid = Integer.parseInt(Objects.requireNonNull(map.get(SID)).toString());
            int cid = Integer.parseInt(Objects.requireNonNull(map.get(CID)).toString());
            StoryFileCount sfc = findSFCBySidCid(cid, sid, sfc_list_alloc, true);
            if(sfc==null){
                hm = new HashMap<String, String>();
                hm.put("file_count", "0");
                hm.put("story_title", "");
                hm.put(UPDATED_AT, upd);
                hm.put("text", txt);
                hm.put(SID, String.valueOf(sid));
                hm.put(CID, String.valueOf(cid));
                comments_to_display.add(hm);
            }else{
                hm = new HashMap<String, String>();
                hm.put("file_count", String.valueOf(sfc.getFilecount()));
                hm.put("story_title", sfc.getStory().getTitle());
                hm.put(UPDATED_AT, "");
                hm.put("text", "");
                hm.put(SID, String.valueOf(sid));
                hm.put(CID, String.valueOf(cid));
                comments_to_display.add(hm);

                hm = new HashMap<String, String>();
                hm.put("file_count", "0");
                hm.put("story_title", "");
                hm.put(UPDATED_AT, upd);
                hm.put("text", txt);
                hm.put(SID, String.valueOf(sid));
                hm.put(CID, String.valueOf(cid));
                comments_to_display.add(hm);
            }
        }
        adapter.setList(comments_to_display);
    }

    private StoryFileCount findSFCBySidCid(int cid, int sid, List<StoryFileCount> search_in, boolean to_delete){
        StoryFileCount sfc = null;
        for(StoryFileCount s : search_in){
            if(s.getStory().getSid()==sid && s.getStory().getCid()==cid){
                sfc = s;
                if(to_delete) search_in.remove(s);
                break;
            }
        }
        return sfc;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_close_view) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
