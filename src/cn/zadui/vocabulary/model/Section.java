package cn.zadui.vocabulary.model;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.database.Cursor;
import cn.zadui.vocabulary.model.course.SimpleCourse;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * TODO Add schedule of a Unit to alarm manager.
 * 
 * @author Huang Gehua
 *
 */
public class Section {
	
	private static final int MAX_UNSAVED_WORDS=5;
	
	private long rowId=0;
	private String courseName;
	private int wordsCount=0;
	//private boolean virginFlag;
	private int reviewTimes=0;
	private long createdAt;
	private long nextExamAt;
	private StudyDbAdapter adapter;
	private List<Word> unsavedWords=new LinkedList<Word>();

	
    /**
     * Obtain a unit that can save new words to.
     * 
     * @return a virgin {@link Section} that can accept new words.
     */
	public static Section obtain(StudyDbAdapter adapter,String courseName){
		Section se=null;
		Cursor c=adapter.getLatestSection(courseName);
		if (c!=null && c.moveToFirst()){
			se=new Section(adapter,c);
			Date today=new Date();
			Date createdDate=se.getCreatedAtDate();
			DateFormat df=DateFormat.getDateInstance(DateFormat.SHORT);
			if(!(df.format(today).equals(df.format(createdDate)))){
				se.freeze();
				se=new Section(adapter,courseName);
				adapter.createSectionInDb(se);				
			}
		}else{
			se=new Section(adapter,courseName);
			adapter.createSectionInDb(se);
		}
		return se;
	}
	
    /**
     * Fetch a unit with the unit DB id
     * @return a virgin {@link Section} that can accept new words.
     */
	public static Section findById(StudyDbAdapter adapter,long id){
		Cursor c=adapter.fetchSection(id);
		if (c!=null && c.moveToFirst()){
			Section su=new Section(adapter,c);
			return su;
		}else{
			return null;
		}
	}
	
	public void addWord(Word word){
		adapter.insertWord(rowId, word);
		adapter.updateSectionWordsCount(rowId,++wordsCount);
	}
	
	public Word previous(Word word){
		if (word==null || word.getId()==0) return null ;
		return adapter.previousWordInSection(rowId, word);
	}
	
	public Word next(Word word){
		if (word==null || word.getId()==0) return null;
		return adapter.nextWordInSection(rowId, word);
	}
	
	/**
	 * TODO schedule this section in Alarm manager
	 */
	public void freeze(){
		//saveUnsavedWords();
		nextExamAt=System.currentTimeMillis() + SimpleCourse.firstInterval;
		adapter.updateSectionToOld(rowId,nextExamAt);
	}
	
	public Date getCreatedAtDate(){
		Date createdDate=new Date();
		createdDate.setTime(createdAt);
		return createdDate;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(long createdAt) {
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

//	public boolean getVirginFlag() {
//		return virginFlag;
//	}
//
//	public void setVirginFlag(boolean virginFlag) {
//		this.virginFlag = virginFlag;
//	}

//	public boolean isOld() {
//		old=((Helper.currentSecTime()-this.createdAt)>SimpleCourse.firstInterval);
//		return old;
//	}

//	public int getCreatedStyle() {
//		return createStyle;
//	}
//
//	public void setCreatedStyle(int createdStyle) {
//		this.createStyle = createdStyle;
//	}

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
    		//createStyle=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_CREATE_STYLE));
    		wordsCount=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_WORDS_COUNT));
    		//virginFlag=(c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_VIRGIN_FLAG))!=0);
    		reviewTimes=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_COMMON_EXAM_TIMES));
    		createdAt=c.getLong(c.getColumnIndex(StudyDbAdapter.KEY_CREATED_AT));
    		nextExamAt=c.getLong(c.getColumnIndex(StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT));
    		c.close();
		}
    	adapter=dbAdapter;
	}
	
	/**
	 * Create a new StudyUnit.
	 */
	private Section(StudyDbAdapter dbAdapter,String courseName){
		//virginFlag=true;
		reviewTimes=0;
		createdAt=System.currentTimeMillis();
		wordsCount=0;
		//createStyle=WORDS_COUNT_STYLE;
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
//	private boolean isVirgin(){
//		if (isNew()) return false;
//		//return (virginFlag && !isOld());
//		return virginFlag;
//	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	
}
