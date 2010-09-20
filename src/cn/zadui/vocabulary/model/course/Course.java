package cn.zadui.vocabulary.model.course;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.util.Log;
import cn.zadui.vocabulary.model.Helper;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * 
 * @author Huang Gehua
 * Abstract class of a course. Course is the main index that lead the 
 * study process of the user.
 * 
 * TODO each course as a unique key which was generated at server side.
 *
 */
public abstract class Course {

	/*
	 * XML tag name in course list XML file.
	 */
	
	public static final String COURSE_MD5_TAG="md5";
	public static final String COURSE_KEY_TAG="key";
	public static final String COURSE_TITLE_TAG="title";
	public static final String COURSE_LANGUAGE_TAG="language";
	public static final String COURSE_REGION_TAG="region";
	public static final String COURSE_LEVEL_TAG="level";
	public static final String COURSE_VERISON_TAG="version";
	public static final String COURSE_TYPE_TAG="type";
	public static final String COURSE_CONTENT_COUNT_TAG="content_count";
	public static final String COURSE_SEPARATOR_TAG="separator";
	public static final String COURSE_DESC_TAG="summary";
	public static final String COURSE_FILE_NAME_TAG="filename";
	public static final String COURSE_URL_TAG="url";
	
	protected String courseAbsolutFilePath;
	protected int headSize;
	protected int contentBeginAt;
	
	protected String key;
	protected String name;
	protected String lang;
	protected String region;
	protected int version;
	protected int contentCount;
	protected String level;
	protected String type;	
	protected char[] separator;
	
	protected byte[] buffer=new byte[8192];


	protected Course(String absolutFilePath) throws IOException{
		courseAbsolutFilePath=absolutFilePath;
		loadCourseInfo();
		contentBeginAt=headSize+2;
	}
	
	protected void loadCourseInfo() throws IOException{
		Log.d("ABSOLUT PATH IS =>",courseAbsolutFilePath);
		InputStream in=new FileInputStream(courseAbsolutFilePath);
		byte[] cache=new byte[50];
		in.read(cache,0,2);
		headSize=Helper.byteArray2Short(cache,0);
		in.read(buffer,0,headSize);
		int point=0;
		String key="";
		String value="";
		for(int i=0;i<headSize;i++){
			char c=(char)buffer[i];
			if (c==':'){
				key=new String(cache,0,point,"UTF-8");
				point=0;
			}else if (c=='\n'){
				value=new String(cache,0,point,"UTF-8");
				point=0;
				if (key.equals(StudyDbAdapter.DB_COL_COURSE_TITLE)) name=value;
				else if (key.equals(COURSE_KEY_TAG)) key=value;
				else if (key.equals(COURSE_LANGUAGE_TAG)) lang=value;
				else if (key.equals(COURSE_REGION_TAG)) region=value;
				else if (key.equals(COURSE_LEVEL_TAG)) level=value;
				else if (key.equals(COURSE_VERISON_TAG)) version=Integer.valueOf(value);
				else if (key.equals(COURSE_TYPE_TAG)) type=(value);
				else if (key.equals(COURSE_CONTENT_COUNT_TAG)) contentCount=Integer.valueOf(value);
				else if (key.equals(COURSE_SEPARATOR_TAG)){
					String[] s=value.split(":");
					separator=new char[s.length];
					for(int j=0;j<s.length;j++)separator[j]=(char)(int)(Integer.valueOf(s[j]));
				}
			}else{
				cache[point]=(byte)c;
				point++;
			}
		}
		in.close();
	}
	
	/**
	 * Get next head word from the course
	 * @param learnedWordsCount means how many words has been learned of 
	 * this course. It is like a pointer.
	 * @return String of the head word
	 */
	public abstract String getContentByNumber(int learnedWordsCount);
	
	/**
	 * Get content from the specific position.
	 * @param position.
	 * @return String of content
	 * @throws EOFCourseException 
	 */
	public String getContent(long position) throws EOFCourseException{
		try {
			RandomAccessFile rio=new RandomAccessFile(courseAbsolutFilePath,"r");
			rio.seek(position+this.contentBeginAt);
			int l=rio.read(buffer);
			//Throw EOFCourseException if reached the end of the course.
			if (l==-1) throw new EOFCourseException("reached the end of the course.");
			int end=0;
			boolean found=false;
			for(end=0;end<l;end++){
				if (buffer[end]!=(byte)separator[0]){
					continue;
				}else{
					if (separator.length==1) break;
					found=true;
					for(int i=1;i<separator.length;i++){
						if (buffer[end+i]!=separator[i]){
							found=false;
							break;
						}
					}
					if (found) break;
				}
			}
			String s=new String(buffer,0,end,"utf-8");//.trim();
			return s;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Dict.ERROR_WORD;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Dict.ERROR_WORD;
		}
	}
	
	/**
	 * Get study progress regarding how many words has been learned. 
	 * @param learnedCount
	 * @return percentage of the study progress 
	 */
	public abstract int getProgress(int learnedCount);
	
	/**
	 * @return Course name
	 */
	public String getName(){
		return name;
	}

	/**
	 * 
	 * @return language of the course
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * 
	 * @return course words count
	 */
	public int getContentCount() {
		return contentCount;
	}

	public String getRegion() {
		return region;
	}

	public String getLevel() {
		return level;
	}

	public int getVersion() {
		return version;
	}

	public String getType() {
		return type;
	}

	public char[] getSeparator() {
		return separator;
	}
	
	public class CourseException extends Exception{
		public CourseException(String message){
			super(message);
		}
	}
	
	public class EOFCourseException extends CourseException{
		public EOFCourseException(String message){
			super(message);
		}
	}

	public String getCourseFileName() {
		return courseAbsolutFilePath;
	}

	public String getKey() {
		return key;
	}
	
}
