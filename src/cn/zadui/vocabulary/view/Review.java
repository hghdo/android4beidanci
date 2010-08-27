package cn.zadui.vocabulary.view;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class Review extends ListActivity implements RadioGroup.OnCheckedChangeListener {

	static final String TAG="Review";
//	SharedPreferences spSettings;
//	CourseStatus status;
	StudyDbAdapter dbAdapter;
    private RadioGroup mRadioGroup;
    private long unitId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//spSettings = getSharedPreferences(Status.PREFS_NAME, 0);		
		//status=Status.getInstance(spSettings);
		unitId=getIntent().getExtras().getLong(StudyDbAdapter.KEY_ROWID);
		
		setContentView(R.layout.review);
		
		mRadioGroup = (RadioGroup) findViewById(R.id.rg_review_filter);
		mRadioGroup.setOnCheckedChangeListener(this);
		
		findViewById(R.id.rb_review_forgot).setTag(StudyDbAdapter.UNIT_WORDS_FILTER_FORGOT_ONLY);
		findViewById(R.id.rb_not_mastered).setTag(StudyDbAdapter.UNIT_WORDS_FILTER_MASTERED_EXCLUDED);
		findViewById(R.id.rb_review_all).setTag(StudyDbAdapter.UNIT_WORDS_FILTER_ALL);
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		fillData();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		fillData();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
	
	private void fillData(){
		Cursor cur=dbAdapter.fetchSectionWords(
				unitId,
				(Integer)findViewById(mRadioGroup.getCheckedRadioButtonId()).getTag()
				);
		startManagingCursor(cur);
		ReviewAdapter listAdapter=new ReviewAdapter(this,cur,dbAdapter);
		setListAdapter(listAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i=new Intent(this,Study.class);
		i.putExtra(Study.KEY_IS_REVIEW, true);
		i.putExtra(StudyDbAdapter.KEY_ROWID, id);
		startActivity(i);
	}
	
	private static class ReviewAdapter extends BaseAdapter{
		
        private LayoutInflater mInflater;
        private Cursor mCursor;
        private Context mContext;
        private StudyDbAdapter mAdapter;
		
        public ReviewAdapter(Context ctx, Cursor cur, StudyDbAdapter dba){
        	mContext=ctx;
        	mInflater = LayoutInflater.from(mContext);
        	mCursor=cur;
        	mAdapter=dba;
        }
        
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView==null){
				convertView=mInflater.inflate(R.layout.review_row, null);
				holder=new ViewHolder();
				holder.headword=(TextView)convertView.findViewById(R.id.tv_review_row_headword);
				holder.meaning=(TextView)convertView.findViewById(R.id.tv_review_row_meaning);
				holder.star=(ImageView)convertView.findViewById(R.id.iv_review_row_star_me);
				holder.star.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						StarStruct ss=(StarStruct)v.getTag();
						ss.starOn=!ss.starOn;
						ImageView iv=(ImageView)v;						
						iv.setImageResource(ss.starOn ? R.drawable.star_on : R.drawable.star_off);
						ReviewAdapter.this.mAdapter.starWord(ss.starOn, ss.wordId);
						ReviewAdapter.this.mCursor.requery();
						Log.d(TAG,"AAAAAAAA--"+String.valueOf(ss.wordId));
					}
				});
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder)convertView.getTag();
			}
			mCursor.moveToPosition(position);
			holder.headword.setText(mCursor.getString(mCursor.getColumnIndex(StudyDbAdapter.KEY_WORD)));
			holder.meaning.setText(mCursor.getString(mCursor.getColumnIndex(StudyDbAdapter.KEY_MEANING)));
			StarStruct ss=new StarStruct();
			ss.starOn=mCursor.getInt(mCursor.getColumnIndex(StudyDbAdapter.KEY_LAST_EXAM_FAILED))==1;
			ss.wordId=getItemId(position);
			holder.star.setTag(ss);
			holder.star.setImageResource(ss.starOn ? R.drawable.star_on : R.drawable.star_off);
			return convertView;
		}


		@Override
		public int getCount() {
			return mCursor.getCount();
		}


		@Override
		public Object getItem(int position) {
			mCursor.moveToPosition(position);
			return mCursor;
		}


		@Override
		public long getItemId(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getLong(mCursor.getColumnIndex(StudyDbAdapter.KEY_ROWID));
		}
		
        static class ViewHolder {
            TextView headword;
            TextView meaning;
            ImageView star;
        }
        
        static class StarStruct{
        	boolean starOn;
        	long wordId;
        }
		
	}

}
