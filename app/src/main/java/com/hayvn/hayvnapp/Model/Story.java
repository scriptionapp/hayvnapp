package com.hayvn.hayvnapp.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hayvn.hayvnapp.Utilities.DateUtils;

import java.io.Serializable;
//import java.time.OffsetDateTime;
import java.util.Date;

@Entity(tableName = "story")
public class Story implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int sid;                //local story id
    @ColumnInfo(name="cid")
    public int cid;                //local case id

    @ColumnInfo(name = "fbId")        //The firebase id for the story
    private String fbId;
    @ColumnInfo(name = "caseFbId")    //the firebase caseId for the case
    private String caseFbId;
    @ColumnInfo(name = "userId")    //the firebase userId
    public String userId;

    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "log")
    public String log;
    @ColumnInfo(name = "occurredAt")
    private String occurredAt;
    @ColumnInfo(name = "witnesses")
    private String witnesses;
    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "typeEntry")
    private String typeEntry;
    @ColumnInfo(name = "statusEntry")
    private String statusEntry;

    @ColumnInfo(name = "createdAt")
    public Long createdAt;
    @ColumnInfo(name = "updatedAt")
    public Long updatedAt;
    @ColumnInfo(name = "updatedFirebaseAt")
    private Long updatedFirebaseAt;


    @ColumnInfo(name = "type")
    public String type; //<<<<this will store support stories, like profile pic story
    @ColumnInfo(name = "instruction")
    private String instruction;
    @ColumnInfo(name = "stringOne")
    private String stringOne; //<<<USER treatment plan
    @ColumnInfo(name = "stringTwo")
    private String stringTwo;
    @ColumnInfo(name = "stringThree")
    private String stringThree;

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

    @Override
    public String toString() {
        return ("Title: " + this.title + "\n" +
                "Log: " + this.log + "\n" +
                "Sid:" + this.sid + "\n"+
                "Cid:" + this.cid + "\n"+
                "Date: " + this.occurredAt + "\n" +
                "Type: " + this.type + "\n" +
                "Witnesses: " + this.witnesses + "\n" +
                "Location: " + this.location + "\n" +
                "Entry made on: " + this.createdAt + "\n");
    }

    public String toClip() {
        return (this.title + "\n" +
                this.log + "\n" +
                "Date: " + this.occurredAt + "\n" +
                this.witnesses + "\n" +
                this.location + "\n");
    }

    public Story() {
        Long now = (new Date()).getTime();
        if(this.createdAt == null) {
            this.createdAt = now;
            this.updatedAt = now;
        }
    }

    public Story(boolean with_occ){
        Long now = (new Date()).getTime();
        if(this.createdAt == null) {
            this.createdAt = now;
            this.updatedAt = now;
            this.occurredAt = DateUtils.dateToString(new Date());
        }
    }

    public int getSid() {
        return sid;
    }
    public void setSid(int sid) {
        this.sid = sid;
    }

    public Long getUpdatedFirebaseAt() {
        return updatedFirebaseAt;
    }
    public void setUpdatedFirebaseAt(Long updatedFirebaseAt) {
        this.updatedFirebaseAt = updatedFirebaseAt;
    }

    public int getCid() { return cid; }
    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getFbId() { return fbId; }
    public void setFbId(String fbId) { this.fbId = fbId; }

    public String getCaseFbId() {
        return caseFbId;
    }
    public void setCaseFbId(String caseFbId) {
        this.caseFbId = caseFbId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }

    public String getOccurredAt() {
        return occurredAt;
    }
    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getWitnesses() {
        return witnesses;
    }
    public void setWitnesses(String witnesses) {
        this.witnesses = witnesses;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Long getCreatedAt() { return createdAt; };
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getTypeEntry() {
        return typeEntry;
    }
    public String getStatusEntry() {
        return statusEntry;
    }

    public void setTypeEntry(String typeEntry) {
        this.typeEntry = typeEntry;
    }
    public void setStatusEntry(String statusEntry) {
        this.statusEntry = statusEntry;
    }


    public String getType() { return type; }
    public String getStringOne() { return stringOne; }
    public String getStringTwo() { return stringTwo; }
    public String getStringThree() { return stringThree; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
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
