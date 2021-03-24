package com.hayvn.hayvnapp.Interface;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Model.StoryFileCount;

//import java.time.OffsetDateTime;
import java.util.List;

import com.hayvn.hayvnapp.Constant.Constant;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface StoryDAO {
    @Query("select * from `story` where fbId IN (:fbids) ")
    Maybe<List<Story>> getByFbIdsMaybeRX(List<String> fbids);

    @Query("SELECT * FROM `story`")
    LiveData<List<Story>> getAll();

    @Query("select * from `story` where cid = :cid order by occurredAt desc")
    LiveData<List<Story>> findByCid(int cid);

    @Query("select * from `story` where cid = :cid")
    List<Story> getAllByLocalCid(int cid);

    @Query("select * from `story` where cid = :cid and "+
            "type='" + RoomConstants.STORY_TYPE_PROFILE_PHOTO +"' LIMIT 1")
    LiveData<Story> getProfilePhotoStoryCID(int cid);

    @Query("select * from `story` where updatedFirebaseAt < :firebase_upd_limit and updatedAt <= updatedFirebaseAt and type <>'"+RoomConstants.STORY_TYPE_PROFILE_PHOTO+"'")
    Maybe<List<Story>>getOldStories(long firebase_upd_limit);

    @Query("select *, " +
            "(select COUNT(*) from attachedfile as a where a.sid=b.sid) as filecount," +
            "(select COUNT(*) from attachedfile as a where a.sid=b.sid and a.type='" + Constant.IMG_FILE_TYPE + "') as imagecount,"+
            "(select COUNT(*) from attachedfile as a where a.sid=b.sid and a.type='" + Constant.AUDIO_FILE_TYPE + "') as audiocount "+
            "from story as b where (b.cid = :cid and (b.type is null OR b.type <>'"+RoomConstants.STORY_TYPE_PROFILE_PHOTO+"')) order by occurredAt desc")
    LiveData<List<StoryFileCount>> getStoriesFileCountByCid(int cid);

    @Query("select *, " +
            "(select COUNT(*) from attachedfile as a where a.sid=b.sid) as filecount," +
            "(select COUNT(*) from attachedfile as a where a.sid=b.sid and a.type='" + Constant.IMG_FILE_TYPE + "') as imagecount,"+
            "(select COUNT(*) from attachedfile as a where a.sid=b.sid and a.type='" + Constant.AUDIO_FILE_TYPE + "') as audiocount from story as b " +
            "where (b.sid IN (:filtered_sids) and (b.type is null OR b.type <>'" + RoomConstants.STORY_TYPE_PROFILE_PHOTO + "')) order by occurredAt desc")
    Maybe<List<StoryFileCount>> getStoriesFileCountBySids(List<Integer> filtered_sids);

    @Query("select * from `story` where "+
            "fbId is null or " +
            "fbId = '' or " +
            "(updatedAt < updatedFirebaseAt and updatedFirebaseAt !=0 )or " +
            "(updatedAt is null and updatedFirebaseAt is null) or "+
            "(updatedAt!=0 and updatedFirebaseAt=0)")
    List<Story> getUnsynced();

    @Query("select * from `story` where "+
            "fbId is null or " +
            "fbId = '' or " +
            "updatedAt > updatedFirebaseAt or " +
            "updatedFirebaseAt is null")
    Maybe<List<Story>> getUnsyncedMaybeRX();

    @Query("UPDATE  `story` SET cid =:this_cid WHERE fbId=:fbid") //
    Completable setCorrectCid(String fbid, int this_cid);
    @Query("UPDATE  `story` SET cid =:this_cid WHERE cid=:wrong_cid") //
    Completable setCorrectCid(int wrong_cid, int this_cid);


    @Query("select * from `story` where cid = :cid and "+
            "type='" + RoomConstants.STORY_TYPE_PROFILE_PHOTO +"'")
    Maybe<List<Story>> getProfileStories(int cid);

    @Query("select * from `story` where fbId IN (:fbids) ")
    List<Story> getByFbIds(List<String> fbids);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Story> stories);

    @Insert(onConflict = REPLACE)
    long insert(Story newStory);

    @Update(onConflict = REPLACE)
    void updatestory(Story story);

    @Delete
    void delete(Story story);

    @Query("DELETE FROM story where cid= :cid")
    void deleteByLocalCaseId(int cid);

}
