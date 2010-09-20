package cn.zadui.vocabulary.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.course.Course;
import cn.zadui.vocabulary.model.course.SimpleCourse;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.PrefStore;
import cn.zadui.vocabulary.storage.StudyDbAdapter;
import cn.zadui.vocabulary.util.NetworkHelper;

public class CourseList extends ListActivity implements View.OnClickListener {
	
	static final String TAG="CourseList";
	public static final String COURSE_LIST_CACHE="courseList.cache"; 	
	public static final String COURSE_TAG_NAME_OF_XML="course";
	public static final String ROOT_TAG_NAME_OF_XML="course-list";
	
	private static final int NEW_DIALOG=0;
	private static final int OLD_DIALOG=1;
	private static final int DOWNLOAD_COURSE_LIST_PROGRESS_DIALOG=2;
	private static final String LOG_TAG="CourseList";
	private List<Map<String,String>> courseList=new ArrayList<Map<String,String>>();
	
	ProgressDialog progressDialog;
	SharedPreferences spSettings ;
	Map<String,String> selectedCourse;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_list);
		setTitle(getResources().getString(R.string.course_list_title));
		
		Button refresh=(Button)findViewById(R.id.btn_course_list_refresh);
		refresh.setOnClickListener(this);
						
		getCourseList(false);
		fillData();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case DOWNLOAD_COURSE_LIST_PROGRESS_DIALOG:
        	progressDialog = new ProgressDialog(this);
    		progressDialog.setMessage("Please wait while fetch courses...");
        	progressDialog.setIndeterminate(true);
        	progressDialog.setCancelable(true);
        	return progressDialog;			
		case OLD_DIALOG:
			return new AlertDialog.Builder(this)
			.setTitle(selectedCourse.get(Course.COURSE_TITLE_TAG))
			.setMessage("This course is already in your study list!")
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//CourseList.this.setResult(RESULT_OK);
				}
			})
			.create();
		case NEW_DIALOG:
			LayoutInflater factory = LayoutInflater.from(this);
			final View courseInfoView = factory.inflate(R.layout.course_info, null);
