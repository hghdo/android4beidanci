package cn.zadui.vocabulary.model;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.database.Cursor;
import cn.zadui.vocabulary.model.course.SimpleCourse;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * TODO Add schedule of a Unit to alarm manager.
 * 
 * @author Huang Gehua
 *
 */
public class Section {
	
	public static final long[] EXAM_INTERVAL={
		4*60*60*1000,
		8*60*60*1000,
		14*60*60*1000,
		24*60*60*1000,
		36*60*60*1000
		};
	
	private static final int MAX_UNSAVED_WORDS=5;
	
	private long rowId=0;
	private String courseKey;
	private String courseTitle;
	private int wordsCount=0;
	private int masteredCount=0;
	private int examTimes=0;
	private long lastExamAt;
	private long nextExamAt;
//	private long nextFailedExamAt;
	private boolean lastExamFinished=false;
	private int lastExamPosition=0;
	private int lastExamMark=0;
	private long createdAt;
	
	private StudyDbAdapter adapter;
	private List<Word> unsavedWords=new LinkedList<Word>();

	
    /**
     * Obtain a unit that can save new words to.
     * 
     * @return a virgin {@link Section} that can accept new words.
     */
	public static Section obtain(StudyDbAdapter adapter,CourseStatus cs){
		Section se=null;
		Cursor c=adapter.getLatestSection(cs.getCourseKey());
		if (c!=null && c.moveToFirst()){
			se=new Section(adapter,c);
			Date today=new Date();
			Date createdDate=se.getCreatedAtDate();
			DateFormat df=DateFormat.getDateInstance(DateFormat.SHORT);
			if(!(df.format(today).equals(df.format(createdDate)))){
				se=new Section(adapter,cs.getCourseTitle(),cs.getCourseKey());
				adapter.createSectionInDb(se);				
			}
		}else{
			se=new Section(adapter,cs.getCourseTitle(),cs.getCourseKey());
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
		adapter.insertWord(rowId, word,courseKey);
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
	
	public void examed(boolean finished){
		if (finished){
			this.examTimes+=1;
			this.lastExamFinished=true;
			if (examTimes>EXAM_INTERVAL.length)
			this.nextExamAt=System.currentTimeMillis() + EXAM_INTERVAL[examTimes-1];
		}
		this.setLastExamAt(System.currentTimeMillis());
		adapter.updateSection(this);
		//adapter.updateSection(id, arg, columnName)
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
    		courseKey=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_COURSE_KEY));
    		courseTitle=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_COURSE_TITLE));
    		wordsCount=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_WORDS_COUNT));
    		masteredCount=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_MASTERED_COUNT));
    		examTimes=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_COMMON_EXAM_TIMES));
    		lastExamAt=c.getLong(c.getColumnIndex(StudyDbAdapter.DB_COL_LAST_EXAM_AT));
    		nextExamAt=c.getLong(c.getColumnIndex(StudyDbAdapter.DB_COL_NEXT_COMMON_EXAM_AT));
    		lastExamFinished=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_LAST_EXAM_FINISHED))==1;
    		lastExamPosition=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_LAST_EXAM_POSITION));
    		lastExamMark=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_LAST_EXAM_MARK));
    		createdAt=c.getLong(c.getColumnIndex(StudyDbAdapter.DB_COL_CREATED_AT));
    		c.close();
		}
    	adapter=dbAdapter;
	}
	
	/**
	 * Create a new StudyUnit.
	 */
	private Section(StudyDbAdapter dbAdapter,String courseTitle,String courseKey){
		examTimes=0;
		createdAt=System.currentTimeMillis();
		wordsCount=0;
		this.courseTitle=courseTitle;
		this.courseKey=courseKey;
		adapter=dbAdapter;
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
		return courseTitle;
	}

	public void setCourseName(String courseName) {
		this.courseTitle = courseName;
	}

	public String getCourseKey() {
		return courseKey;
	}

	public int getLastExamPosition() {
		return lastExamPosition;
	}

	public long getNextExamAt() {
		return nextExamAt;
	}

	public void setNextExamAt(long nextExamAt) {
		this.nextExamAt = nextExamAt;
	}

	public long getLastExamAt() {
		return lastExamAt;
	}

	public void setLastExamAt(long lastExamAt) {
		this.lastExamAt = lastExamAt;
	}

	public boolean isLastExamFinished() {
		return lastExamFinished;
	}

	public void setLastExamFinished(boolean lastExamFinished) {
		this.lastExamFinished = lastExamFinished;
	}

	public int getLastExamMark() {
		return lastExamMark;
	}

	public void setLastExamMark(int lastExamMark) {
		this.lastExamMark = lastExamMark;
	}

	public void setLastExamPosition(int lastExamPosition) {
		this.lastExamPosition = lastExamPosition;
	}

	public int getMasteredCount() {
		return masteredCount;
	}

	public void setMasteredCount(int masteredCount) {
		this.masteredCount = masteredCount;
	}

	public int getExamTimes() {
		return examTimes;
	}

	public void setExamTimes(int examTimes) {
		this.examTimes = examTimes;
	}
	
}
