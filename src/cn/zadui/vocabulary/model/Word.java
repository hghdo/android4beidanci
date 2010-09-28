package cn.zadui.vocabulary.model;

import cn.zadui.vocabulary.storage.StudyDbAdapter;
import android.database.Cursor;

public class Word {
	
	public static final int FORGOT=2;
	public static final int PASS=1;
	public static final int MASTERED=0;
	
	private long _id;
	private String headword;
	private String meaning;
	private String phonetic;
	
	private int lastFailed=0;
	private int reviewTimes=0; 
	private int successTimes=0;
	private int failedTimes=0;
	private int mastered=0;
	
	public Word(String headword){
		this.headword=headword;
	}
	
	public Word(Cursor cur){
		_id=cur.getLong(cur.getColumnIndex(StudyDbAdapter.KEY_ROWID));
		headword=cur.getString(cur.getColumnIndex(StudyDbAdapter.DB_COL_WORD));
		meaning=cur.getString(cur.getColumnIndex(StudyDbAdapter.DB_COL_MEANING));
		phonetic=cur.getString(cur.getColumnIndex(StudyDbAdapter.DB_COL_PHONETIC));
		lastFailed=cur.getInt(cur.getColumnIndex(StudyDbAdapter.DB_COL_LAST_EXAM_FAILED));
		reviewTimes=cur.getInt(cur.getColumnIndex(StudyDbAdapter.DB_COL_EXAM_TIMES));
		successTimes=cur.getInt(cur.getColumnIndex(StudyDbAdapter.DB_COL_SUCCESS_TIMES));
		failedTimes=cur.getInt(cur.getColumnIndex(StudyDbAdapter.DB_COL_FAILED_TIMES));
		mastered=cur.getInt(cur.getColumnIndex(StudyDbAdapter.DB_COL_MASTERED));
	}
	
	public boolean directMastered(StudyDbAdapter adapter){
		mastered=1;
		lastFailed=0;
		return adapter.updateWord(this);
	}
	
	public boolean review(StudyDbAdapter adapter,int status){
		reviewTimes+=1;
		switch (status){
		case Word.MASTERED:
			mastered=1;
			break;
		case Word.FORGOT:
			failedTimes+=1;
			lastFailed=1;
			break;
		case Word.PASS:
			successTimes+=1;
			lastFailed=0;
			if (successTimes-failedTimes>5) mastered=1;
			break;
		}
		return adapter.updateWord(this);
	}
	
	public Word(String headword,String meaning){
		this.headword = headword;
		this.meaning = meaning;
	}
	
	public String getHeadword() {
		return headword;
	}
	public void setHeadword(String headword) {
		this.headword = headword;
	}
	public String getMeaning() {
		return meaning;
	}
	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}
	public boolean isForgot() {
		return lastFailed==1;
	}
	public void setForgot(boolean forget) {
		this.lastFailed = forget ? 1 : 0;
	}

	public int getReviewTimes() {
		return reviewTimes;
	}

	public void setReviewTimes(int reviewTimes) {
		this.reviewTimes = reviewTimes;
	}

	public int getSuccessTimes() {
		return successTimes;
	}

	public void setSuccessTimes(int successTimes) {
		this.successTimes = successTimes;
	}

	public int getFailedTimes() {
		return failedTimes;
	}

	public void setFailedTimes(int failedTimes) {
		this.failedTimes = failedTimes;
	}

	public boolean isMastered() {
		return mastered==1;
	}

	public void setMastered(boolean mastered) {
		this.mastered = mastered ? 1 : 0;
	}

	public long getId() {
		return _id;
	}
	
	public void setId(long id){
		_id=id;
	}

	public String getPhonetic() {
		return phonetic;
	}

	public void setPhonetic(String phonetic) {
		this.phonetic = phonetic;
	}

	public int getLastFailed() {
		return lastFailed;
	}

	public void setLastFailed(int lastFailed) {
		this.lastFailed = lastFailed;
	}

	public int getMastered() {
		return mastered;
	}

	public void setMastered(int mastered) {
		this.mastered = mastered;
	}
	
	
}
