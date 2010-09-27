package cn.zadui.vocabulary.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class ExamSpelling extends Activity implements View.OnClickListener {
	
	static final int FINISH_DIALOG=0;
	static final int TIP_DIALOG=1;
	
	//long sectionId;
	private Section section;
	private Cursor cur;
	private Word word;
	private StudyDbAdapter dbAdapter;
	
	private EditText etSpelling;
	private TextView tvSpellingMeaning;
	private Button btnCheckSpelling;
	private Button btnforgot;
	
	Toast rightToast;
	Toast wrongToast;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.exam_spelling);
		setProgressBarVisibility(true);
		
		etSpelling=(EditText)findViewById(R.id.et_exam_spell);
		etSpelling.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        int sdkLevel = 1;
        try {
            sdkLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {}
        //524288 means TYPE_TEXT_FLAG_NO_SUGGESTIONS
//		if (sdkLevel>4)	etSpelling.setInputType(524288);
		etSpelling.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				if (actionId==EditorInfo.IME_ACTION_DONE){
					btnCheckSpelling.performClick();
				}
				return true;
			}
			
		});
		
		tvSpellingMeaning=(TextView) findViewById(R.id.tv_exam_spell_meaning);
		btnCheckSpelling=(Button)findViewById(R.id.btn_exam_spell_check);
		btnCheckSpelling.setOnClickListener(this);
		btnforgot=(Button)findViewById(R.id.btn_exam_forgot);
		btnforgot.setOnClickListener(this);
		
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		long sectionId=getIntent().getExtras().getLong(StudyDbAdapter.KEY_ROWID);
		cur =dbAdapter.fetchSectionWords(sectionId,StudyDbAdapter.UNIT_WORDS_FILTER_MASTERED_EXCLUDED);
		section=Section.findById(dbAdapter, sectionId);
		startManagingCursor(cur);
		if (cur.getCount()>0){
			if (section.isLastExamFinished()) cur.moveToFirst();
			else cur.moveToPosition(section.getLastExamPosition());
			word=new Word(cur);
			fillData();
		}else{
			setProgress(10000);
			showDialog(FINISH_DIALOG);
		}
		
		//TODO fix the hard code strings
		rightToast=Toast.makeText(this, "Great, you are right!",Toast.LENGTH_SHORT);
		wrongToast=Toast.makeText(this, "Sorry, it's wrong, please retry!",Toast.LENGTH_SHORT);
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.btn_exam_spell_check){
			section.setLastExamPosition(cur.getPosition());
			if (etSpelling.getText().toString().equals(word.getHeadword())){
				etSpelling.setTextColor(getResources().getColor(R.color.green));
				rightToast.show();
				if (cur.isLast()){
					setProgress(10000);
	                showDialog(FINISH_DIALOG);
					return;					
				}else{
					cur.moveToNext();
					word=new Word(cur);
					fillData();
				}
			}else{
				etSpelling.setTextColor(this.getResources().getColor(R.color.red));
				etSpelling.selectAll();
				wrongToast.show();
			}
		}else{
			word.review(dbAdapter, Word.FORGOT);
			showDialog(TIP_DIALOG);
		}
	}
	
	private void calProgress(){
		setTitle(String.format("Exam progress: %1$s/%2$s", cur.getPosition(),cur.getCount()));
		setProgress((cur.getPosition())*10000/cur.getCount());
	}
	
	private void fillData(){
		etSpelling.setTextColor(getResources().getColor(R.color.black));
		etSpelling.setText("");
		tvSpellingMeaning.setText(word.getMeaning());
		calProgress();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case FINISH_DIALOG:
	        return new AlertDialog.Builder(ExamSpelling.this)
	        //.setIcon(R.drawable.alert_dialog_icon)
	        .setTitle(R.string.exam_finished)
	        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	ExamSpelling.this.finish();
	            }
	        })
	        .create();
		case TIP_DIALOG:
	        return new AlertDialog.Builder(ExamSpelling.this)
	        //.setIcon(R.drawable.alert_dialog_icon)
	        .setTitle(word.getHeadword())
	        .setMessage(word.getMeaning())
	        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	etSpelling.selectAll();
	            	//etSpelling.setText("");
	            	//ExamSpelling.this.dismissDialog(TIP_DIALOG);
	            }
	        })
	        .create();			
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id){
		case TIP_DIALOG:
			AlertDialog d=(AlertDialog)dialog;
			d.setTitle(word.getHeadword());
			d.setMessage(word.getMeaning());
		}
	}

	@Override
	protected void onPause() {
		section.examed(cur.isLast());
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
	

}
