package cn.zadui.vocabulary.view;

import java.util.ArrayList;

import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.Word;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ExamTwo extends Activity {

	private static final int HEADWORD_SPELLING_TAG=0;
	private static final int HEADWORD_CONTROL_TAG=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		ScrollView sv=new ScrollView(this);
		LinearLayout ll=new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		sv.addView(ll);
		
		StudyDbAdapter dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		//StudyUnit su=dbAdapter.getLatestUnit();
		StudyUnit su=dbAdapter.fetchStudyUnit(this.getIntent().getExtras().getLong("id"));
		ArrayList<Word> list=StudyUnit.fetchWords(dbAdapter, su.getRowId());
		dbAdapter.close();
		LayoutInflater vi=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for(int i=0;i<list.size();i++){
			View v=vi.inflate(R.layout.exam_two_row, null);
			Object[] tags=new Object[3];
			TextView meaning=(TextView)v.findViewById(R.id.meaning);
			EditText spelling=(EditText)v.findViewById(R.id.input_spelling);
			View indicate=(View)v.findViewById(R.id.indicator);
			tags[0]=spelling;
			tags[1]=indicate;
			tags[2]=list.get(i).getHeadword();
			meaning.setText(list.get(i).getMeaning());
			spelling.setEnabled(false);
			Button input=(Button)v.findViewById(R.id.btn_input);
			input.setTag(tags);
			input.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Button btn=(Button)v;
					Object[] tags=(Object[])btn.getTag();
					EditText et=(EditText)tags[0];
					View ind=(View)tags[1];
					String hw=(String)tags[2];
					String tmp=v.getResources().getString(R.string.default_input_btn_value);
					if (btn.getText().toString().equals(tmp)){
						et.setEnabled(true);
						btn.setText("OK");
					}else{
						et.setEnabled(false);
						btn.setText(tmp);
						if (et.getText().toString().equals(hw)) ind.setBackgroundColor(Color.GREEN);
						else ind.setBackgroundColor(Color.RED);
					}
					
				}
			});

			
//			spelling.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					EditText et=(EditText)v;
//					TextView hw=(TextView)et.getTag();
//					if(hasFocus){
//						if (et.getText().toString().equals(v.getResources().getString(R.string.input_spelling))) et.selectAll();
//					}else{
//						if (et.getText().toString().equals(v.getResources().getString(R.string.input_spelling))){
//							// do nothing
//						}else if(et.getText().toString().equals(hw.getText().toString())) {
//							hw.setBackgroundColor(Color.GREEN);
//							hw.setVisibility(View.VISIBLE);
//							et.setVisibility(View.GONE);
//						}else{
//							et.setBackgroundColor(Color.RED);
//						}
//					}				
//				}
//			});
			
			
			//TextView headword=(TextView)v.findViewById(R.id.spelling);
			//headword.setVisibility(View.GONE);
			//headword.setText(list.get(i).getHeadword());
			
//			meaning.setText(list.get(i).getMeaning());
//			spelling.setTag(headword);
			
			ll.addView(v);
		}
		setContentView(sv);
*/
	}

}
