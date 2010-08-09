package cn.zadui.vocabulary.view;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class LearnedCourses extends ListActivity {
	
	//LinkedList<Map<String,String>> list;
	static final int SELECT_COURSE_REQUEST=0;
	StudyDbAdapter dbAdapter;
	private Cursor cur;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learned_courses);
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		((Button)findViewById(R.id.btn_learned_course_new_course)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent sc=new Intent(v.getContext(), CourseList.class);
				startActivityForResult(sc, SELECT_COURSE_REQUEST);				
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}

	private void fillData() {
		int[] displayViews=new int[]{
				R.id.tv_selected_courses_course_name,
				R.id.tv_selected_courses_learned_count,
				R.id.tv_selected_courses_content_count
				//R.id.tv_unit_created_at,
				//R.id.tv_unit_next_exam_at,
				//R.id.tv_unit_exam_times
				};
		String[] columns=new String[]{	
				//StudyDbAdapter.KEY_ROWID,
				StudyDbAdapter.KEY_COURSE_NAME,
				StudyDbAdapter.KEY_LEARNED_CONTENT_COUNT,
				StudyDbAdapter.KEY_CONTENT_COUNT,
				//StudyDbAdapter.KEY_CREATED_AT,
				//StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT,
				//StudyDbAdapter.KEY_COMMON_EXAM_TIMES
				};
		StudyDbAdapter dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		cur=dbAdapter.fetchCourseStatus();
		startManagingCursor(cur);
		SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.learned_courses_row,cur,columns,displayViews);
		//list=dbAdapter.fetchCourseStatusList();
		//dbAdapter.close();
//		Map<String,String> header=new HashMap<String,String>();
//		header.put(StudyDbAdapter.KEY_COURSE_NAME, "Donwload New Course");
//		list.add(0, header);
		//SimpleAdapter adapter=new SimpleAdapter(this,list,R.layout.learned_courses_row,columns,displayViews);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		cur.moveToFirst();
		cur.move(position);
		Intent i=new Intent();
		i.setClass(this, Sections.class);
		i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, cur.getString(cur.getColumnIndex(StudyDbAdapter.KEY_COURSE_NAME)));
		startActivity(i);
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==LearnedCourses.SELECT_COURSE_REQUEST){
			if (resultCode==RESULT_OK){
				Intent i = new Intent(this, Study.class);
				startActivity(i);
				//status=new CourseStatus(PrefStore.getCurrentCourseStatusId(this),dbAdapter);
			}
		}
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}

}
