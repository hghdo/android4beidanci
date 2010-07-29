package cn.zadui.vocabulary.storage;

import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.course.Course;
import android.content.SharedPreferences;
import android.database.Cursor;

public class CourseStatus {

	public static final String PREFS_NAME="beidanci.prefs";
	
	/**
	 * The home directory where is used to save this application's data  
	 */
	public static final String DATA_DIR="/sdcard/beidanci/data/";
	/**
	 * Used for initialize the {@link lastWord} property.
	 */
	public static final String AT_BEGINNING="initialStatus";
	
	public static final String SP_KEY_CURRENT_COURSE_FILE_NAME="currentCourseFileName";
	public static final String SP_KEY_CURRENT_COURSE_NAME="currentCourseName";
	public static final String SP_KEY_LEARNED_COUNT="learnedWordsCount";
	public static final String SP_KEY_NEXT_CONTENT_OFFSET="nextContentOffset";
	public static final String SP_KEY_LAST_WORD="lastWord";
	public static final String SP_KEY_CONTENT_COUNT="content_count";
	public static final String SP_KEY_DICT_FILE_NAME="defDictFileName";
	public static final String SP_KEY_UNIT_CREATE_STYLE="unitCreateStyle";
	public static final String SP_KEY_UNIT_CREATE_STYLE_VALUE="unitCreateStyleValue";

	//private static Status status;
	//public static SharedPreferences spSettings;
	
	//If no course is selected to study empty is true
	private boolean empty=false;
	// Course status
	private String courseFileName;
	private String courseName;
	private int learnedWordsCount=0;
	private long nextContentOffset=0;
	private String lastWord;
	private int contentCount;
	private int unitCreateStyle;
	private int unitCreateStyleValue;
	//Dict status
	private String dictFileName;
	
	private long rowId=0;
	
//	public static Status getInstance(SharedPreferences sp){
//		if (status==null) status=new Status(sp);
//		return status;
//	}
	
	public CourseStatus(){
		
	}
	
	public CourseStatus(Course c){
		courseName=c.getName();
		courseFileName=c.getCourseFileName();
		learnedWordsCount=0;	
		lastWord=AT_BEGINNING;
		nextContentOffset=0;	
		contentCount=c.getContentCount();	
		unitCreateStyle=Section.WORDS_COUNT_STYLE;
		unitCreateStyleValue=Section.WORDS_COUNT_STYLE_DEFAULT;
		empty=(courseFileName==null || courseFileName.length()<1);
	}
	
	public CourseStatus(SharedPreferences sp){
		courseName=sp.getString(SP_KEY_CURRENT_COURSE_NAME, courseName);
		courseFileName=sp.getString(SP_KEY_CURRENT_COURSE_FILE_NAME, "");
		learnedWordsCount=sp.getInt(SP_KEY_LEARNED_COUNT, 0);	
		lastWord=sp.getString(SP_KEY_LAST_WORD, AT_BEGINNING);
		nextContentOffset=sp.getLong(SP_KEY_NEXT_CONTENT_OFFSET, 0);	
		contentCount=sp.getInt(SP_KEY_CONTENT_COUNT, 0);	
		//TODO Should add a mechanism to select a default course.
		dictFileName=sp.getString(SP_KEY_DICT_FILE_NAME, "");
		unitCreateStyle=sp.getInt(SP_KEY_UNIT_CREATE_STYLE, Section.WORDS_COUNT_STYLE);
		unitCreateStyleValue=sp.getInt("SP_KEY_UNIT_CREATE_STYLE_VALUE", Section.WORDS_COUNT_STYLE_DEFAULT);
		empty=(courseFileName==null || courseFileName.length()<1);
	}
	
