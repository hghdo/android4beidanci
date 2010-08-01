package cn.zadui.vocabulary.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.zadui.vocabulary.model.course.SimpleCourse;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

import android.database.Cursor;

/**
 * TODO Add schedule of a Unit to alarm manager.
 * 
 * @author david
 *
 */
public class Section {
	
	public static final int WORDS_COUNT_STYLE=0;
	public static final int WORDS_COUNT_STYLE_DEFAULT=20;
	public static final int TIME_INTERVAL_STYLE=1;
	public static final int TIME_INTERVAL_STYLE_DEFAULT=1*3600;

	private static final int MAX_UNSAVED_WORDS=5;
	
	private long rowId=0;
	private int createStyle;
	private String courseName;
	private int wordsCount=0;
	private boolean virginFlag;
	private int reviewTimes=0;
	private int createdAt;
	private int nextExamAt;
	private StudyDbAdapter adapter;
	private ArrayList<Word> unsavedWords=new ArrayList<Word>();

	
    /**
     * Obtain a unit that can save new words to.
     * 
     * @return a virgin {@link Section} that can accept new words.
     */
	public static Section obtainUnit(StudyDbAdapter adapter,String courseName){
		Section su=null;
		Cursor c=adapter.getLatestSection(courseName);
		if (c!=null && c.moveToFirst()){
			su=new Section(adapter,c);
			if(!su.isVirgin()){
				su=new Section(adapter,courseName);
				adapter.createSectionInDb(su);
			}
		}else{
			su=new Section(adapter,courseName);
			adapter.createSectionInDb(su);
		}
		return su;
	}
	
    /**
     * Fetch a unit with the unit DB id
     * @return a virgin {@link Section} that can accept new words.
     */
	public static Section findUnitById(StudyDbAdapter adapter,long id){
		Cursor c=adapter.fetchSection(id);
		if (c!=null && c.moveToFirst()){
			Section su=new Section(adapter,c);
			return su;
		}else{
			return null;
		}
	}
	
	public void addWord(Word word){
		unsavedWords.add(word);
		if (unsavedWords.size()>=MAX_UNSAVED_WORDS) saveUnsavedWords();
	}
	
	/**
	 * Add words to this StudyUnit 
	 * @param dbAdapter
	 * @param words
	 */
	public void saveUnsavedWords(){
		if (unsavedWords.size()==0) return;
		for(Iterator<Word> it=unsavedWords.iterator();it.hasNext();){
			adapter.insertWord(rowId, it.next());
		}
		if (adapter.updateSectionWordsCount(rowId,unsavedWords.size()+wordsCount)) wordsCount=wordsCount+unsavedWords.size();
		unsavedWords.clear();
	}
	
	/**
	 * TODO schedule this unit in Alarm manager
	 */
	public void closeUnit(){
		saveUnsavedWords();
		int currentSec=(int)System.currentTimeMillis()/1000;
		nextExamAt=currentSec + SimpleCourse.firstInterval;
		adapter.updateSectionToOld(rowId,nextExamAt);
	}
	
	public int getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(int createdAt) {
		this.createdAt = createdAt;
	}
	
	public long getRowId() {
		return rowId;
	}
	
	public void setRowId(long rowId) {
		this.rowId = rowId;
	}

	public int getReviewTimes() {
		return reviewTimes;
	}

	public void setReviewTimes(int reviewTimes) {
		this.reviewTimes = reviewTimes;
	}

	public boolean getVirginFlag() {
		return virginFlag;
	}

	public void setVirginFlag(boolean virginFlag) {
		this.virginFlag = virginFlag;
	}

//	public boolean isOld() {
//		old=((Helper.currentSecTime()-this.createdAt)>SimpleCourse.firstInterval);
//		return old;
//	}

	public int getCreatedStyle() {
		return createStyle;
	}

	public void setCreatedStyle(int createdStyle) {
		this.createStyle = createdStyle;
	}

	public int getWordsCount() {
		return wordsCount+unsavedWords.size();
	}
	
	/**
	 * Create StudyUnit using a Cursor object that fetched from DB.
	 * @param dbAdapter
	 * @param c
	 */
	private Section(StudyDbAdapter dbAdapter,Cursor c){
    	if (c!=null && c.moveToFirst()){
    		rowId=c.getLong(c.getColumnIndex(StudyDbAdapter.KEY_ROWID));
    		courseName=c.getString(c.getColumnIndex(StudyDbAdapter.KEY_COURSE_NAME));
    		createStyle=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_CREATE_STYLE));
    		wordsCount=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_WORDS_COUNT));
    		virginFlag=(c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_VIRGIN_FLAG))!=0);
    		reviewTimes=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_COMMON_EXAM_TIMES));
    		createdAt=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_CREATED_AT));
    		nextExamAt=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT));
    		c.close();
		}
    	adapter=dbAdapter;
	}
	
	/**
	 * Create a new StudyUnit.
	 */
	private Section(StudyDbAdapter dbAdapter,String courseName){
		virginFlag=true;
		reviewTimes=0;
		createdAt=Helper.currentSecTime();
		wordsCount=0;
		createStyle=WORDS_COUNT_STYLE;
		this.courseName=courseName;
		adapter=dbAdapter;
	}
		
	private boolean isNew(){
		return rowId==0;
	}
	
	/**
	 * Virgin means 1)The unit is already saved to DB. 2)never be reviewed.
	 * 3) created within one hour.
	 * @return whether the unit is a virgin one.
	 */
	private boolean isVirgin(){
		if (isNew()) return false;
		//return (virginFlag && !isOld());
		return virginFlag;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	
}
