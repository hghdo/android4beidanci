package cn.zadui.vocabulary.model.course;

import java.io.IOException;

import cn.zadui.vocabulary.model.dictionary.Dict;


/**
 * 
 * @author david
 * The Course represents the current study status including study progress
 * Course format: Plain text file. The top several lines 
 * was the course basic informations(course name,language,word counts,min dictionary version etc.).
 * Following each line is a word.
 */
public class SimpleCourse extends Course {
	
	public static int[] studyInterval=new int[]{
		5  * 60 * 60 * 1000,
		10 * 60 * 60 * 1000
		};
	public static int firstInterval=studyInterval[0];
	
	private static SimpleCourse course=null;
	
	private SimpleCourse(String absolutFilePath) throws IOException{
		super(absolutFilePath);
	}
	
//	public static Course getInstance(CourseStatus status){
//		return getInstance(status.getCourseFileName());
//	}

	public static Course getInstance(String absolutFilePath){
		if(course==null || !course.getCourseFileName().equalsIgnoreCase(absolutFilePath)){
			try {
				course=new SimpleCourse(absolutFilePath);
			} catch (IOException e) {
				course=null;
				e.printStackTrace();
			}
		}
		return course; 
	}
	
	/**
	 * TODO the performance can be enhancement by using InputStream to 
	 * read more bytes and then fetch words from bytes.
	 */
	public String getContentByNumber(int learnedWordsCount){
		return Dict.ERROR_WORD;
		/*
		LineNumberReader reader;
		try {
			reader = new LineNumberReader(new FileReader(Status.DATA_DIR+courseFileName));
			for(int i=0;i<(HEADWORD_LINE_NUM_OFFSET+learnedWordsCount);i++)reader.readLine();
			String result=reader.readLine();
			reader.close();
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return Dict.ERROR_WORD;
		} catch (IOException ioe){
			return Dict.ERROR_WORD;
		} 
		*/
	}
	
	public int getProgress(int learnedCount){
		return (learnedCount * 100)/contentCount;
	}
}
