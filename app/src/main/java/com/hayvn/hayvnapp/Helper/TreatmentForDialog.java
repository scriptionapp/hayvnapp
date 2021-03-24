package com.hayvn.hayvnapp.Helper;

import java.util.ArrayList;
import java.util.List;

public class TreatmentForDialog {
    String dateStart;
    String dateEnd;
    String dosageAmount;
    String drugName;
    String comment;
    String doctorUid;
    String doctorScriptionId;
    String doctorEmail;
    String doctorName;
    String dateNow;
    public TreatmentForDialog(){

    }

    public TreatmentForDialog(String dateStart,
                              String dateEnd,
                              String dosageAmount,
                              String drugName,
                              String comment,
                              String doctorUid,
                              String doctorScriptionId,
                              String doctorEmail,
                              String doctorName){
        this.dateStart = emptyIfNull(dateStart);
        this.dateEnd = emptyIfNull(dateEnd);
        this.dosageAmount = emptyIfNull(dosageAmount);
        this.drugName = emptyIfNull(drugName);
        this.comment = emptyIfNull(comment);
        this.doctorUid = emptyIfNull(doctorUid);
        this.doctorScriptionId = emptyIfNull(doctorScriptionId);
        this.doctorEmail = emptyIfNull(doctorEmail);
        this.doctorName = emptyIfNull(doctorName);
    }

    public List<String> toStringList(){
        List<String> strings = new ArrayList<String>();
        strings.add("Date start: " + emptyIfNull(dateStart));
        strings.add("Date end: " + emptyIfNull(dateEnd));
        strings.add("Dosage/Amount: " + emptyIfNull(dosageAmount));
        strings.add("Comments: " + emptyIfNull(comment));
        strings.add("Doctor: " + emptyIfNull(doctorName));
        return strings;
    }

    @Override
    public String toString(){
        String strings = "";
        strings = strings.concat("Date start: " + emptyIfNull(dateStart) + "\n");
        strings = strings.concat("Date end: " + emptyIfNull(dateEnd) + "\n");
        strings = strings.concat("Dosage/Amount: " + emptyIfNull(dosageAmount) + "\n");
        strings = strings.concat("Comments: " + emptyIfNull(comment) + "\n");
        strings = strings.concat("Doctor: " + emptyIfNull(doctorName) + "\n");
        return strings;
    }

    public String getDateNow() {
        return dateNow;
    }

    public void setDateNow(String dateNow) {
        this.dateNow = dateNow;
    }


    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDoctorScriptionId() {
        return doctorScriptionId;
    }

    public void setDoctorScriptionId(String doctorScriptionId) {
        this.doctorScriptionId = doctorScriptionId;
    }
    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getDosageAmount() {
        return dosageAmount;
    }

    public void setDosageAmount(String dosageAmount) {
        this.dosageAmount = dosageAmount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDoctorUid() {
        return doctorUid;
    }

    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    private String emptyIfNull(String s){
        if(s==null){
            return "";
        }
        return s;
    }

}