	public CourseStatus(Cursor c){
		rowId=c.getLong(c.getColumnIndex(StudyDbAdapter.KEY_ROWID));
		courseName=c.getString(c.getColumnIndex(StudyDbAdapter.KEY_COURSE_NAME));
		courseFileName=c.getString(c.getColumnIndex(StudyDbAdapter.KEY_COURSE_FILE_NAME));
		learnedWordsCount=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_LEARNED_CONTENT_COUNT));
		lastWord=c.getString(c.getColumnIndex(StudyDbAdapter.KEY_LAST_WORD));
		nextContentOffset=c.getLong(c.getColumnIndex(StudyDbAdapter.KEY_NEXT_CONTENT_OFFSET));
		contentCount=c.getInt(c.getColumnIndex(StudyDbAdapter.KEY_CONTENT_COUNT));
	}
	
	public void refresh(SharedPreferences sp){
		courseName=sp.getString(SP_KEY_CURRENT_COURSE_NAME, courseName);
		courseFileName=sp.getString(SP_KEY_CURRENT_COURSE_FILE_NAME, "");
		learnedWordsCount=sp.getInt(SP_KEY_LEARNED_COUNT, 0);	
		lastWord=sp.getString(SP_KEY_LAST_WORD, AT_BEGINNING);
		nextContentOffset=sp.getLong(SP_KEY_NEXT_CONTENT_OFFSET, 0);	
		contentCount=sp.getInt(SP_KEY_CONTENT_COUNT, 0);	
		//TODO Should add a mechanism to select a default course.
		dictFileName=sp.getString(SP_KEY_DICT_FILE_NAME, "");
		unitCreateStyle=sp.getInt(SP_KEY_UNIT_CREATE_STYLE, Section.WORDS_COUNT_STYLE);
		unitCreateStyleValue=sp.getInt("SP_KEY_UNIT_CREATE_STYLE_VALUE", Section.WORDS_COUNT_STYLE_DEFAULT);
		empty=(courseFileName==null || courseFileName.length()<1);
	}
	
	public void saveCourseStatusToPreferences(SharedPreferences spSettings){
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putString(SP_KEY_CURRENT_COURSE_NAME, courseName);
		editor.putString(SP_KEY_CURRENT_COURSE_FILE_NAME, courseFileName);
		editor.putInt(SP_KEY_LEARNED_COUNT, learnedWordsCount);
		editor.putString(SP_KEY_LAST_WORD, lastWord);
		editor.putLong(SP_KEY_NEXT_CONTENT_OFFSET, nextContentOffset);
		editor.putInt(SP_KEY_CONTENT_COUNT, contentCount);
		editor.putString(SP_KEY_DICT_FILE_NAME, dictFileName);
		editor.putInt(SP_KEY_UNIT_CREATE_STYLE, unitCreateStyle);
		editor.putInt(SP_KEY_UNIT_CREATE_STYLE_VALUE, unitCreateStyleValue);
		editor.commit();
	}
	
	/*
	public void updateStatus(CourseStatus cs){
		courseName=cs.courseName;
		courseFileName=cs.courseFileName;
		learnedWordsCount=cs.learnedContentCount;
		nextContentOffset=cs.nextContentOffset;
		lastWord=cs.lastContent;
		saveStatus();
	}
	*/
	
	public String getCourseFileName() {
		return courseFileName;
	}

//	public void setCourseFileName(String courseFileName) {
//		this.courseFileName = courseFileName;
//		SharedPreferences.Editor editor = spSettings.edit();
//		editor.putString(SP_KEY_CURRENT_COURSE_FILE_NAME, courseFileName);
//	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public int getLearnedWordsCount() {
		return learnedWordsCount;
	}

	public void setLearnedWordsCount(int learnedWordsCount) {
		this.learnedWordsCount = learnedWordsCount;
	}
	
	public void increaseLearnedWordsCount(){
		this.learnedWordsCount++;
	}

	/**
	 * If no course is selected to study returns true
	 * @return whether there is a course selected for study
	 */
	public boolean isEmpty() {
		return empty;
	}

	public void setStudyStatus(boolean em) {
		this.empty = em;
	}

	public String getDictFileName() {
		return dictFileName;
	}

	public void setDictFileName(String dictFileName) {
		this.dictFileName = dictFileName;
	}

	public String getLastWord() {
		return lastWord;
	}

	public void setLastWord(String lastWord) {
		this.lastWord = lastWord;
	}

	/**
	 * Means how to construct or separate a Unit. 
	 * Currently there are two unit styles: by word counts or by time interval.  
	 */
	public int getUnitCreateStyle() {
		return unitCreateStyle;
	}

	public void setUnitCreateStyle(int unitCreateStyle) {
		this.unitCreateStyle = unitCreateStyle;
	}

	/**
	 * Means the trigger value to create a new {@link Section study unit} according to the 
	 * {@link getUnitCreateStyle} settings
	 */
	public int getUnitCreateStyleValue() {
		return unitCreateStyleValue;
	}

	public void setUnitCreateStyleValue(int unitCreateStyleValue) {
		this.unitCreateStyleValue = unitCreateStyleValue;
	}

	public long getNextContentOffset() {
		return nextContentOffset;
	}

	public void setNextContentOffset(long nextContentOffset) {
		this.nextContentOffset = nextContentOffset;
	}
	
	public void increaseNextContentOffset(int length){
		this.nextContentOffset+=length;
	}
	
	public int getProgress(){
		return (this.learnedWordsCount*100)/this.contentCount;
	}

	public int getContentCount() {
		return contentCount;
	}

	public long getRowId() {
		return rowId;
	}
	
	public boolean isNew(){
		return rowId==0;
	}
}
