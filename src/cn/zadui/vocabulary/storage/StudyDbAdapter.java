package cn.zadui.vocabulary.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.Word;

/**
 * Used to save user study history. The first review time is specified when the unit was first created.
 * A virgin unit means a unit was never be reviewed since it was created. NOTE that the virgin status 
 * can only be alive within one hour.  In other words if you found you have hold a virgin unit more than
 * one hour you need create a new one and release the previous.
 * @author david
 *
 */
public class StudyDbAdapter {
	
	public static final int INVALID_ROW_ID=-1;
    
    public static final int UNIT_WORDS_FILTER_ALL=0;
    public static final int UNIT_WORDS_FILTER_FORGOT_ONLY=1;
    public static final int UNIT_WORDS_FILTER_MASTERED_EXCLUDED=2;
   
    public static final String KEY_ROWID = "_id";
    
    public static final String DB_TABLE_SECTION = "sections";
    public static final String DB_COL_COURSE_KEY="course_key";
    public static final String DB_COL_COURSE_TITLE="title";
    public static final String DB_COL_WORDS_COUNT="words_count";
    public static final String DB_COL_MASTERED_COUNT="mastered_count";
//    public static final String DB_COL_FINISHED="finished";
    public static final String DB_COL_COMMON_EXAM_TIMES="common_exam_times";
    public static final String DB_COL_LAST_EXAM_AT="last_exam_at";
    public static final String DB_COL_NEXT_COMMON_EXAM_AT="next_common_exam_at";
    public static final String DB_COL_NEXT_FAILED_EXAM_AT="next_failed_exam_at";
    public static final String DB_COL_LAST_EXAM_FINISHED="last_exam_finished";
    public static final String DB_COL_LAST_EXAM_POSITION="last_exam_position";
    public static final String DB_COL_LAST_EXAM_MARK="last_exam_mark";   
    public static final String DB_COL_CREATED_AT="created_at";
 
    public static final String DB_TABLE_WORDS = "words";
    //public static final String DB_COL_COURSE_KEY="course_key";
    public static final String DB_COL_SECTION_ID="section_id";
    public static final String DB_COL_WORD="word";
    public static final String DB_COL_MEANING="meaning";
    public static final String DB_COL_PHONETIC="phonetic";
    public static final String DB_COL_LAST_EXAM_FAILED="last_exam_failed";
    public static final String DB_COL_EXAM_TIMES="exam_times";
    public static final String DB_COL_SUCCESS_TIMES="success_times";
    public static final String DB_COL_FAILED_TIMES="failed_times";
    public static final String DB_COL_MASTERED="mastered";
    
    //columns for course status
    /*
	public static final String SP_KEY_CURRENT_COURSE_FILE_NAME="currentCourseFileName";
	public static final String SP_KEY_CURRENT_COURSE_NAME="currentCourseName";
	public static final String SP_KEY_LEARNED_COUNT="learnedWordsCount";
	public static final String SP_KEY_NEXT_CONTENT_OFFSET="nextContentOffset";
	public static final String SP_KEY_LAST_WORD="lastWord";
    
    */
    private static final String DB_TABLE_COURSE_STATUS = "course_status";
    //public static final String DB_COL_COURSE_KEY="course_key";
    public static final String DB_COL_COURSE_MD5="md5";
    public static final String DB_COL_COURSE_FILE_NAME="course_file_name";
    public static final String DB_COL_LEARNED_CONTENT_COUNT="learned_content_count";
    public static final String DB_COL_NEXT_CONTENT_OFFSET="next_content_offset";
    public static final String DB_COL_LAST_WORD="last_word";
    public static final String DB_COL_CONTENT_COUNT="content_count";   
    public static final String DB_COL_UPDATED_AT="updated_at";   
    
//    public static final String EXAMPLES_TABLE = "examples";
//    public static final String KEY_SENTENCE = "sentence";
//    public static final String KEY_TIMESTAMP = "timestamp";
    