//			TextView tvName=(TextView)courseInfoView.findViewById(R.id.tv_course_list_dialog_name);
//			tvName.setText(courseList.get(id).get(Course.NAME_KEY));
			TextView tvDesc=(TextView)courseInfoView.findViewById(R.id.tv_course_list_dialog_desc);
			tvDesc.setText(selectedCourse.get(Course.COURSE_DESC_TAG));
			//tvDesc.setText(getResources().getString(R.string.long_string));
			return new AlertDialog.Builder(this)
			.setTitle(selectedCourse.get(Course.COURSE_TITLE_TAG))
			.setView(courseInfoView)
			.setPositiveButton(R.string.select_it, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					File courseFile=new File(CourseStatus.DATA_DIR+selectedCourse.get(Course.COURSE_FILE_NAME_TAG));
					InputStream in=null;
					OutputStream out=null;
					try {
						in=NetworkHelper.buildUrlConnection(selectedCourse.get(Course.COURSE_URL_TAG)).getInputStream();
						byte[] buf=new byte[4*1024];
						int readBytes=0;
						out=new FileOutputStream(courseFile);
						while((readBytes=in.read(buf))!=-1){
							out.write(buf,0,readBytes);
						}
						out.flush();
					} catch (IOException e) {
						// TODO Can't down load this course file. Let user check network.
						e.printStackTrace();
					} finally{
						try {
							if(in!=null)in.close();
							if(out!=null)out.close();
						} catch (IOException e1) {}
					}
					
					//Course course=SimpleCourse.getInstance(courseFile.getAbsolutePath());
					
					//Update courseStatus
					StudyDbAdapter dbAdapter=new StudyDbAdapter(CourseList.this);
					dbAdapter.open();
					CourseStatus cs=new CourseStatus(
							selectedCourse.get(Course.COURSE_KEY_TAG),
							selectedCourse.get(Course.COURSE_MD5_TAG),
							selectedCourse.get(Course.COURSE_TITLE_TAG),
							selectedCourse.get(Course.COURSE_FILE_NAME_TAG),
							Integer.valueOf(selectedCourse.get(Course.COURSE_CONTENT_COUNT_TAG))
					);
					cs.save(dbAdapter);
					//PrefStore.saveSelectedCourseStatusId(CourseList.this, cs.save(dbAdapter));
					PrefStore.saveSelectedCourseKey(CourseList.this, selectedCourse.get(Course.COURSE_KEY_TAG));
					dbAdapter.close();
					CourseList.this.setResult(RESULT_OK);
					CourseList.this.finish();
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//CourseList.this.setResult(RESULT_OK);
				}
			})
			.create();
		}
		return null;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		selectedCourse=courseList.get(position);
		// Verify whether it is a selected course
		String key=selectedCourse.get(Course.COURSE_KEY_TAG);
		Log.d(TAG,key);
		StudyDbAdapter dbAdapter=new StudyDbAdapter(CourseList.this);
		dbAdapter.open();
		Cursor c=dbAdapter.findCourseStatusByKey(key);
		if (c.getCount()>0){
			showDialog(OLD_DIALOG);
		}else{
			showDialog(NEW_DIALOG);			
		}
		dbAdapter.close();
	}

	@Override
	public void onClick(View v) {
		getCourseList(true);
		fillData();
	}
	
	/**
	 * 
	 * @param forceRemote Whether force get list from remote web site.
	 */
	private void getCourseList(boolean forceRemote){
		showDialog(DOWNLOAD_COURSE_LIST_PROGRESS_DIALOG);
		new CourseListBuilder(forceRemote).start();
	}
	
	private void downloadRemoteCourseList(InputStream in,OutputStream out){
		try {
			in=NetworkHelper.buildUrlConnection(NetworkHelper.courseListUrl(NetworkHelper.XML_FORMAT)).getInputStream();
    		byte[] buf=new byte[4*1024];
    		int readBytes=0;
    		out=openFileOutput(COURSE_LIST_CACHE,MODE_PRIVATE);
    		while((readBytes=in.read(buf))!=-1){
    			out.write(buf,0,readBytes);
    		}
    		out.flush();
		} catch (IOException e2) {
			// TODO Can't get course list from web site. Should pop up notification.
			e2.printStackTrace();
			return;
		} finally{
			try {
				if(in!=null)in.close();
				if(out!=null)out.close();
			} catch (IOException e1) {}
		}			
	}
	
	private void parseCourseListXmlFile(InputStream in){
		try {
			in = this.openFileInput(COURSE_LIST_CACHE);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp=factory.newPullParser();
			xpp.setInput(in,"UTF-8");
			int eventType = xpp.getEventType();
			String attr="";
			Map<String,String> course=null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType){
				case XmlPullParser.START_DOCUMENT:
					courseList.clear();
					break;
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals(COURSE_TAG_NAME_OF_XML)){
						course=new HashMap<String, String>();
					}
					break;
					//else if (xpp.getName().equals(Course.KEY_NAME)) course.put(xpp.getName(), value)
				case XmlPullParser.TEXT:
					attr=xpp.getText();
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals(COURSE_TAG_NAME_OF_XML)){
						courseList.add(course);
						break;
					}else if (xpp.getName().equals(ROOT_TAG_NAME_OF_XML)){
						break;
					}else{
						Log.d(LOG_TAG,xpp.getName()+"==>"+attr);
						if (course!=null)course.put(xpp.getName(), attr);
						break;
					}
				}
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Can't parse this course list xml.
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Can't parse this course list xml.
			e.printStackTrace();
		}finally{
			try {
				if(in!=null)in.close();
			} catch (IOException e) {}
		}

	}
	
	private void fillData(){
		String[] from=new String[]{
				Course.COURSE_TITLE_TAG,
				Course.COURSE_LANGUAGE_TAG,
				//Course.REGION_KEY,
				//Course.LEVEL_KEY,
				Course.COURSE_CONTENT_COUNT_TAG};
		int[] displayViews=new int[] {R.id.tv_course_list_course_name,R.id.tv_course_list_course_lang,R.id.tv_course_list_content_count};
		ListAdapter adapter=new SimpleAdapter(this,courseList,R.layout.course_list_item,from,displayViews);
		setListAdapter(adapter);		
	}
	
	class CourseListBuilder extends Thread{
		
		boolean forceRemote=false;
		
		public CourseListBuilder(boolean fr){
			forceRemote=fr;
		}
		
		@Override
		public void run() {
			InputStream in=null;
			OutputStream out=null;

			if (forceRemote){
				downloadRemoteCourseList(in,out);
			}else{
				try {
					in = CourseList.this.openFileInput(COURSE_LIST_CACHE);
				} catch (FileNotFoundException e) {
					downloadRemoteCourseList(in,out);
				}
			}
			parseCourseListXmlFile(in);
			CourseList.this.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					CourseList.this.fillData();
					CourseList.this.dismissDialog(DOWNLOAD_COURSE_LIST_PROGRESS_DIALOG);
				}
				
			});
		}
		
	}
	
}
