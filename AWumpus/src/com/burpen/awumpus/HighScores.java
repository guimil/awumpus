package com.burpen.awumpus;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HighScores extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.highscores);
	    
	    final TextView tv = (TextView) findViewById(R.id.highScoresContent);
	    
	    // TODO get actual scores
	    int totalMaxHighScores = 20;
	    
	    tv.setText("");
	    
	    for (int i=0; i<totalMaxHighScores; i++) {
	    	tv.append("" + (i+1) + ". test score - " + (1000/(i+1)) + "\n");
	    }
	    
	}
	
}
