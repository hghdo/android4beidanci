package cn.zadui.vocabulary.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.PrefStore;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class FirstScreen extends ListActivity {
	
	private static final String TAG="FirstScreennnnn";
	private static final int DIALOG_SELECT_LANG=0;
	private static final int DIALOG_CONFIRM_DELETE=1;
	private static final int CONTEXT_MENU_DELETE=5;
	private static final int MENU_SETTINGS=0;
	private static final int MENU_ABOUT=1;
	
	private static int[] displayViews=new int[]{
			R.id.tv_selected_courses_course_name,
			R.id.tv_selected_courses_progress,
			//R.id.tv_selected_courses_content_count
			//R.id.tv_unit_created_at,
			//R.id.tv_unit_next_exam_at,
			//R.id.tv_unit_exam_times
			};
	private static String[] columns=new String[]{	
			//StudyDbAdapter.KEY_ROWID,
			StudyDbAdapter.KEY_COURSE_NAME,
			StudyDbAdapter.KEY_LEARNED_CONTENT_COUNT,
			//StudyDbAdapter.KEY_CONTENT_COUNT,
			//StudyDbAdapter.KEY_CREATED_AT,
			//StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT,
			//StudyDbAdapter.KEY_COMMON_EXAM_TIMES
			};
	
	private StudyDbAdapter dbAdapter;
	private Cursor cur;
	private long deleteCourseId;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first);
		LayoutInflater mInflater=(LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View header=mInflater.inflate(R.layout.first_header, null);
		getListView().addHeaderView(header);
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();	
		cur=dbAdapter.fetchCourseStatus();
		startManagingCursor(cur);
		fillData();
		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (PrefStore.getMotherTongueCode(this).equals("initial")){
			showDialog(DIALOG_SELECT_LANG);
		}
		Log.d("BBBBBBBBBBBBBBBB","show dialog");
		fillData();
		//getListView().invalidate();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}

	private void fillData() {
		cur.requery();
		SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.first_row,cur,columns,displayViews);
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
			startActivityForResult(sc, Actions.SELECT_COURSE_REQUEST);
		}else{
			cur.moveToFirst();
			cur.move(position-1);
			Intent i=new Intent();
			i.setClass(this, Sections.class);
			// TODO should use course unique id to replace the course name. the uuid should generated in server side.
			i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, cur.getString(cur.getColumnIndex(StudyDbAdapter.KEY_COURSE_NAME)));
			startActivity(i);
		}
	}	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)menuInfo;
		if (info.position==0){
			//do nothing
		}else{
			menu.add(0,CONTEXT_MENU_DELETE,0,getString(R.string.delete));
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		deleteCourseId=info.id;
		showDialog(DIALOG_CONFIRM_DELETE);				
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==Actions.SELECT_COURSE_REQUEST){
			if (resultCode==RESULT_OK){
				Intent i = new Intent(this, Study.class);
				i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, PrefStore.getSelectedCourseName(this));
				startActivity(i);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(getResources().getDrawable(android.R.drawable.ic_menu_preferences));
		menu.add(0, MENU_ABOUT,1,"About").setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case MENU_SETTINGS:
			Intent settings=new Intent(this,Settings.class);
			startActivity(settings);
			return true;
		case MENU_ABOUT:
			return true;
		default:
			return false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case DIALOG_SELECT_LANG:
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
		case DIALOG_CONFIRM_DELETE:
			return new AlertDialog.Builder(this)
				.setTitle("Did your really want to delete it?")
				.setPositiveButton(R.string.alert_dialog_ok,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dbAdapter.deleteCourseStatus(deleteCourseId);
						fillData();
						//FirstScreen.this.getListView().invalidate();
					}
				})
				.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();
		}
		return null;
	}

}
