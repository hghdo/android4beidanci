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
		headword=cur.getString(cur.getColumnIndex(StudyDbAdapter.KEY_WORD));
		meaning=cur.getString(cur.getColumnIndex(StudyDbAdapter.KEY_MEANING));
		phonetic=cur.getString(cur.getColumnIndex(StudyDbAdapter.KEY_PHONETIC));
		lastFailed=cur.getInt(cur.getColumnIndex(StudyDbAdapter.KEY_LAST_EXAM_FAILED));
		reviewTimes=cur.getInt(cur.getColumnIndex(StudyDbAdapter.KEY_EXAM_TIMES));
		successTimes=cur.getInt(cur.getColumnIndex(StudyDbAdapter.KEY_SUCCESS_TIMES));
		failedTimes=cur.getInt(cur.getColumnIndex(StudyDbAdapter.KEY_FAILED_TIMES));
		mastered=cur.getInt(cur.getColumnIndex(StudyDbAdapter.KEY_MASTERED));
	}
	
	public boolean review(StudyDbAdapter adapter,int status){
		return adapter.updateWordStatus(status, this);
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

	public String getPhonetic() {
		return phonetic;
	}

	public void setPhonetic(String phonetic) {
		this.phonetic = phonetic;
	}
	
	
}
