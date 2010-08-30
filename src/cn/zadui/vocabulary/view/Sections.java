package cn.zadui.vocabulary.view;

import java.util.Calendar;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.Helper;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class Sections extends ListActivity {

	public static final int REVIEW					=0;
	public static final int EXAM_MEANING_TO_SPELLING=1;
	public static final int EXAM_SPELLING_TO_MEANING=2;
	public static final int EXAM_BY_EXAMPLES		=3;
	public static final int SYNC_EXAMPLES			=4;
	public static final int DELETE_SECTION			=5;
	public static final int STUDY					=6;
	
	//SimpleCursorAdapter.ViewBinder viewBinder;
	StudyDbAdapter dbAdapter;	
	Cursor cur;
	View header;
	int[] displayViews=new int[]{
			//R.id.tv_unit_id,
			//R.id.tv_unit_course_name,
			R.id.tv_units_row_item_title,
			R.id.tv_unit_last_exam_at,
			//R.id.tv_unit_created_at,
			//R.id.tv_unit_next_exam_at,
			//R.id.tv_unit_exam_times
			};
	String[] columns=new String[]{	
			//StudyDbAdapter.KEY_ROWID,
			//StudyDbAdapter.KEY_COURSE_NAME,
			//StudyDbAdapter.KEY_CREATED_AT,
			StudyDbAdapter.KEY_WORDS_COUNT,
			StudyDbAdapter.KEY_LAST_EXAM_AT,
			//StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT,
			//StudyDbAdapter.KEY_COMMON_EXAM_TIMES
			};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.units);
		setTitle(getResources().getString(R.string.units_title)+" - "+getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME));
		LayoutInflater mInflater=(LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		header=mInflater.inflate(R.layout.units_header, null);
		getListView().addHeaderView(header);
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		cur=dbAdapter.fetchSectionsByCourse(getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME));
		startManagingCursor(cur);
		registerForContextMenu(getListView());
		fillData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getListView().invalidate();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position==0){
			continueStudy();
		}else{
			Intent newIntent=new Intent();
			newIntent.putExtra(StudyDbAdapter.KEY_ROWID, id);
			newIntent.setClass(this, Review.class);
			startActivity(newIntent);
		}
	}

	private void continueStudy() {
		Intent i = new Intent(this, Study.class);
		i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME));
		startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)menuInfo;
		if (info.position==0){
			//do nothing
		}else{
			menu.add(0,REVIEW,0,getString(R.string.review));
			menu.add(0,EXAM_MEANING_TO_SPELLING,0,getString(R.string.exam_meaning_2_spelling));
			menu.add(0,EXAM_SPELLING_TO_MEANING,0,getString(R.string.exam_spelling_2_meaning));
			//menu.add(0,EXAM_BY_EXAMPLES,0,getString(R.string.exam_by_example));
			menu.add(0,DELETE_SECTION,0,getString(R.string.delete));
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		Intent newIntent=new Intent();
		newIntent.putExtra(StudyDbAdapter.KEY_ROWID, info.id);
		
		switch(item.getItemId()){
		case REVIEW:
			newIntent.setClass(this, Review.class);
			startActivity(newIntent);
			break;
		case EXAM_MEANING_TO_SPELLING:
			newIntent.setClass(this, ExamSpelling.class);
			//newIntent.putExtra(Exam.DIRECTION, EXAM_MEANING_TO_SPELLING);
			startActivity(newIntent);
			break;
		case EXAM_SPELLING_TO_MEANING:
			newIntent.setClass(this, Exam.class);
			//TODO remove the direction parameter
			newIntent.putExtra(Exam.DIRECTION, EXAM_SPELLING_TO_MEANING);
			startActivity(newIntent);
			break;
		case EXAM_BY_EXAMPLES:
			newIntent.setClass(this, Examples.class);
			newIntent.putExtra(Exam.DIRECTION, EXAM_BY_EXAMPLES);
			startActivity(newIntent);
			break;
		case DELETE_SECTION:
			dbAdapter.deleteSection(info.id);
			fillData();
			break;
		case STUDY:
			continueStudy();
			break;
		}
		return true;
		
	}

	private void fillData() {
		SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.units_row,cur,columns,displayViews);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_WORDS_COUNT)){
					long createAt=cursor.getLong(cursor.getColumnIndex(StudyDbAdapter.KEY_CREATED_AT));
					Calendar cal=Calendar.getInstance();
					cal.setTimeInMillis(createAt);
					
					int wordsCount=cursor.getInt(columnIndex);
					TextView tv=(TextView)view;
					tv.setText(
							String.format(
									getString(R.string.sections_row_item_title),
									android.text.format.DateFormat.getDateFormat(Sections.this).format(cal.getTime()),
									wordsCount)
							 );
					return true;
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_LAST_EXAM_AT)){
					TextView tv=(TextView)view;
					int val=cursor.getInt(columnIndex);
					if (val==0){
						tv.setText(getResources().getString(R.string.never_exam));
					}else{
						Object[] textValues=Helper.friendlyTime(val);
						if (textValues[1].equals("hour")) textValues[1]=Sections.this.getResources().getString(R.string.hour);
						else if (textValues[1].equals("month")) textValues[1]=Sections.this.getResources().getString(R.string.month);
						else if (textValues[1].equals("day")) textValues[1]=Sections.this.getResources().getString(R.string.day);
						tv.setText(String.format(getString(R.string.unit_last_exam_at), textValues));
					}
					return true;
				}
				return false;
			}
		});
		setListAdapter(adapter);
	}
}
