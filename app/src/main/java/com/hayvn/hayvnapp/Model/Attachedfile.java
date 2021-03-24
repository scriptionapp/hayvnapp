package com.hayvn.hayvnapp.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

//, foreignKeys = @ForeignKey(entity = Story.class,
//        parentColumns = "sid",
//        childColumns = "storyid",
//        onDelete = CASCADE), indices = {@Index(value = "storyid")}
@Entity(tableName = "attachedfile")
public class Attachedfile implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "fid")               //local file id
    private int fid;
    @ColumnInfo(name = "sid")              //local story id
    private int sid;
    @ColumnInfo(name="cid")                 //local case id
    private int cid;

    @ColumnInfo(name = "fbId")                //The the firestore id for the file
    private String fbId;
    @ColumnInfo(name = "caseFbId")            //the firestore caseId for the case
    private String caseFbId;
    @ColumnInfo(name = "storyFbId")           // the firebase storyId for the owning story
    private String storyFbId;
    @ColumnInfo(name = "userId")            //the firestore userId
    private String userId;

    @ColumnInfo(name = "fileName")
    private String fileName;
    @ColumnInfo(name = "extension")
    private String extension;
    @ColumnInfo(name = "localFilePath")
    private String localFilePath;
    @ColumnInfo(name = "fireStorageFilePath")
    private String fireStorageFilePath;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "localStatus")
    private String localStatus;
    @ColumnInfo(name = "createdAt")
    private Long createdAt;

    @ColumnInfo(name = "updatedAt")
    private Long updatedAt;

    @ColumnInfo(name = "updatedFirebaseAt")
    private Long updatedFirebaseAt;


    @ColumnInfo(name = "typeEntry")
    private String typeEntry;
    @ColumnInfo(name = "stringOne")
    private String stringOne;
    @ColumnInfo(name = "stringTwo")
    private String stringTwo;
    @ColumnInfo(name = "stringThree")
    private String stringThree;


    public Attachedfile() {
        Long now = (new Date()).getTime();
        if(this.createdAt == null) {
            this.createdAt = now;
            this.updatedAt = now;
        }
    }
    @Override
    public String toString() {
        return ("story ID: " + this.sid + "\n" +
                "firebaseId: " + this.fbId + "\n" +
                "fireStorageFilePath: " + this.fireStorageFilePath + "\n" +
                "storyFbId: " + this.storyFbId + "\n" +
                "fileName: " + this.fileName + "\n" +
                "localFilePath: " + this.localFilePath + "\n" +
                "fid: " + this.fid + "\n");
    }

    public int getFid() {
        return fid;
    }
    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getSid() {
        return sid;
    }
    public void setSid(int sid) { this.sid = sid; }

    public int getCid() { return cid; }
    public void setCid(int cid) {
        this.cid = cid;
    }

    public Long getUpdatedFirebaseAt() {
        return updatedFirebaseAt;
    }
    public void setUpdatedFirebaseAt(Long updatedFirebaseAt) {
        this.updatedFirebaseAt = updatedFirebaseAt;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }
    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getLocalStatus() {
        return localStatus;
    }
    public void setLocalStatus(String localStatus) {
        this.localStatus = localStatus;
    }

    public void setFireStorageFilePath(String fireStorageFilePath) {
        this.fireStorageFilePath = fireStorageFilePath;
    }
    public String getFireStorageFilePath() {
        return fireStorageFilePath;
    }

    public String getFbId() { return fbId; }
    public void setFbId(String fbId) { this.fbId = fbId; }

    public String getCaseFbId() {
        return caseFbId;
    }
    public void setCaseFbId(String caseFbId) {
        this.caseFbId = caseFbId;
    }

    public String getStoryFbId() { return storyFbId; }
    public void setStoryFbId(String storyFbId) {
        this.storyFbId = storyFbId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) {
        this.userId =  userId;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) { this.type = type;  }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getTypeEntry() { return typeEntry; }
    public String getStringOne() { return stringOne; }
    public String getStringTwo() { return stringTwo; }
    public String getStringThree() { return stringThree; }
    public void setTypeEntry(String typeEntry) { this.typeEntry = typeEntry; }
    public void setStringOne(String stringOne) { this.stringOne = stringOne; }
    public void setStringTwo(String stringTwo) { this.stringTwo = stringTwo; }
    public void setStringThree(String stringThree) { this.stringThree = stringThree; }

}
