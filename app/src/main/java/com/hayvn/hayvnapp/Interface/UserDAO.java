package com.hayvn.hayvnapp.Interface;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hayvn.hayvnapp.Model.User;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserDAO {

    @Query("select * from `user` limit 1")
    LiveData<User> findOnlyUser();

    @Query("select * from `user` where id= :id")
    LiveData<User> findById(int id);

    @Insert(onConflict = REPLACE)
    void insertAll(User... users);

    @Update(onConflict = REPLACE)
    void updateSetting(User caseName);

    @Delete
    void delete(User caseName);

}