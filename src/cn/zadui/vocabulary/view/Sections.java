package cn.zadui.vocabulary.view;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;

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
import android.widget.Button;
import android.widget.ListAdapter;
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
	
	SimpleCursorAdapter.ViewBinder viewBinder;
	
	Locale loc=Locale.getDefault();
	//DateFormat sdf=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,loc);
	Calendar ca=Calendar.getInstance();
	MessageFormat mf_created_at;
	MessageFormat mf_next_exam_at;
	MessageFormat mf_last_exam_at;
	MessageFormat mf_words_count;	
	MessageFormat mf_exam_times;	
	MessageFormat mf_unit_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mf_created_at=new MessageFormat(Sections.this.getResources().getString(R.string.unit_created_at),loc);
		mf_next_exam_at=new MessageFormat(Sections.this.getResources().getString(R.string.unit_next_exam_at),loc);	
		mf_last_exam_at=new MessageFormat(Sections.this.getResources().getString(R.string.unit_last_exam_at),loc);	
		mf_words_count=new MessageFormat(Sections.this.getResources().getString(R.string.unit_words_count),loc);	
		mf_exam_times=new MessageFormat(Sections.this.getResources().getString(R.string.unit_exam_times),loc);	
		mf_unit_id=new MessageFormat(Sections.this.getResources().getString(R.string.unit_id),loc);	
		
		setContentView(R.layout.units);
		
		//setTitle(getResources().getString(R.string.units_title));
		setTitle(getResources().getString(R.string.units_title)+" - "+getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME));

		viewBinder=new SimpleCursorAdapter.ViewBinder() {			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_ROWID)){
					TextView tv=(TextView)view;
					tv.setText(
							mf_unit_id.format(new Integer[]{cursor.getInt(columnIndex)}));					
					//tv.setText(String.valueOf(cursor.getLong(columnIndex)));
					return true;
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_WORDS_COUNT)){
					//ca.setTimeInMillis(cursor.getLong(columnIndex)*1000);
					TextView tv=(TextView)view;
					tv.setText(
							mf_words_count.format(new Integer[]{cursor.getInt(columnIndex)}));
					return true;
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_LAST_EXAM_AT)){
					TextView tv=(TextView)view;
					int val=cursor.getInt(columnIndex);
					if (val==0){
						tv.setText(getResources().getString(R.string.never_exam));
					}else{
						String[] textValues=Helper.friendlyTime(val);
						if (textValues[1].equals("hour")) textValues[1]=Sections.this.getResources().getString(R.string.hour);
						else if (textValues[1].equals("month")) textValues[1]=Sections.this.getResources().getString(R.string.month);
						else if (textValues[1].equals("day")) textValues[1]=Sections.this.getResources().getString(R.string.day);
						tv.setText(mf_last_exam_at.format(textValues));
								//mf_words_count.format(new Integer[]{cursor.getInt(columnIndex)}));
					}
					return true;
				}
				/*
				if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_CREATED_AT)){
					ca.setTimeInMillis(cursor.getLong(columnIndex)*1000);
					TextView tv=(TextView)view;
					tv.setText(mf_created_at.format(new Date[]{ca.getTime(),ca.getTime()}));
					return true;					
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_ROWID)){
					TextView tv=(TextView)view;
					tv.setText(String.valueOf(cursor.getLong(cursor.getColumnIndex(StudyDbAdapter.KEY_ROWID))));
					return true;
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT)){
					ca.setTimeInMillis(cursor.getLong(columnIndex)*1000);
					TextView tv=(TextView)view;
					tv.setText(mf_next_exam_at.format(new Date[]{ca.getTime(),ca.getTime()}));
					return true;
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_WORDS_COUNT)){
					ca.setTimeInMillis(cursor.getLong(columnIndex)*1000);
					TextView tv=(TextView)view;
					tv.setText(
							mf_words_count.format(new Integer[]{cursor.getInt(columnIndex)}));
					return true;
				}else if (columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_COMMON_EXAM_TIMES)){
					TextView tv=(TextView)view;
					tv.setText(mf_exam_times.format(new Integer[]{cursor.getInt(columnIndex)}));
					return true;
				}
				*/
//				if(columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_CREATED_AT) ||
//						columnIndex==cursor.getColumnIndex(StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT)){
//					Calendar ca=Calendar.getInstance();
//					ca.setTimeInMillis(cursor.getLong(columnIndex)*1000);
//					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
//					TextView tv=(TextView)view;
//					tv.setText(sdf.format(ca.getTime()));
//					return true;
//				}
				return false;
			}
		};
		
		LayoutInflater mInflater=(LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View v=mInflater.inflate(R.layout.units_header, null);
		getListView().addHeaderView(v);
		fillData();
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		//super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0,REVIEW,0,getString(R.string.review));
			menu.add(0,EXAM_MEANING_TO_SPELLING,0,getString(R.string.exam_meaning_2_spelling));
			menu.add(0,EXAM_SPELLING_TO_MEANING,0,getString(R.string.exam_spelling_2_meaning));
			menu.add(0,EXAM_BY_EXAMPLES,0,getString(R.string.exam_by_example));
			menu.add(0,DELETE_SECTION,0,getString(R.string.delete));
			
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
		Intent newIntent=new Intent();
		newIntent.putExtra("id", info.id);
		
		switch(item.getItemId()){
		case REVIEW:
			newIntent.setClass(this, Review.class);
			break;
		case EXAM_MEANING_TO_SPELLING:
			newIntent.setClass(this, Exam.class);
			newIntent.putExtra(Exam.DIRECTION, EXAM_MEANING_TO_SPELLING);
			break;
		case EXAM_SPELLING_TO_MEANING:
			newIntent.setClass(this, Exam.class);
			newIntent.putExtra(Exam.DIRECTION, EXAM_SPELLING_TO_MEANING);
			break;
		case EXAM_BY_EXAMPLES:
			newIntent.setClass(this, Examples.class);
			newIntent.putExtra(Exam.DIRECTION, EXAM_BY_EXAMPLES);
			break;
		case DELETE_SECTION:
			break;
		}
		startActivity(newIntent);
		return true;
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position==0){
			Intent i = new Intent(v.getContext(), Study.class);
			i.putExtra(StudyDbAdapter.KEY_COURSE_NAME, getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME));
			startActivity(i);
		}else{
			Intent newIntent=new Intent();
			newIntent.putExtra("id", id);
			newIntent.setClass(this, Review.class);
			startActivity(newIntent);
		}
	}

	private void fillData() {
		int[] displayViews=new int[]{
				R.id.tv_unit_id,
				//R.id.tv_unit_course_name,
				R.id.tv_unit_words_count,
				R.id.tv_unit_last_exam_at,
				//R.id.tv_unit_created_at,
				//R.id.tv_unit_next_exam_at,
				//R.id.tv_unit_exam_times
				};
		String[] columns=new String[]{	
				StudyDbAdapter.KEY_ROWID,
				//StudyDbAdapter.KEY_COURSE_NAME,
				StudyDbAdapter.KEY_WORDS_COUNT,
				StudyDbAdapter.KEY_LAST_EXAM_AT,
				//StudyDbAdapter.KEY_CREATED_AT,
				//StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT,
				//StudyDbAdapter.KEY_COMMON_EXAM_TIMES
				};
		
		StudyDbAdapter dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		
		SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.units_row,dbAdapter.fetchSectionsByCourse(getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME)),columns,displayViews);
		adapter.setViewBinder(viewBinder);
		setListAdapter(adapter);
		dbAdapter.close();
	}	
}
