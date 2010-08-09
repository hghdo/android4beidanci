package cn.zadui.vocabulary.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.service.DictionaryService;
import cn.zadui.vocabulary.service.NetworkService;
import cn.zadui.vocabulary.service.StateChangeListener;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.zadui.vocabulary.R;

public class Examples extends ListActivity implements StateChangeListener,View.OnClickListener {

	long unitId;
	private Cursor cur;
	private Word word;
	private StudyDbAdapter dbAdapter;
	
	private TextView tvExamAnswer;
	private View bottomBar;
	private Button btnAnswer;
	
	private ArrayList<Map<String,CharSequence>> examples;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.examples);
		
		tvExamAnswer=(TextView)findViewById(R.id.tv_example_answer);
		bottomBar=findViewById(R.id.ll_example_button_bar);
		btnAnswer=(Button)findViewById(R.id.btn_example_show_answer);
		btnAnswer.setOnClickListener(this);
		//pb=(ProgressBar)findViewById(R.id.pb_example_progress);
		setProgress(0);
		
		findViewById(R.id.btn_example_mastered).setOnClickListener(this);
		findViewById(R.id.btn_example_mastered).setTag(Word.MASTERED);
		findViewById(R.id.btn_example_pass).setOnClickListener(this);
		findViewById(R.id.btn_example_pass).setTag(Word.PASS);
		findViewById(R.id.btn_example_forgot).setOnClickListener(this);
		findViewById(R.id.btn_example_forgot).setTag(Word.FORGOT);
		
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		unitId=getIntent().getExtras().getLong("id");
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

		tvExamAnswer.setText(word.getMeaning());	
		
		DictionaryService.setStateChangeListener(this);
		Intent i=new Intent();
		i.setClass(this, DictionaryService.class);
		i.putExtra(NetworkService.KEY_ACTION, NetworkService.GOOGLE_EXAMPLE_ACTION);
		i.putExtra(NetworkService.KEY_HEADWORD, word.getHeadword());
		startService(i);
	}


	private void fillExamples(){
		int[] to = {R.id.tv_example_title,R.id.tv_example_content};
		String [] from = {"title","content"};
		SimpleAdapter adapter=new SimpleAdapter(this,examples,R.layout.examples_row,from,to);
		adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				//Log.d("AAAAAAAAAAAAAAAAAAaa",textRepresentation);
				//Log.d("BBBBBBBBBBBBBBBBBBBB",data.toString());
				((TextView)view).setText(Html.fromHtml(textRepresentation));
				return true;
			}
		});
		setListAdapter(adapter);
	}


	@Override
	public void onServiceStateChanged(Object result){
		String jstr=(String)result;
		try {
			JSONObject jo=new JSONObject(jstr);
			JSONArray results=jo.getJSONObject("responseData").getJSONArray("results");
			if (examples==null) examples=new ArrayList<Map<String,CharSequence>>();
			else examples.clear();
			for(int i=0;i<results.length();i++){
				Map<String,CharSequence> ex=new HashMap<String,CharSequence>();
				ex.put("title",results.getJSONObject(i).getString("title"));
				ex.put("content", results.getJSONObject(i).getString("content"));
				ex.put("url", results.getJSONObject(i).getString("blogUrl"));				
				examples.add(ex);
			}
			fillExamples();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.btn_example_show_answer){
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
	protected Dialog onCreateDialog(int id) {
		
        return new AlertDialog.Builder(Examples.this)
        //.setIcon(R.drawable.alert_dialog_icon)
        .setTitle(R.string.exam_finished)
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Examples.this.finish();
            }
        })
        .create();

	}
	
	
}



