package com.hayvn.hayvnapp.Model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;
    @ColumnInfo(name = "email")
    public String email;
    @ColumnInfo(name = "doctorId")
    public String doctorId;
    @ColumnInfo(name = "notification")
    private boolean notification;
    @ColumnInfo(name = "pin")
    public String pin;
    @ColumnInfo(name = "tempPinTime")
    private String tempPinTime;
    @ColumnInfo(name = "tempPinValue")
    private String tempPinValue;
    @ColumnInfo(name = "deviceSyncStatus")
    private String deviceSyncStatus;

    @ColumnInfo(name = "stringOne")
    private String stringOne; //<<<<< amadergram ID
    @ColumnInfo(name = "stringTwo")
    private String stringTwo;//<<<<< default country
    @ColumnInfo(name = "stringThree")
    private String stringThree; //<<<<<<name of the user/doctor

    @ColumnInfo(name = "string4")
    private String string4; //
    @ColumnInfo(name = "string5")
    private String string5; //
    @ColumnInfo(name = "string6")
    private String string6; //
    @ColumnInfo(name = "string7")
    private String string7; //
    @ColumnInfo(name = "string8")
    private String string8; //
    @ColumnInfo(name = "string9")
    private String string9; //

    public User(){    }

    @Override
    public String toString(){
        return(id + email);
    }

    public String getId() {
        return id;
    }


    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTempPinTime() {
        return tempPinTime;
    }

    public void setTempPinTime(String tempPinTime) {
        this.tempPinTime = tempPinTime;
    }

    public String getTempPinValue() {
        return tempPinValue;
    }

    public void setTempPinValue(String tempPinValue) {
        this.tempPinValue = tempPinValue;
    }

    public String getDeviceSyncStatus() {
        return deviceSyncStatus;
    }

    public void setDeviceSyncStatus(String deviceSyncStatus) {  this.deviceSyncStatus = deviceSyncStatus;  }

    public String getStringOne() {
        return stringOne;
    }

    public void setStringOne(String stringOne) {
        this.stringOne = stringOne;
    }

    public String getStringTwo() {
        return stringTwo;
    }

    public void setStringTwo(String stringTwo) {
        this.stringTwo = stringTwo;
    }

    public String getStringThree() {
        return stringThree;
    }

    public void setStringThree(String stringThree) {
        this.stringThree = stringThree;
    }

    public String getString4() {
        return string4;
    }

    public void setString4(String string4) {
        this.string4 = string4;
    }

    public String getString5() {
        return string5;
    }

    public void setString5(String string5) {
        this.string5 = string5;
    }

    public String getString6() {
        return string6;
    }

    public void setString6(String string6) {
        this.string6 = string6;
    }

    public String getString7() {
        return string7;
    }

    public void setString7(String string7) {
        this.string7 = string7;
    }

    public String getString8() {
        return string8;
    }

    public void setString8(String string8) {
        this.string8 = string8;
    }

    public String getString9() {
        return string9;
    }

    public void setString9(String string9) {
        this.string9 = string9;
    }
}
