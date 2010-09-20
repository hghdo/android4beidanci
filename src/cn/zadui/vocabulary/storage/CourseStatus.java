package cn.zadui.vocabulary.storage;

import android.database.Cursor;
import cn.zadui.vocabulary.model.Helper;
import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.course.Course;

public class CourseStatus {

	/**
	 * The home directory where is used to save this application's data  
	 */
	public static final String DATA_DIR="/sdcard/beidanci/data/";
	/**
	 * Used for initialize the {@link lastWord} property.
	 */
	public static final String AT_BEGINNING="initialStatus";

	//If no course is selected to study empty is true
	private boolean empty=false;
	
	// Course status
	private String courseKey;
	private String courseMd5;
	private String courseTitle;
	private String courseFileName;
	private String courseLang;
	private int learnedWordsCount=0;
	private long nextContentOffset=0;
	private String lastWord;
	private int contentCount;
	private long createdAt;
	private long updatedAt;
	//private int unitCreateStyle=Section.WORDS_COUNT_STYLE;
	//private int unitCreateStyleValue=Section.WORDS_COUNT_STYLE_DEFAULT;
	
	//Dict status
	private String dictFileName;
	
	private long rowId=StudyDbAdapter.INVALID_ROW_ID;
	
	
	/**
	 * Create a CourseStatus using a {@Link Course}
	 * @param c
	 */
	public CourseStatus(String key,String md5,String title,String filename,int count){
		courseTitle=title;
		courseKey=key;
		courseMd5=md5;
		courseFileName=filename;
		learnedWordsCount=0;	
		lastWord=AT_BEGINNING;
		nextContentOffset=0;	
		contentCount=count;	
	}
		
	/**
	 * Try to load by course name
	 * @param courseKey
	 * @param dbAdapter
	 */
	public CourseStatus(String courseKey,StudyDbAdapter dbAdapter){
		empty=true;
		Cursor c=dbAdapter.findCourseStatusByKey(courseKey);
		if (!c.moveToFirst()){
			c.close();
			return;
		}
		loadFromCursor(c);
		c.close();
	}
	
	/**
	 * Try to load CourseStatus by rowId
	 * @param sp
	 * @param dbAdapter
	 */
	public CourseStatus(long _id,StudyDbAdapter dbAdapter){
		empty=true;
		if (_id==0) return;
		Cursor c=dbAdapter.findCourseStatus(_id);
		if (!c.moveToFirst()){
			c.close();
			return;
		}
		loadFromCursor(c);
		c.close();
	}
	
	private void loadFromCursor(Cursor c){
		rowId=c.getLong(c.getColumnIndex(StudyDbAdapter.KEY_ROWID));
		courseKey=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_COURSE_KEY));
		courseMd5=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_COURSE_MD5));
		courseTitle=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_COURSE_TITLE));
		courseFileName=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_COURSE_FILE_NAME));
		learnedWordsCount=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_LEARNED_CONTENT_COUNT));
		lastWord=c.getString(c.getColumnIndex(StudyDbAdapter.DB_COL_LAST_WORD));
		nextContentOffset=c.getLong(c.getColumnIndex(StudyDbAdapter.DB_COL_NEXT_CONTENT_OFFSET));
		contentCount=c.getInt(c.getColumnIndex(StudyDbAdapter.DB_COL_CONTENT_COUNT));
		createdAt=c.getLong(c.getColumnIndex(StudyDbAdapter.DB_COL_CREATED_AT));
		updatedAt=c.getLong(c.getColumnIndex(StudyDbAdapter.DB_COL_UPDATED_AT));
	}
	
	/**
	 * Save a new record if is new other wise update it.
	 * @param dbAdapter
	 * @return Sqlite rowId of this CourseStatus
	 */
	public long save(StudyDbAdapter dbAdapter){
		if (isNew()){
    		createdAt=System.currentTimeMillis();
    		updatedAt=createdAt;
		}else{
			updatedAt=System.currentTimeMillis();
		}
		rowId=dbAdapter.saveOrUpdateCourseStatus(this);
		return rowId;
	}
	
	public String getCourseTitle() {
		return courseTitle;
	}
	
	public String getCourseFileName() {
		return courseFileName;
	}

	public int getLearnedWordsCount() {
		return learnedWordsCount;
	}

	public void change(String headword,int courseSeparatorByteLength){
		nextContentOffset+=(headword.getBytes().length + courseSeparatorByteLength);
		learnedWordsCount++;
		lastWord = headword;
	}

	public String getDictFileName() {
		return dictFileName;
	}

	public String getLastWord() {
		return lastWord;
	}

	/**
	 * Means how to construct or separate a Unit. 
	 * Currently there are two unit styles: by word counts or by time interval.  
	 */
//	public int getUnitCreateStyle() {
//		return unitCreateStyle;
//	}
//
//	public void setUnitCreateStyle(int unitCreateStyle) {
//		this.unitCreateStyle = unitCreateStyle;
//	}

	/**
	 * Means the trigger value to create a new {@link Section study unit} according to the 
	 * {@link getUnitCreateStyle} settings
	 */
//	public int getUnitCreateStyleValue() {
//		return unitCreateStyleValue;
//	}
//
//	public void setUnitCreateStyleValue(int unitCreateStyleValue) {
//		this.unitCreateStyleValue = unitCreateStyleValue;
//	}

	public long getNextContentOffset() {
		return nextContentOffset;
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
		return rowId==StudyDbAdapter.INVALID_ROW_ID;
	}

	public String getCourseLang() {
		return courseLang;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}
	
	/**
	 * If no course is selected to study returns true
	 * @return whether there is a course selected for study
	 */
	public boolean isEmpty() {
		empty=(courseFileName==null || courseFileName.length()<1);
		return empty;
	}

	public String getCourseKey() {
		return courseKey;
	}

	public void setCourseKey(String courseKey) {
		this.courseKey = courseKey;
	}

	public String getCourseMd5() {
		return courseMd5;
	}

	public void setCourseMd5(String courseMd5) {
		this.courseMd5 = courseMd5;
	}

	
}
