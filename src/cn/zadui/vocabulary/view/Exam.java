package cn.zadui.vocabulary.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.service.DictionaryService;
import cn.zadui.vocabulary.service.NetworkService;
import cn.zadui.vocabulary.service.StateChangeListener;
import cn.zadui.vocabulary.service.NetworkService.ServiceState;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * This activity used in 3 situations.
 * 1. Show the meaning of a headword and let user spelling it.
 * 2. Show the headword/content let the user recall it's meaning.
 * 3. Show the meaning/translate of a headword/content and let user recall it's headword/content
 * TODO Should store the cur position in onPause callback and restore it in onResume
 * @author Huang Gehua
 *
 */
public class Exam extends Activity implements View.OnClickListener,StateChangeListener{
	
	public static final String DIRECTION="direction";
//	public static final int POSITIVE=0;
//	public static final int NEGATIVE=1;

	SharedPreferences spSettings ;
	CourseStatus status;
	private TextView tvExamTip;
	private TextView tvExamAnswer;
	TextView tvFirstTitle;
	TextView tvSecondTitle;
	private View bottomBar;
	private Button btnAnswer;
	//private ProgressBar pb;
	long unitId;
	private Cursor cur;
	private Word word;
	private StudyDbAdapter dbAdapter;
	
	private static final String TAG="Exam";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//spSettings = getSharedPreferences(Status.PREFS_NAME, 0);
		//status=Status.getInstance(spSettings);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.exam);
		setProgressBarVisibility(true);
		
		tvExamTip=(TextView)findViewById(R.id.tv_exam_tip);
		tvExamAnswer=(TextView)findViewById(R.id.tv_exam_answer);
		tvFirstTitle=(TextView)findViewById(R.id.tv_exam_first_sub_title);
		tvSecondTitle=(TextView)findViewById(R.id.tv_exam_second_sub_title);
		bottomBar=findViewById(R.id.ll_exam_button_bar);
		btnAnswer=(Button)findViewById(R.id.btn_exam_show_answer);
		btnAnswer.setOnClickListener(this);
		//pb=(ProgressBar)findViewById(R.id.pb_exam_progress);
		setProgress(0);
		
		findViewById(R.id.btn_exam_mastered).setOnClickListener(this);
		findViewById(R.id.btn_exam_mastered).setTag(Word.MASTERED);
		findViewById(R.id.btn_exam_pass).setOnClickListener(this);
		findViewById(R.id.btn_exam_pass).setTag(Word.PASS);
		findViewById(R.id.btn_exam_forgot).setOnClickListener(this);
		findViewById(R.id.btn_exam_forgot).setTag(Word.FORGOT);
		
		
		if (getIntent().getExtras().getInt(DIRECTION)==Sections.EXAM_MEANING_TO_SPELLING){
			tvFirstTitle.setText(getResources().getString(R.string.meaning));
			tvSecondTitle.setText(getResources().getString(R.string.spelling));
		}else if (getIntent().getExtras().getInt(DIRECTION)==Sections.EXAM_SPELLING_TO_MEANING){
			tvFirstTitle.setText(getResources().getString(R.string.spelling));
			tvSecondTitle.setText(getResources().getString(R.string.meaning));
		}else{
			tvFirstTitle.setText(getResources().getString(R.string.examples));
			tvSecondTitle.setText(getResources().getString(R.string.meaning));
		}
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		unitId=getIntent().getExtras().getLong(StudyDbAdapter.KEY_ROWID);
		cur =dbAdapter.fetchSectionWords(unitId,StudyDbAdapter.UNIT_WORDS_FILTER_MASTERED_EXCLUDED);
		startManagingCursor(cur);
		if (cur.getCount()>0){
			cur.moveToFirst();
			word=new Word(cur);
			fillWord();
		}else{
			setProgress(10000);
			showDialog(0);
		}
	}
	
	private void fillWord(){
		bottomBar.setVisibility(View.GONE);
		btnAnswer.setVisibility(View.VISIBLE);
		tvExamAnswer.setTextColor(getResources().getColor(R.color.white));
		if (getIntent().getExtras().getInt(DIRECTION)==Sections.EXAM_MEANING_TO_SPELLING){
			tvExamTip.setText(word.getMeaning());
			tvExamAnswer.setText(word.getHeadword());		
		}else if (getIntent().getExtras().getInt(DIRECTION)==Sections.EXAM_SPELLING_TO_MEANING){
			tvExamTip.setText(word.getHeadword());
			tvExamAnswer.setText(word.getMeaning());		
		}else{
			tvExamAnswer.setText(word.getMeaning());	
			Intent i=new Intent();
			DictionaryService.setStateChangeListener(this);
			i.setClass(this, DictionaryService.class);
			i.putExtra(NetworkService.KEY_ACTION, NetworkService.SELECTIVE_EXAMPLE_ACTION);
			i.putExtra(NetworkService.KEY_HEADWORD, word.getHeadword());
			startService(i);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.btn_exam_show_answer){
			v.setVisibility(View.GONE);
			bottomBar.setVisibility(View.VISIBLE);
			tvExamAnswer.setTextColor(getResources().getColor(R.color.black));
		}else{
			int action=(Integer)(v.getTag());
			word.review(dbAdapter, action);
			if(cur.isLast()) {
				setProgress(10000);
				// show exam finished dialog
                showDialog(0);
				return;
			}else{
				cur.moveToNext();
				word=new Word(cur);
				//pb.setProgress((cur.getPosition())*100/cur.getCount());
				setProgress((cur.getPosition())*10000/cur.getCount());
				fillWord();
			}
		}
	}

	@Override
	protected void onDestroy() {
		dbAdapter.updateSection(unitId, (int)System.currentTimeMillis()/1000, StudyDbAdapter.DB_COL_LAST_EXAM_AT);
		dbAdapter.close();
		Log.d("DDDDDDDDDDDDDDDDDD","in onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		
        return new AlertDialog.Builder(Exam.this)
        //.setIcon(R.drawable.alert_dialog_icon)
        .setTitle(R.string.exam_finished)
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Exam.this.finish();
            }
        })
        .create();

	}

	@Override
	public void onServiceStateChanged(Object result,ServiceState state) {
		tvExamTip.setText(
				Html.fromHtml((String)result)
				);
	}
}
