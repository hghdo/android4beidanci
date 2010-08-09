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
	
	private static final int DATABASE_VERSION = 17;
    private static final String DATABASE_NAME = "data";
    
    public static final int UNIT_WORDS_FILTER_ALL=0;
    public static final int UNIT_WORDS_FILTER_FORGOT_ONLY=1;
    public static final int UNIT_WORDS_FILTER_MASTERED_EXCLUDED=2;
   
    public static final String KEY_ROWID = "_id";
    
    private static final String UNIT_TABLE = "unit";
    //public static final String KEY_USER_ID = "user_id";
    //public static final String KEY_COURSE_ID="course_id";
    public static final String KEY_COURSE_NAME="course_name";
    public static final String KEY_CREATE_STYLE="create_style";    
    public static final String KEY_WORDS_COUNT="words_count";
    public static final String KEY_VIRGIN_FLAG="virgin_flag";
    public static final String KEY_FINISHED="finished";
    public static final String KEY_COMMON_EXAM_TIMES="common_exam_times";
    public static final String KEY_LAST_EXAM_AT="last_exam_at";
    public static final String KEY_NEXT_COMMON_EXAM_AT="next_common_exam_at";
    //public static final String KEY_LAST_FAILED_EXAM_AT="last_failed_exam_at";
    public static final String KEY_NEXT_FAILED_EXAM_AT="next_failed_exam_at";
    public static final String KEY_CREATED_AT="created_at";
    
    public static final String UNIT_WORDS_TABLE = "unit_words";
    public static final String KEY_Unit_ID="user_unit_id";
    public static final String KEY_WORD="word";
    public static final String KEY_MEANING="meaning";
    public static final String KEY_PHONETIC="phonetic";
    public static final String KEY_LAST_EXAM_FAILED="last_exam_failed";
    public static final String KEY_EXAM_TIMES="exam_times";
    public static final String KEY_SUCCESS_TIMES="success_times";
    public static final String KEY_FAILED_TIMES="failed_times";
    public static final String KEY_MASTERED="mastered";
    
    //columns for course status
    /*
	public static final String SP_KEY_CURRENT_COURSE_FILE_NAME="currentCourseFileName";
	public static final String SP_KEY_CURRENT_COURSE_NAME="currentCourseName";
	public static final String SP_KEY_LEARNED_COUNT="learnedWordsCount";
	public static final String SP_KEY_NEXT_CONTENT_OFFSET="nextContentOffset";
	public static final String SP_KEY_LAST_WORD="lastWord";
    
    */
    private static final String COURSE_STATUS_TABLE = "course_status";
    //public static final String KEY_COURSE_NAME="course_name";
    public static final String KEY_COURSE_FILE_NAME="course_file_name";
    public static final String KEY_LEARNED_CONTENT_COUNT="learned_content_count";
    public static final String KEY_NEXT_CONTENT_OFFSET="next_content_offset";
    public static final String KEY_LAST_WORD="last_word";
    public static final String KEY_CONTENT_COUNT="content_count";   
    
    public static final String EXAMPLES_TABLE = "examples";
    public static final String KEY_SENTENCE = "sentence";
    public static final String KEY_TIMESTAMP = "timestamp";
    
    private static final String DATABASE_CREATE_UNIT_TABLE =
        				"create table unit (_id integer primary key autoincrement, " + 
        				//"user_id integer, " +
        				"create_style integer, "+
                		"course_name text," +
                		"words_count integer default 0," +
                		"virgin_flag integer default 1," +
                		"finished integer default 0," +
                		"common_exam_times integer default 0," +
                		"last_exam_at integer default 0," +
                		"next_common_exam_at integer default 0," +
                		//"last_failed_exam_at integer default 0," +
                		"next_failed_exam_at integer default 0," +
                		"created_at integer);";
    
    private static final String DATABASE_CREATE_UNIT_WORDS_TABLE =
                		"create table unit_words (_id integer primary key autoincrement,"+
                		"user_unit_id integer," +
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
                		"course_name text," +
                		"course_file_name text," +
                		"learned_content_count integer default 0," +
                		"next_content_offset integer default 0," +
                		"last_word text," +
                		"content_count integer default 0);";
        
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
    	return mDb.query(UNIT_TABLE, null, KEY_VIRGIN_FLAG+"=0", null, null, null, KEY_CREATED_AT+" desc");
    }
    
    public Cursor fetchSectionsByCourse(String courseName){
    	return mDb.query(UNIT_TABLE, null, "virgin_flag=0 and course_name='"+courseName+"'", null, null, null, KEY_CREATED_AT+" desc");
    }
    
    public Cursor fetchSection(long rowId){
    	Cursor c=mDb.query(true, UNIT_TABLE, new String[]{KEY_ROWID,KEY_COURSE_NAME,KEY_CREATE_STYLE,KEY_WORDS_COUNT,KEY_VIRGIN_FLAG,KEY_COMMON_EXAM_TIMES,KEY_CREATED_AT,KEY_WORDS_COUNT}, KEY_ROWID+"="+rowId, null, null, null, null, null);
    	return c;
    }
    
    /**
     * Get the latest Study Unit from the DB.
     * @return the latest created study unit from the DB or null if the DB is empty.
     */
    public Cursor getLatestSection(String courseName){
    	//String [] columns=new String[]{KEY_ROWID,KEY_COURSE_NAME,KEY_CREATE_STYLE,KEY_WORDS_COUNT,KEY_VIRGIN_FLAG,KEY_COMMON_EXAM_TIMES,KEY_CREATED_AT};
    	Cursor c=mDb.query(UNIT_TABLE, null, "course_name=?", new String[]{courseName}, null, null, KEY_CREATED_AT+" desc");
    	return c;
    }
    	
	public void createSectionInDb(Section section){
		ContentValues args=new ContentValues();
		args.put(KEY_COURSE_NAME, section.getCourseName());
		args.put(KEY_CREATE_STYLE, section.getCreatedStyle());
		args.put(KEY_WORDS_COUNT, 0);
		args.put(KEY_VIRGIN_FLAG, 1);
		args.put(KEY_FINISHED, 0);
		args.put(KEY_COMMON_EXAM_TIMES, 0);
		//args(KEY_LAST_COMMON_exam_at, value);
		//args.put(KEY_NEXT_COMMON_EXAM_AT, su.getCreatedAt()+SimpleCourse.firstInterval);
		//args(KEY_LAST_FAILED_REVIEW_AT, value);
		//args(KEY_NEXT_FAILED_REVIEW_AT, value);
		args.put(KEY_CREATED_AT, section.getCreatedAt());
		section.setRowId(mDb.insert(UNIT_TABLE, null, args));
	}
	
	public boolean updateSectionToOld(long id,int nextExamAt){
		ContentValues args=new ContentValues();
		args.put(KEY_VIRGIN_FLAG, 0);
		args.put(KEY_NEXT_FAILED_EXAM_AT, nextExamAt);
		return mDb.update(UNIT_TABLE, args, KEY_ROWID + "=" + id, null) > 0;
	}
	
	public boolean updateSectionWordsCount(long id,int newCount){
		ContentValues args=new ContentValues();
		args.put(KEY_WORDS_COUNT, newCount);
		return mDb.update(UNIT_TABLE, args, KEY_ROWID + "=" + id, null) > 0;
	}
	
	public boolean updateSection(long id,int arg,String columnName){
		ContentValues args=new ContentValues();
		args.put(columnName, arg);
		return mDb.update(UNIT_TABLE, args, KEY_ROWID + "=" + id, null) > 0;
	}
    
    public Cursor fetchSectionWords(long unitId,int filter){   
    	switch (filter){
	    	case UNIT_WORDS_FILTER_FORGOT_ONLY:
	    		return mDb.query(UNIT_WORDS_TABLE, null, KEY_Unit_ID+"=? and "+KEY_LAST_EXAM_FAILED+"=?", new String[]{String.valueOf(unitId),"1"}, null, null, null);
	    	case UNIT_WORDS_FILTER_ALL:
	    		return mDb.query(UNIT_WORDS_TABLE, null, KEY_Unit_ID+"="+unitId, null, null, null, null);
	    	case UNIT_WORDS_FILTER_MASTERED_EXCLUDED:
	    		return mDb.query(UNIT_WORDS_TABLE, null, KEY_Unit_ID+"=? and "+KEY_MASTERED+"=?", new String[]{String.valueOf(unitId),"0"}, null, null, null);
	    	default: return null;
    	}
    }
	
	public long insertWord(long unitId,Word word){
		ContentValues args=new ContentValues();
		args.put(KEY_Unit_ID, unitId);
		args.put(KEY_WORD, word.getHeadword());
		args.put(KEY_MEANING, word.getMeaning());
		args.put(KEY_PHONETIC, word.getPhonetic());
		args.put(KEY_LAST_EXAM_FAILED, 1);
		args.put(KEY_EXAM_TIMES, 0);
		args.put(KEY_SUCCESS_TIMES, 0);
		args.put(KEY_FAILED_TIMES, 0);
		args.put(KEY_MASTERED, 0);
		word.setId(mDb.insert(UNIT_WORDS_TABLE, null, args));
		return word.getId();
	}
	
	public Word previousWordInSection(long unitId,Word word){
		Cursor c=mDb.query(UNIT_WORDS_TABLE, null, KEY_Unit_ID+"=? and "+KEY_ROWID+"<?", new String[]{String.valueOf(unitId),String.valueOf(word.getId())}, null, null, "_id desc");
		return c.moveToFirst() ? new Word(c) : null;
	}
	
	public Word nextWordInSection(long unitId,Word word){
		Cursor c=mDb.query(UNIT_WORDS_TABLE, null, KEY_Unit_ID+"=? and "+KEY_ROWID+">?", new String[]{String.valueOf(unitId),String.valueOf(word.getId())}, null, null, "_id");
		return c.moveToFirst() ? new Word(c) : null;
		
	}
	
	public boolean updateWordStatus(int status,Word word){
		ContentValues args=new ContentValues();
		args.put(KEY_EXAM_TIMES, word.getReviewTimes()+1);
		switch (status){
		case Word.MASTERED:
			args.put(KEY_MASTERED, 1);
			args.put(KEY_LAST_EXAM_FAILED, 0);
			break;
		case Word.PASS:
			args.put(KEY_SUCCESS_TIMES, word.getSuccessTimes()+1);
			args.put(KEY_MASTERED, 0);
			args.put(KEY_LAST_EXAM_FAILED, 0);
			break;
		case Word.FORGOT:
			args.put(KEY_FAILED_TIMES, word.getFailedTimes()+1);
			args.put(KEY_MASTERED, 0);
			args.put(KEY_LAST_EXAM_FAILED, 1);
		}
		return mDb.update(UNIT_WORDS_TABLE, args, KEY_ROWID + "=" + word.getId(), null) > 0;
	}
	
	public  Cursor findCourseStatusByCourseName(String courseName){
		Cursor c=mDb.query(COURSE_STATUS_TABLE,null,"course_name='"+courseName+"'",null,null,null,null);
		return c;
	}
	
	public Cursor findCourseStatus(long rowId){
		Cursor c=mDb.query(COURSE_STATUS_TABLE,null,"_id="+String.valueOf(rowId),null,null,null,null);
		return c;
	}
    
    public LinkedList<Map<String,String>> fetchCourseStatusList(){
    	Cursor c=mDb.query(COURSE_STATUS_TABLE,null,null,null,null,null,null);
    	LinkedList<Map<String,String>> list=new LinkedList<Map<String,String>>();
    	while(c.moveToNext()){
    		Map<String,String> m=new HashMap<String,String>();
    		m.put(KEY_COURSE_NAME, c.getString(c.getColumnIndex(KEY_COURSE_NAME)));
    		m.put(KEY_LEARNED_CONTENT_COUNT, String.valueOf(c.getInt(c.getColumnIndex(KEY_LEARNED_CONTENT_COUNT))));
    		m.put(KEY_LEARNED_CONTENT_COUNT, String.valueOf(c.getInt(c.getColumnIndex(KEY_CONTENT_COUNT))));
			list.add(m);
    	}
    	return list;
    }
    
	/**
	 * List all {@link CourseStatus}
	 * @return
	 */
    public Cursor fetchCourseStatus(){
    	return mDb.query(COURSE_STATUS_TABLE,null,null,null,null,null,null);
    }
    
    public long saveOrUpdateCourseStatus(CourseStatus cs){
    	if (cs.isNew()){
    		cs.setRowId(insertCourseStatus(cs));
    		return cs.getRowId();
    	}else{
    		updateCourseStatus(cs);
    		return cs.getRowId();
    	}
    }
    
    public long insertCourseStatus(CourseStatus cs){
		ContentValues args=new ContentValues();
		args.put(KEY_COURSE_NAME, cs.getCourseName());
		args.put(KEY_COURSE_FILE_NAME, cs.getCourseFileName());
		args.put(KEY_LEARNED_CONTENT_COUNT, cs.getLearnedWordsCount());
		args.put(KEY_NEXT_CONTENT_OFFSET, cs.getNextContentOffset());
		args.put(KEY_LAST_WORD, cs.getLastWord());
		args.put(KEY_CONTENT_COUNT, cs.getContentCount());
		return mDb.insert(COURSE_STATUS_TABLE, null, args);
    }
    
    public boolean updateCourseStatus(CourseStatus cs){
		ContentValues args=new ContentValues();
		args.put(KEY_COURSE_NAME, cs.getCourseName());
		args.put(KEY_COURSE_FILE_NAME, cs.getCourseFileName());
		args.put(KEY_LEARNED_CONTENT_COUNT, cs.getLearnedWordsCount());
		args.put(KEY_NEXT_CONTENT_OFFSET, cs.getNextContentOffset());
		args.put(KEY_LAST_WORD, cs.getLastWord());
		args.put(KEY_CONTENT_COUNT, cs.getContentCount());
		return mDb.update(COURSE_STATUS_TABLE, args, KEY_ROWID + "=" + cs.getRowId(), null)>0;    	
    }	
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_UNIT_TABLE);
			db.execSQL(DATABASE_CREATE_UNIT_WORDS_TABLE);
			db.execSQL(DATABASE_CREATE_COURSE_STATUS_TABLE);
			db.execSQL(DATABASE_CREATE_EXAMPLES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS unit");
			db.execSQL("DROP TABLE IF EXISTS unit_words");
			db.execSQL("DROP TABLE IF EXISTS course_status");
			db.execSQL("DROP TABLE IF EXISTS examples");
			onCreate(db);
		}
	}
		
}