    private static final String DATABASE_CREATE_SECTIONS_TABLE =
        				"create table sections (_id integer primary key autoincrement, " + 
        				"course_key text," +
                		"title text," +
                		"words_count integer default 0," +
                		"mastered_count integer default 0," +
//                		"virgin_flag integer default 1," +
//                		"finished integer default 0," +
                		"common_exam_times integer default 0," +
                		"last_exam_at integer default 0," +
                		"next_common_exam_at integer default 0," +
                		//"last_failed_exam_at integer default 0," +
                		"next_failed_exam_at integer default 0," +
                		"last_exam_finished integer default 0," +
                		"last_exam_position integer default 0," +
                		"last_exam_mark integer default 0," +
                		"created_at integer);";
    
    private static final String DATABASE_CREATE_UNIT_WORDS_TABLE =
                		"create table words (_id integer primary key autoincrement,"+
        				"course_key text," +
                		"section_id integer," +
                		"word text," +
                		"meaning text," +
                		"phonetic text," +
                		"last_exam_failed integer default 0," +
                		"exam_times integer default 0," +
                		"success_times integer default 0," +
                		"failed_times integer default 0," +
                		"mastered integer default 0);";
    
    private static final String DATABASE_CREATE_COURSE_STATUS_TABLE =
                		"create table course_status (_id integer primary key autoincrement,"+
                		"course_key text," +
                		"md5 text," +
                		"title text," +
                		"course_file_name text," +
                		"learned_content_count integer default 0," +
                		"next_content_offset integer default 0," +
                		"last_word text," +
                		"content_count integer default 0," +
                		"updated_at integer,"+
                		"created_at integer);";
        
    private static final String DATABASE_CREATE_EXAMPLES_TABLE =
						"create table examples (_id integer primary key autoincrement,"+
						"word text," +
						"sentence text," +
						"timestamp integer default 0);";
    
    private static final String TAG = "StudyDbHelper";
    
    private final Context mCtx;    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    //private int userId;
    //private int courseId;
    
    public StudyDbAdapter(Context context){
    	mCtx=context;
    }
    
