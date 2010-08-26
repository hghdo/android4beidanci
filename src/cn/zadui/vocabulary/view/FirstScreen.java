package cn.zadui.vocabulary.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.PrefStore;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class FirstScreen extends ListActivity {
	
	static final String TAG="LLLLLLLLLLLLLLLLLLLLLLLLLLearnedCourses";
	//LinkedList<Map<String,String>> list;
	static final int SELECT_COURSE_REQUEST=0;
	static final int MENU_SETTINGS=0;
	static final int MENU_ABOUT=1;
	StudyDbAdapter dbAdapter;
	private Cursor cur;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.first);
		LayoutInflater mInflater=(LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View v=mInflater.inflate(R.layout.first_header, null);
		getListView().addHeaderView(v);
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (PrefStore.getMotherTongueCode(this).equals("initial")){
			showDialog(0);
		}
		fillData();
	}

	private void fillData() {
		int[] displayViews=new int[]{
				R.id.tv_selected_courses_course_name,
				R.id.tv_selected_courses_progress,
				//R.id.tv_selected_courses_content_count
				//R.id.tv_unit_created_at,
				//R.id.tv_unit_next_exam_at,
				//R.id.tv_unit_exam_times
				};
		String[] columns=new String[]{	
				//StudyDbAdapter.KEY_ROWID,
				StudyDbAdapter.KEY_COURSE_NAME,
				StudyDbAdapter.KEY_LEARNED_CONTENT_COUNT,
				//StudyDbAdapter.KEY_CONTENT_COUNT,
				//StudyDbAdapter.KEY_CREATED_AT,
				//StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT,
				//StudyDbAdapter.KEY_COMMON_EXAM_TIMES
				};
		cur=dbAdapter.fetchCourseStatus();
		startManagingCursor(cur);
		SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.first_row,cur,columns,displayViews);
		//list=dbAdapter.fetchCourseStatusList();
		//dbAdapter.close();
//		Map<String,String> header=new HashMap<String,String>();
//		header.put(StudyDbAdapter.KEY_COURSE_NAME, "Donwload New Course");
//		list.add(0, header);
		//SimpleAdapter adapter=new SimpleAdapter(this,list,R.layout.learned_courses_row,columns,displayViews);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_LEARNED_CONTENT_COUNT)){
					TextView tv=(TextView)view;
					tv.setText(String.format(
							getString(R.string.course_progress), 
							cursor.getInt(columnIndex),
							cursor.getInt(cursor.getColumnIndex(StudyDbAdapter.KEY_CONTENT_COUNT))
							));
					return true;
				}
				return false;
			}
		});
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position==0){
			Intent sc=new Intent(this, CourseList.class);
			startActivityForResult(sc, FirstScreen.SELECT_COURSE_REQUEST);
		}else{
			cur.moveToFirst();
			cur.move(position-1);
			Intent i=new Intent();
			i.setClass(this, Sections.class);
			i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, cur.getString(cur.getColumnIndex(StudyDbAdapter.KEY_COURSE_NAME)));
			startActivity(i);
		}
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==FirstScreen.SELECT_COURSE_REQUEST){
			if (resultCode==RESULT_OK){
				Intent i = new Intent(this, Study.class);
				i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, PrefStore.getSelectedCourseName(this));
				startActivity(i);
			}
		}
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(getResources().getDrawable(android.R.drawable.ic_menu_preferences));
		menu.add(0, MENU_ABOUT,1,"About").setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()){
		case MENU_SETTINGS:
			Intent settings=new Intent(this,Settings.class);
			startActivity(settings);
			break;
		case MENU_ABOUT:
			break;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(this)
			.setTitle("Please select your language")
			.setPositiveButton(R.string.alert_dialog_ok,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent settings=new Intent(FirstScreen.this,Settings.class);
					startActivity(settings);
				}
			})
			.setCancelable(false)
			.create();
	}

}
