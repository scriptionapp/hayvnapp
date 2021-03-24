package com.hayvn.hayvnapp.Interface;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hayvn.hayvnapp.Model.Attachedfile;

import java.util.List;

import io.reactivex.Maybe;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface AttachFileDAO {

    @Query("SELECT * FROM attachedfile")
    List<Attachedfile> getAll();

    @Query("SELECT * FROM attachedfile where localStatus = :status")
    List<Attachedfile> getAllByLocalStatus(String status);

    @Query("SELECT * FROM attachedfile where sid = :sid")
    List<Attachedfile> getAllForStory(int sid);

    @Query("select * from `attachedfile` where "+
            "fbId is null or " +
            "fbId = '' or " +
            "(updatedAt < updatedFirebaseAt and updatedFirebaseAt !=0 )or " +
            "(updatedAt is null and updatedFirebaseAt is null) or "+
            "(updatedAt!=0 and updatedFirebaseAt=0)")
    List<Attachedfile> getUnsynced();

    @Query("select * from `attachedfile` where "+
            "fbId is null or " +
            "fbId = '' or " +
            "updatedAt > updatedFirebaseAt or " +
            "updatedFirebaseAt is null")
    Maybe<List<Attachedfile>> getUnsyncedMaybeRX();

    @Query("select * from attachedfile where sid= :sid")
    LiveData<List<Attachedfile>> findBySid(int sid);

    @Query("select * from `attachedfile` where fbId IN (:fbids) ")
    List<Attachedfile> getByFbIds(List<String> fbids);

    @Query("select * from `attachedfile` where sid IN (:sids) ")
    Maybe<List<Attachedfile>> getBySids(List<Integer> sids);

    @Query("SELECT * FROM attachedfile where fileName LIKE  :name")
    Attachedfile findByName(String name);

    @Insert(onConflict = REPLACE)
    long insert(Attachedfile file);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Attachedfile> files);

    @Update(onConflict = REPLACE)
    void updateAttachedFiles(Attachedfile files);

    @Delete
    void delete(Attachedfile... file_);

    @Query("DELETE FROM attachedfile where sid= :sid")
    void deleteFilesForStory(int sid);

    @Query("DELETE FROM attachedfile where fid= :fid")
    void deleteFileByFid(int fid);

}
