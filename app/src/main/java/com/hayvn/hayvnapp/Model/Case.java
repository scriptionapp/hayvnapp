package com.hayvn.hayvnapp.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;


/**
 * Created by Olia on 16/01/2018.
 */


@Entity(tableName = "case")
public class Case implements Serializable {
    @PrimaryKey(autoGenerate = true) //special local id for ROOM
    private int cid;
    @ColumnInfo(name = "fbId")       //The firebase id of the case
    private String fbId;
    @ColumnInfo(name = "userId")     //the firebase userId
    private String userId;
    @ColumnInfo(name = "patientId")     //the unique patient ID
    private String patientId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "lastEntry")
    private String lastEntry;
    @ColumnInfo(name = "summary")
    private String summary;
    @ColumnInfo(name = "dateofbirth")
    private String dateofbirth;
    @ColumnInfo(name = "submitted")
    private boolean submitted;
    @ColumnInfo(name = "submittedAt")
    private Long submittedAt;
    @ColumnInfo(name = "createdAt")
    private Long createdAt;
    @ColumnInfo(name = "updatedAt")
    private Long updatedAt;
    @ColumnInfo(name = "updatedFirebaseAt")
    private Long updatedFirebaseAt;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "stringOne")
    private String stringOne;  //<<<USED FOR ALLERGIES
    @ColumnInfo(name = "stringTwo")
    private String stringTwo; // <<<used for location
    @ColumnInfo(name = "stringThree")
    private String stringThree; // <<<< used for favourites

    @ColumnInfo(name = "string4")
    private String string4; ////<<<<< hospital ID
    @ColumnInfo(name = "string5")
    private String string5; //read-only list of medication
    @ColumnInfo(name = "string6")
    private String string6; //<<phone number
    @ColumnInfo(name = "string7")
    private String string7; //
    @ColumnInfo(name = "string8")
    private String string8; //
    @ColumnInfo(name = "string9")
    private String string9; //

    public Case(String uid) {
        this.userId = uid;
    }

    public Case() {
    }


    @Override
    public String toString() {
        return ("fbid: " + this.fbId + "\n" +
                "cid: " + this.cid + "\n" +
                "name: " + this.name + "\n" +
                "lastEntry: " + this.lastEntry+ "\n" +
                "summary: " + this.summary );
    }


    public Long getUpdatedFirebaseAt() {
        return updatedFirebaseAt;
    }
    public void setUpdatedFirebaseAt(Long updatedFirebaseAt) {
        this.updatedFirebaseAt = updatedFirebaseAt;
    }

    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public int getCid() {
        return cid;
    }
    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getFbId() { return fbId; }
    public void setFbId(String fbId) { this.fbId = fbId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) {
        if(this.userId == null || this.userId.equals(""))
            this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLastEntry() {
        return lastEntry;
    }
    public void setLastEntry(String last_entry) {
        this.lastEntry = last_entry;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }
    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean getSubmitted() {
        return submitted;
    }
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public Long getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Long submittedAt) { this.submittedAt = submittedAt; }

    public Long getCreatedAt() { return createdAt; };
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getType() { return type; }
    public String getStringOne() { return stringOne; }
    public String getStringTwo() { return stringTwo; }
    public String getStringThree() { return stringThree; }

    public void setType(String type) { this.type = type; }
    public void setStringOne(String stringOne) { this.stringOne = stringOne; }
    public void setStringTwo(String stringTwo) { this.stringTwo = stringTwo; }
    public void setStringThree(String stringThree) { this.stringThree = stringThree; }

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
