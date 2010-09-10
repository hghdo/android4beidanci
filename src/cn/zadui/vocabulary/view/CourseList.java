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
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
	
	public static final String COURSE_LIST_CACHE="courseList.cache"; 
	
	public static final String COURSE_NODE_NAME_OF_XML="course";
	public static final String ROOT_NODE_NAME_OF_XML="course-list";
	
	private static final String LOG_TAG="CourseList";
	private List<Map<String,String>> courseList=new ArrayList<Map<String,String>>();
	
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
		selectedCourse=courseList.get(id);
		LayoutInflater factory = LayoutInflater.from(this);
		final View courseInfoView = factory.inflate(R.layout.course_info, null);
//		TextView tvName=(TextView)courseInfoView.findViewById(R.id.tv_course_list_dialog_name);
//		tvName.setText(courseList.get(id).get(Course.NAME_KEY));
		TextView tvDesc=(TextView)courseInfoView.findViewById(R.id.tv_course_list_dialog_desc);
		tvDesc.setText(courseList.get(id).get(Course.DESC_KEY));
		//tvDesc.setText(getResources().getString(R.string.long_string));
		return new AlertDialog.Builder(this)
			.setTitle(courseList.get(id).get(Course.NAME_KEY))
			.setView(courseInfoView)
			.setPositiveButton(R.string.select_it, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	// TODO if the file is existed then ask user whether override it.
	            	// Also should add version to the file name like: xxxxxx_version-code.cou
	            	// the version-code will become larger if the content is modified at server side.
	            	// TODO check whether this course is already in user db. If already existed should not 
	            	// create duplicated CourseStatus
	            	File courseFile=new File(CourseStatus.DATA_DIR+selectedCourse.get(Course.FILE_NAME_KEY));
	            	if (!courseFile.exists()){
	            		InputStream in=null;
	            		OutputStream out=null;
		            	try {
		            		String cu=selectedCourse.get(Course.COURSE_URL_KEY);
		            		in=NetworkHelper.buildUrlConnection(cu).getInputStream();
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
	            	}
	            	
					Course course=SimpleCourse.getInstance(courseFile.getAbsolutePath());
					//Update courseStatus
					StudyDbAdapter dbAdapter=new StudyDbAdapter(CourseList.this);
					dbAdapter.open();
					//TODO for learned course can't create new CourseStatus.
					PrefStore.saveSelectedCourseStatusId(CourseList.this, (new CourseStatus(course)).save(dbAdapter));
					PrefStore.saveSelectedCourseName(CourseList.this, course.getName());
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		selectedCourse=courseList.get(position);
		Log.d(LOG_TAG,"position==>"+String.valueOf(position));
		Log.d(LOG_TAG,"id==>"+String.valueOf(id));
		showDialog(position);
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
		InputStream in=null;
		OutputStream out=null;
		
		if (forceRemote){
			downloadRemoteCourseList(in,out);
		}else{
			try {
				in = this.openFileInput(COURSE_LIST_CACHE);
			} catch (FileNotFoundException e) {
				downloadRemoteCourseList(in,out);
			}
		}
		parseCourseListXmlFile(in);
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
					if (xpp.getName().equals(COURSE_NODE_NAME_OF_XML)){
						course=new HashMap<String, String>();
					}
					break;
					//else if (xpp.getName().equals(Course.KEY_NAME)) course.put(xpp.getName(), value)
				case XmlPullParser.TEXT:
					attr=xpp.getText();
					break;
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals(COURSE_NODE_NAME_OF_XML)){
						courseList.add(course);
						break;
					}else if (xpp.getName().equals(ROOT_NODE_NAME_OF_XML)){
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
				Course.NAME_KEY,
				Course.LANGUAGE_KEY,
				//Course.REGION_KEY,
				//Course.LEVEL_KEY,
				Course.CONTENT_COUNT_KEY};
		int[] displayViews=new int[] {R.id.tv_course_list_course_name,R.id.tv_course_list_course_lang,R.id.tv_course_list_content_count};
		ListAdapter adapter=new SimpleAdapter(this,courseList,R.layout.course_list_item,from,displayViews);
		setListAdapter(adapter);		
	}
	
	
}
