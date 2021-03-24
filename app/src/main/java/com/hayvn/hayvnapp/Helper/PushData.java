package com.hayvn.hayvnapp.Helper;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Constant.SharedprefConstants;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedCases;
import com.hayvn.hayvnapp.Interfaces.CallbackReceivedDataGeneral;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Repository.FileRepo;
import com.hayvn.hayvnapp.Repository.StoryRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PushData implements
        CallbackReceivedDataGeneral,
        CallbackReceivedCases,
        CallbackObjectUpdated {

    Application app;
    CaseRepo crepo;
    StoryRepo srepo;
    FileRepo frepo;
    List<Case> cases_ = new ArrayList<Case>();
    List<Story> stories_ = new ArrayList<Story>();
    List<Attachedfile> afiles_ = new ArrayList<Attachedfile>();
    List<Case> cases_with_fbid = new ArrayList<Case>();
    List<Story> stories_with_fbid = new ArrayList<Story>();
    List<Attachedfile> afiles_with_fbid = new ArrayList<Attachedfile>();
    CallbackReceivedDataGeneral callback_received;
    CallbackReceivedCases callback_cases;
    CallbackObjectUpdated callback_updated;
    String stage_;
    int received_cases = 0, received_stories = 0, received_files = 0;
    ArrayList<String> new_f_final = new ArrayList<>();
    ArrayList<String> removed_f_final = new ArrayList<>();
    private static final String TAG = "PUSH_DATA";

    public PushData(Application app){
        this.app = app;
        crepo = new CaseRepo(app);
        srepo = new StoryRepo(app);
        frepo = new FileRepo(app);
        callback_received = this;
        callback_updated = this;
        callback_cases = this;
        stage_ = "step1";
    }

    private void sendIntent(String val) {
        Intent intent = new Intent();
        intent.setAction(IntentConstants.SYNC_TO_FIRESTORE_ACTION);
        intent.putExtra(IntentConstants.SYNC_BROADCAST_MESSAGE, val);
        app.sendBroadcast(intent);
    }

    public void launchSync(){
        sendIntent("Clearing old data");
        srepo.deleteOldStoriesFilesLocally();
        crepo.getUnsyncedEverything(callback_received);//calls callback received
    }

    @Override
    public void callbackReceivedData(List<Case> cases_, List<Story> stories_, List<Attachedfile> afiles_) {
        // this function gets executed as a result of launchGetUnsyncXX
        if(cases_ != null){
            this.cases_ = cases_;
            received_cases = 1;
        }
        if(stories_ != null){
            this.stories_ = stories_;
            received_stories = 1;
        }
        if(afiles_ != null){
            this.afiles_ = afiles_;
            received_files = 1;
        }

        if(received_cases + received_stories + received_files == 3){
            writeCasesIntoFirebase();
        }
    }


    private void writeCasesIntoFirebase(){
        sendIntent("Sending data 1/4");
        if(cases_.size() > 0){
            for(Case c: cases_) crepo.update(c, true, callback_updated); //calls onObjectGotFireId
        }else{
            writeStoriesIntoFirebase();
        }
    }

    private void writeStoriesIntoFirebase(){
        sendIntent("Sending data 2/4");
        syncFavouriteCases();
        if(stories_.size() > 0){
            for(Story s: stories_) srepo.updateInsert(s, callback_updated, false, true);//calls onObjectGotFireId
        }else{
            writeFilesIntoFirebase();
        }
    }

    private void writeFilesIntoFirebase(){
        sendIntent("Sending data 3/4");
        if(afiles_.size() > 0){
            for(Attachedfile af: afiles_) frepo.insertUpdate(af, callback_updated, true, false);//calls onObjectGotFireId
        }else{
            sendIntent(IntentConstants.PUSH_TO_FIRESTORE_FINISHED);
        }
    }

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        //not needed here
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {
        if(obj.getCase_() != null){
            handleNewCaseFbId(obj);
        }else if(obj.getStory_() != null){
            handleNewStoryFbId(obj);
        }else if(obj.getAfile_() != null){
            handleNewAttachedfileFbId(obj);
        }
    }

    private void handleNewCaseFbId(ObjectContainer obj){
        Case case_ = obj.getCase_();
        if(case_.getFbId() != null && !case_.getFbId().equals("")){
            cases_with_fbid.add(case_);
            if(cases_with_fbid.size() == cases_.size()){
                cases_ = new ArrayList<Case>();
                updateDownstreamForCases(cases_with_fbid);
                writeStoriesIntoFirebase();
            }
        }
    }

    private void syncFavouriteCases(){
        ArrayList<String> new_f = SharedPrefHelper.getStringList(app.getBaseContext(), SharedprefConstants.FAVOURITES_LIST);
        ArrayList<String> removed_f = SharedPrefHelper.getStringList(app.getBaseContext(), SharedprefConstants.REMOVED_FROM_FAVOURITES_LIST);
        if(new_f != null) new_f_final = new ArrayList<>(new_f);
        if(removed_f != null) {
            removed_f_final = new ArrayList<>(removed_f);
            for (String st : removed_f) {
                if (new_f_final.contains(st)) {
                    new_f_final.remove(st);
                    removed_f_final.remove(st);
                }
            }
        }
        ArrayList<Integer>combo = new ArrayList<Integer>();
        for(String st: new_f_final){
            if(st!=null && !st.equals("0")){
                combo.add(Integer.parseInt(st));
            }
        }
        for(String st: removed_f_final){
            if(st!=null && !st.equals("0")){
                combo.add(Integer.parseInt(st));
            }
        }
        crepo.getCasesByCids(callback_cases, combo); //calls onCallbackReceivedCases
    }

    private void handleNewStoryFbId(ObjectContainer obj){
        Story story_ = obj.getStory_();
        if(story_.getFbId() != null && !story_.getFbId().equals("")){
            stories_with_fbid.add(story_);
            if(stories_with_fbid.size() == stories_.size()){
                stories_ = new ArrayList<Story>();
                updateDownstreamForStories(stories_with_fbid);
                writeFilesIntoFirebase();
            }
        }
    }

    private void handleNewAttachedfileFbId(ObjectContainer obj){
        Attachedfile af = obj.getAfile_();
        sendIntent("Sending files: "+String.valueOf(afiles_with_fbid.size()+1)+" out of "+String.valueOf(afiles_.size())+" files");
        if(af.getFbId() != null && !af.getFbId().equals("")){
            afiles_with_fbid.add(af);
            if(afiles_with_fbid.size() == afiles_.size()){
                afiles_ = new ArrayList<Attachedfile>();
                sendIntent(IntentConstants.PUSH_TO_FIRESTORE_FINISHED);
            }
        }
    }

    public void updateDownstreamForCases(List<Case> cases){
        int index = 0;
        for(Story s: stories_){
            Case parent = findCaseByCid(cases, s.getCid());
            if(parent != null){
                s.setCaseFbId(parent.getFbId());
                stories_.set(index, s);
            }
            index++;
        }
        index = 0;
        for(Attachedfile af: afiles_){
            Case parent = findCaseByCid(cases, af.getCid());
            if(parent != null){
                af.setCaseFbId(parent.getFbId());
                afiles_.set(index, af);
            }
            index++;
        }
    }

    private Case findCaseByCid(List<Case> cases, int cid){
        Case c1 = null;
        for(Case c: cases){
            if(c.getCid() == cid){
                c1 = c;
                break;
            }
        }
        return c1;
    }

    public void updateDownstreamForStories(List<Story> stories){
        int index = 0;
        for(Attachedfile af: afiles_){
            Story parent = findStoryBySid(stories, af.getSid());
            if(parent != null){
                af.setStoryFbId(parent.getFbId());
                afiles_.set(index, af);
            }
            index++;
        }
    }

    private Story findStoryBySid(List<Story> stories, int sid){
        Story s1 = null;
        for(Story s: stories){
            if(s.getSid() == sid){
                s1 = s;
                break;
            }
        }
        return s1;
    }

    @Override
    public void onCallbackReceivedCases(List<Case> cs) {
        HashMap<Integer, String> hm = new HashMap<>();
        for(Case c: cs){
            hm.put(c.getCid(), c.getFbId());
        }
        ArrayList<String> new_fbids = new ArrayList<>();
        ArrayList<String> removed_fbids = new ArrayList<>();
        for(String st: new_f_final){
            int a = Integer.parseInt(st);
            if(hm.get(a) != null) new_fbids.add(hm.get(a));
        }

        for(String st: removed_f_final){
            int a = Integer.parseInt(st);
            if(hm.get(a) != null) removed_fbids.add(hm.get(a));
        }
        if(new_fbids.size()>0 || removed_fbids.size()>0) crepo.storeFavourites(new_fbids, removed_fbids);
    }
}
