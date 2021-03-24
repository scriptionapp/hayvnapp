package com.hayvn.hayvnapp.Interface;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.CaseStoryFileCount;
import com.hayvn.hayvnapp.Model.CompoundStringInt;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface CaseNamesDAO {

    @Query("SELECT * FROM `case`")
    LiveData<List<Case>> getAll();

    @Query("select * from `case`")
    Maybe<List<Case>> getAllMaybeRX();

    @Query("select * from `case` where cid= :cid")
    LiveData<Case> findById(int cid);

    @Query("select * from `case` where cid= (select max(cid) from `Case`) LIMIT 1")
    LiveData<Case> findLatestCase();

    @Query("SELECT * FROM `case` where name LIKE  :name")
    Case findByName(String name);

    @Query("select * from `case` where " +
            "fbId is null or " +
            "fbId = '' or " +
            "(updatedAt < updatedFirebaseAt and updatedFirebaseAt <> 0 ) or " +
            "(updatedAt is null and updatedFirebaseAt is null) or "+
            "(updatedAt <> 0 and updatedFirebaseAt=0)")
    List<Case> getUnsynced();

    @Query("select * from `case` where " +
            "fbId is null or " +
            "fbId = '' or " +
            "updatedAt > updatedFirebaseAt or " +
            "updatedFirebaseAt is null")
    Maybe<List<Case>> getUnsyncedMaybeRX();

    @Query("select * from `case` where cid IN (:cids) ")
    Maybe<List<Case>>getByCidsMaybeRX(List<Integer> cids);

    @Query("select * from `case` where fbId IN (:fbids) ")
    List<Case> getByFbIds(List<String> fbids);

    @Query("select doctorId name, " +
            "(select COUNT(cid) count from `case` where userId = :uid) as counter " +
            "from `user`")
    CompoundStringInt getCountForUserDoctorId(String uid);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Case> cases);

    @Insert(onConflict = REPLACE)
    long insertCase(Case newCase);

    @Update(onConflict = REPLACE)
    void updateCaseNames(Case caseName);

    @Query("UPDATE  `case`" +
            "SET stringThree = '" +RoomConstants.IS_FAVOURITE_ED+"' "+
            "WHERE\n" +
            "  fbId IN (:fbids)") //
    Completable updateFavourites(ArrayList<String> fbids);

    @Query("delete from  `case` WHERE cid=:cid") //
    Completable deleteDuplicate(int cid);

    @Delete
    void delete(Case caseName);

    @Query(
            "SELECT *," +
                    "(select COUNT(*) from Story as a where a.cid=case1.cid and (a.type is null OR a.type <>'" +RoomConstants.STORY_TYPE_PROFILE_PHOTO+ "')) as storycount, " +
                    "(select COUNT(*) from attachedfile as file1 where file1.cid=case1.cid and ((select type from Story where sid=file1.sid) is null OR (select type from Story where sid=file1.sid) <>'"+RoomConstants.STORY_TYPE_PROFILE_PHOTO+"')   ) as filecount " +
                    "FROM `case` "+
                    "as case1 "+
                    "WHERE  :quer "+
                    "order by Datetime(updatedAt) desc"
    )
    LiveData<List<CaseStoryFileCount>> getCasesCountsForCaseFilt(String quer);

    @RawQuery(observedEntities = Case.class)
    LiveData<List<CaseStoryFileCount>> getUserViaQuery(SimpleSQLiteQuery query);

    @Query(
            "select * ," +
                    "(select COUNT(*) from Story as a where a.cid=case1.cid and (a.type is null OR a.type <>'" +RoomConstants.STORY_TYPE_PROFILE_PHOTO+ "')) as storycount, " +
                    "(select COUNT(*) from attachedfile as file1 where file1.cid=case1.cid and ((select type from Story where sid=file1.sid) is null OR (select type from Story where sid=file1.sid) <>'"+RoomConstants.STORY_TYPE_PROFILE_PHOTO+"')   ) as filecount " +
                    " from `case`  "+ //(SELECT * FROM `case` WHERE name LIKE :filt or patientId LIKE :filt )
                    "as case1 "+
                    "WHERE stringThree='" + RoomConstants.IS_FAVOURITE_ED +"' "+
                    "order by Datetime(updatedAt) desc"
    )
    LiveData<List<CaseStoryFileCount>> getCasesCountsForCase();
}