    public StudyDbAdapter open(){
    	mDbHelper=new DatabaseHelper(mCtx);
    	mDb=mDbHelper.getWritableDatabase();
    	return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public Cursor fetchSections(){
    	return mDb.query(DB_TABLE_SECTION, null, null, null, null, null, DB_COL_CREATED_AT+" desc");
    }
    
    public Cursor fetchSectionsByCourseKey(String courseKey){
    	return mDb.query(DB_TABLE_SECTION, null, DB_COL_COURSE_KEY + "='"+courseKey+"'", null, null, null, DB_COL_CREATED_AT+" desc");
    }
    
    public Cursor fetchSection(long rowId){
    	Cursor c=mDb.query(true, DB_TABLE_SECTION, null, KEY_ROWID+"="+rowId, null, null, null, null, null);
    	return c;
    }
    
    /**
     * Get the latest Study Unit from the DB.
     * @return the latest created study unit from the DB or null if the DB is empty.
     */
    public Cursor getLatestSection(String courseKey){
    	//String [] columns=new String[]{KEY_ROWID,KEY_COURSE_NAME,KEY_CREATE_STYLE,KEY_WORDS_COUNT,KEY_VIRGIN_FLAG,KEY_COMMON_EXAM_TIMES,KEY_CREATED_AT};
    	Cursor c=mDb.query(DB_TABLE_SECTION, null, "course_key=?", new String[]{courseKey}, null, null, DB_COL_CREATED_AT+" desc");
    	return c;
    }
    	
	public void createSectionInDb(Section section){
		ContentValues args=new ContentValues();
		args.put(DB_COL_COURSE_KEY, section.getCourseKey());
		args.put(DB_COL_COURSE_TITLE, section.getCourseName());
		args.put(DB_COL_WORDS_COUNT, 0);
		args.put(DB_COL_MASTERED_COUNT, 0);
//		args.put(DB_COL_FINISHED, 0);
		args.put(DB_COL_COMMON_EXAM_TIMES, 0);
//		args.put(DB_COL_LAST_EXAM_AT, 0);
		args.put(DB_COL_NEXT_COMMON_EXAM_AT, section.getCreatedAt()+Section.EXAM_INTERVAL[0]);
		args.put(DB_COL_CREATED_AT, section.getCreatedAt());
		section.setRowId(mDb.insert(DB_TABLE_SECTION, null, args));
	}
//	
//	public boolean freezeSection(long id,long nextExamAt){
//		ContentValues args=new ContentValues();
//		//args.put(KEY_VIRGIN_FLAG, 0);
//		args.put(DB_COL_NEXT_FAILED_EXAM_AT, nextExamAt);
//		return mDb.update(DB_TABLE_SECTION, args, KEY_ROWID + "=" + id, null) > 0;
//	}
	
	public boolean updateSectionWordsCount(long id,int newCount){
		ContentValues args=new ContentValues();
		args.put(DB_COL_WORDS_COUNT, newCount);
		return mDb.update(DB_TABLE_SECTION, args, KEY_ROWID + "=" + id, null) > 0;
	}
	
	public boolean updateSection(Section section){
		ContentValues args=new ContentValues();
//		args.put(DB_COL_COURSE_KEY, section.getCourseKey());
//		args.put(DB_COL_COURSE_TITLE, section.getCourseName());
		args.put(DB_COL_WORDS_COUNT, section.getWordsCount());
		args.put(DB_COL_MASTERED_COUNT, section.getMasteredCount());
		args.put(DB_COL_COMMON_EXAM_TIMES, section.getExamTimes());
		args.put(DB_COL_LAST_EXAM_AT, section.getLastExamAt());
		args.put(DB_COL_NEXT_COMMON_EXAM_AT, section.getNextExamAt());
		args.put(DB_COL_LAST_EXAM_FINISHED, section.isLastExamFinished() ? 1 : 0);
		args.put(DB_COL_LAST_EXAM_POSITION, section.getLastExamPosition());
		args.put(DB_COL_LAST_EXAM_MARK, section.getLastExamMark());
		return mDb.update(DB_TABLE_SECTION, args, KEY_ROWID + "=" + section.getRowId(), null) > 0;
	}
    
    public Cursor fetchSectionWords(long unitId,int filter){   
    	switch (filter){
	    	case UNIT_WORDS_FILTER_FORGOT_ONLY:
	    		return mDb.query(DB_TABLE_WORDS, null, DB_COL_SECTION_ID+"=? and "+DB_COL_LAST_EXAM_FAILED+"=?", new String[]{String.valueOf(unitId),"1"}, null, null, null);
	    	case UNIT_WORDS_FILTER_ALL:
	    		return mDb.query(DB_TABLE_WORDS, null, DB_COL_SECTION_ID+"="+unitId, null, null, null, null);
	    	case UNIT_WORDS_FILTER_MASTERED_EXCLUDED:
	    		return mDb.query(DB_TABLE_WORDS, null, DB_COL_SECTION_ID+"=? and "+DB_COL_MASTERED+"=?", new String[]{String.valueOf(unitId),"0"}, null, null, null);
	    	default: return null;
    	}
    }
    
    public void deleteSection(long sectionId){
    	mDb.delete(DB_TABLE_WORDS, DB_COL_SECTION_ID+"=? ", new String[]{String.valueOf(sectionId)});
    	mDb.delete(DB_TABLE_SECTION, "_id=? ", new String[]{String.valueOf(sectionId)});
    }
    
    public Cursor fetchWordById(long id){
    	return mDb.query(DB_TABLE_WORDS, null, "_id=? ", new String[]{String.valueOf(id)}, null, null, null);
    }
	
	public long insertWord(long unitId,Word word,String courseKey){
		ContentValues args=new ContentValues();
		args.put(DB_COL_SECTION_ID, unitId);
		args.put(DB_COL_COURSE_KEY, courseKey);
		args.put(DB_COL_WORD, word.getHeadword());
		args.put(DB_COL_MEANING, word.getMeaning());
		args.put(DB_COL_PHONETIC, word.getPhonetic());
		args.put(DB_COL_LAST_EXAM_FAILED, 0);
		args.put(DB_COL_EXAM_TIMES, 0);
		args.put(DB_COL_SUCCESS_TIMES, 0);
		args.put(DB_COL_FAILED_TIMES, 0);
		args.put(DB_COL_MASTERED, 0);
		word.setId(mDb.insert(DB_TABLE_WORDS, null, args));
		return word.getId();
	}
	
	public Word previousWordInSection(long unitId,Word word){
		Cursor c=mDb.query(DB_TABLE_WORDS, null, DB_COL_SECTION_ID+"=? and "+KEY_ROWID+"<?", new String[]{String.valueOf(unitId),String.valueOf(word.getId())}, null, null, "_id desc");
		return c.moveToFirst() ? new Word(c) : null;
	}
	
	public Word nextWordInSection(long unitId,Word word){
		Cursor c=mDb.query(DB_TABLE_WORDS, null, DB_COL_SECTION_ID+"=? and "+KEY_ROWID+">?", new String[]{String.valueOf(unitId),String.valueOf(word.getId())}, null, null, "_id");
		return c.moveToFirst() ? new Word(c) : null;
		
	}
	
	public boolean updateWordStatusExam(int status,Word word){
		ContentValues args=new ContentValues();
		args.put(DB_COL_EXAM_TIMES, word.getReviewTimes()+1);
		switch (status){
		case Word.MASTERED:
			args.put(DB_COL_MASTERED, 1);
			args.put(DB_COL_LAST_EXAM_FAILED, 0);
			break;
		case Word.PASS:
			args.put(DB_COL_SUCCESS_TIMES, word.getSuccessTimes()+1);
			args.put(DB_COL_MASTERED, 0);
			args.put(DB_COL_LAST_EXAM_FAILED, 0);
			break;
		case Word.FORGOT:
			args.put(DB_COL_FAILED_TIMES, word.getFailedTimes()+1);
			args.put(DB_COL_MASTERED, 0);
			args.put(DB_COL_LAST_EXAM_FAILED, 1);
		}
		return mDb.update(DB_TABLE_WORDS, args, KEY_ROWID + "=" + word.getId(), null) > 0;
	}
	
	public void starWord(boolean star, long id){
		ContentValues args=new ContentValues();
		args.put(DB_COL_LAST_EXAM_FAILED, star ? 1 : 0);
		mDb.update(DB_TABLE_WORDS, args, KEY_ROWID + "=" + id, null);
	}
	
	public  Cursor findCourseStatusByKey(String key){
		Cursor c=mDb.query(DB_TABLE_COURSE_STATUS,null,"course_key='"+key+"'",null,null,null,null);
		return c;
	}	
	
//	public  Cursor findCourseStatusByCourseTitle(String courseName){
//		Cursor c=mDb.query(DB_TABLE_COURSE_STATUS,null,"title='"+courseName+"'",null,null,null,null);
//		return c;
//	}
	
	public Cursor findCourseStatus(long rowId){
		Cursor c=mDb.query(DB_TABLE_COURSE_STATUS,null,"_id="+String.valueOf(rowId),null,null,null,null);
		return c;
	}
    
    public LinkedList<Map<String,String>> fetchCourseStatusList(){
    	Cursor c=mDb.query(DB_TABLE_COURSE_STATUS,null,null,null,null,null,null);
    	LinkedList<Map<String,String>> list=new LinkedList<Map<String,String>>();
    	while(c.moveToNext()){
    		Map<String,String> m=new HashMap<String,String>();
    		m.put(DB_COL_COURSE_TITLE, c.getString(c.getColumnIndex(DB_COL_COURSE_TITLE)));
    		m.put(DB_COL_LEARNED_CONTENT_COUNT, String.valueOf(c.getInt(c.getColumnIndex(DB_COL_LEARNED_CONTENT_COUNT))));
    		m.put(DB_COL_LEARNED_CONTENT_COUNT, String.valueOf(c.getInt(c.getColumnIndex(DB_COL_CONTENT_COUNT))));
			list.add(m);
    	}
    	return list;
    }
    
	/**
	 * List all {@link CourseStatus}
	 * @return
	 */
    public Cursor fetchCourseStatus(){
    	return mDb.query(DB_TABLE_COURSE_STATUS,null,null,null,null,null,null);
    }
    
    public long saveOrUpdateCourseStatus(CourseStatus cs){
    	if (cs.isNew()){
    		return insertCourseStatus(cs);
    	}else{
    		updateCourseStatus(cs);
    		return cs.getRowId();
    	}
    }
    
    public long insertCourseStatus(CourseStatus cs){
		ContentValues args=new ContentValues();
		args.put(DB_COL_COURSE_KEY, cs.getCourseKey());
		args.put(DB_COL_COURSE_MD5, cs.getCourseMd5());
		args.put(DB_COL_COURSE_TITLE, cs.getCourseTitle());
		args.put(DB_COL_COURSE_FILE_NAME, cs.getCourseFileName());
		args.put(DB_COL_LEARNED_CONTENT_COUNT, cs.getLearnedWordsCount());
		args.put(DB_COL_NEXT_CONTENT_OFFSET, cs.getNextContentOffset());
		args.put(DB_COL_LAST_WORD, cs.getLastWord());
		args.put(DB_COL_CONTENT_COUNT, cs.getContentCount());
		args.put(DB_COL_CREATED_AT, cs.getCreatedAt());
		args.put(DB_COL_UPDATED_AT, cs.getUpdatedAt());
		return mDb.insert(DB_TABLE_COURSE_STATUS, null, args);
    }
    
    public boolean updateCourseStatus(CourseStatus cs){
		ContentValues args=new ContentValues();
		//args.put(KEY_COURSE_NAME, cs.getCourseName());
		//args.put(KEY_COURSE_FILE_NAME, cs.getCourseFileName());
		args.put(DB_COL_LEARNED_CONTENT_COUNT, cs.getLearnedWordsCount());
		args.put(DB_COL_NEXT_CONTENT_OFFSET, cs.getNextContentOffset());
		args.put(DB_COL_LAST_WORD, cs.getLastWord());
		//args.put(KEY_CONTENT_COUNT, cs.getContentCount());
		args.put(DB_COL_UPDATED_AT, cs.getUpdatedAt());
		return mDb.update(DB_TABLE_COURSE_STATUS, args, KEY_ROWID + "=" + cs.getRowId(), null)>0;    	
    }	
    
    /**
     * TODO should also delete Section and Word of this Course
     * @param _id
     * @return
     */
    public boolean deleteCourseStatus(long _id){
    	mDb.delete(DB_TABLE_SECTION, DB_COL_COURSE_KEY + "=" + _id, null);
    	mDb.delete(DB_TABLE_WORDS, DB_COL_COURSE_KEY + "=" + _id, null);
    	return mDb.delete(DB_TABLE_COURSE_STATUS, KEY_ROWID + "=" + _id, null)>0;
    }
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_SECTIONS_TABLE);
			db.execSQL(DATABASE_CREATE_UNIT_WORDS_TABLE);
			db.execSQL(DATABASE_CREATE_COURSE_STATUS_TABLE);
			db.execSQL(DATABASE_CREATE_EXAMPLES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS sections");
			db.execSQL("DROP TABLE IF EXISTS words");
			db.execSQL("DROP TABLE IF EXISTS course_status");
			db.execSQL("DROP TABLE IF EXISTS examples");
			onCreate(db);
		}
	}
	
	private static final int DATABASE_VERSION = 22;
    private static final String DATABASE_NAME = "data";
		
}
