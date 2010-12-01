package com.burpen.awumpus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.burpen.awumpus.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AWumpus extends Activity {
	
	//game prefs
	private SharedPreferences preferences;
	private static boolean useDebug, canSmellWumpus, mustHaveArrows, clearLogOnReshuffle, mobileBats;
	private static int maxLogLines;
	
	//views
	private TextView textView, arrowView, moveView;
	private ScrollView scrollView;
	
	/** The OpenGL View */
	private GLSurfaceView glSurface;
	
	//buttons
	private static Button clearLogButton, startGameButton, shootButton, moveButton;
	
	//constants
	private static final int DEFAULT_MAX_LOG_LINES = 300;
	private static final int WIDTH = 8;
	private static final int HEIGHT = 3;
	private static final int MAX_ROOMS = HEIGHT * WIDTH;
	private static final int MAX_EDGES_PER_ROOM = 4;
	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	private static final int MAX_ENTITIES = 6;
	private static final int PLAYER = 0;
	private static final int WUMPUS = 1;
	private static final int PIT1 = 2;
	private static final int PIT2 = 3;
	private static final int BAT1 = 4;
	private static final int BAT2 = 5;
	private static final int MAX_ARROWS = 5;
	private static final int MAX_ARROW_RANGE = 5;
	
	//game variables (non-preferences)
	private static Calendar gameStarted, gameEnded;
	private static int arrows, moveNumber, currentLocation, range, pathSpecified;
	private static int[] location = new int[7];
	private static Map<Integer, int[]> map = new HashMap<Integer, int[]>();
	private static AlertDialog alert;
	private static int chosenItem = -1;
	private static final int[] m = new int[7];
	private static int[] p = new int[MAX_ARROW_RANGE];
	private static boolean requestInit, settled, gameOver, firstRun, limitChanged;
	private static String masterLog;
	private static int pitsNearby, batsNearby;
	
	private void setupMap() {
//		map.put(1, new int[]{2, 5, 8});
//		map.put(2, new int[]{1, 3, 10});
//		map.put(3, new int[]{2, 4, 12});
//		map.put(4, new int[]{3, 5, 14});
//		map.put(5, new int[]{1, 4, 6});
//		map.put(6, new int[]{5, 7, 15});
//		map.put(7, new int[]{6, 8, 17});
//		map.put(8, new int[]{1, 7, 9});
//		map.put(9, new int[]{8, 10, 18});
//		map.put(10, new int[]{2, 9, 11});
//		map.put(11, new int[]{10, 12, 19});
//		map.put(12, new int[]{3, 11, 13});
//		map.put(13, new int[]{12, 14, 20});
//		map.put(14, new int[]{4, 13, 15});
//		map.put(15, new int[]{6, 14, 16});
//		map.put(16, new int[]{15, 17, 20});
//		map.put(17, new int[]{7, 16, 18});
//		map.put(18, new int[]{9, 17, 19});
//		map.put(19, new int[]{11, 18, 20});
//		map.put(20, new int[]{13, 16, 19});
		
		Grid grid = new Grid(WIDTH, HEIGHT);
		
		grid.deleteNode(5);
		
		for (int i=0; i<MAX_ROOMS; i++) {
			if (grid.isAvailable(i)) {
				map.put(i, grid.getConnectedNodes(i));
			}
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		
		masterLog = "";
		
//		surfaceView = (SurfaceView) findViewById(R.id.SurfaceView01);
		scrollView = (ScrollView) findViewById(R.id.ScrollView);
		textView = (TextView) findViewById(R.id.TextView02);
		arrowView = (TextView) findViewById(R.id.arrowView);
		moveView = (TextView) findViewById(R.id.moveView);
		
		shootButton = (Button) findViewById(R.id.Button01);
		moveButton = (Button) findViewById(R.id.Button02);
		startGameButton = (Button) findViewById(R.id.Button03);
		clearLogButton = (Button) findViewById(R.id.Button04);
		
		// Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		//Create an Instance with this Activity
		glSurface = (GLSurfaceView) findViewById(R.id.SurfaceView01);
		//Set our own Renderer
		glSurface.setRenderer(new SurfaceViewClass());
		//Set the GLSurface as View to this Activity
//		setContentView(glSurface);
//		surfaceView = glSurface;
		
		shootButton.setEnabled(false);
		shootButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				createPromptRangeAlert();
				alert.show();
				
			}
		});
		
		moveButton.setEnabled(false);
		moveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				createMoveAlert();
				alert.show();

			}
		});
		
		startGameButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gameOver) {
					startNewGame();
				} else {
					createConfirmEndGameAlert();
					alert.show();
				}
				
			}
		});
		
		clearLogButton.setEnabled(false);
		
		clearLogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createConfirmClearLogAlert();
				alert.show();
			}
		});
		
		Toast.makeText(AWumpus.this,
				R.string.welcome,
				Toast.LENGTH_SHORT).show();
		
		firstRun = true;
		gameOver = true;
		limitChanged = true;
    }
    
    private void move() {
		if (!(chosenItem > -1 && chosenItem < MAX_EDGES_PER_ROOM)) {
			Toast.makeText(getApplicationContext(), getString(R.string.invalidInput), Toast.LENGTH_SHORT).show();
			return;
		}
		
		location[PLAYER] = map.get(location[PLAYER])[chosenItem];
		currentLocation = location[PLAYER];
		
		printInfo();

		moveNumber++;
		updateMoveView();
    }
    
	protected void createMoveAlert() {
		int[] tmp = new int[MAX_EDGES_PER_ROOM];
		tmp = map.get(location[PLAYER]);
		Arrays.sort(tmp);
		
		final CharSequence[] items = { getString(R.string.room) + " " + tmp[0],
				getString(R.string.room) + " " + tmp[1],
				getString(R.string.room) + " " + tmp[2],
				getString(R.string.room) + " " + tmp[3] };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.moveAlertTitle);
		chosenItem = -1;
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
//		        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
		    	chosenItem = item;
		    	move();
		    }
		});
		alert = builder.create();
	}
	
	protected void createReshuffleMapAlert() {
		final CharSequence[] items = { getString(R.string.no),
				getString(R.string.yes) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.reshuffleMapAlertTitle);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if (item == 0) {
		    		requestInit = false;
		    	} else {
		    		requestInit = true;
		    	}
		    	runGame();
		    }
		});
		alert = builder.create();
	}
	
	protected void createConfirmEndGameAlert() {
		final CharSequence[] items = { getString(R.string.no),
				getString(R.string.yes) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirmEndGameAlertTitle);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if (item == 0) {
		    		return;
		    	} else {
		    		stopGame();
		    	}
		    }
		});
		alert = builder.create();
	}
	
	protected void createConfirmClearLogAlert() {
		final CharSequence[] items = { getString(R.string.no),
				getString(R.string.yes) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirmClearLogAlertTitle);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if (item == 0) {
		    		return;
		    	} else {
		    		initializeLog(true);
		    	}
		    }
		});
		alert = builder.create();
	}
	
	// TODO shoot menu is broken currently
	protected void createPromptRangeAlert() {
		// TODO make dynamic based on max range
		final CharSequence[] items = { "1", "2", "3", "4", "5" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// TODO strings
		builder.setTitle("Select range (rooms):");
		chosenItem = -1;
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	range = item + 1;
		    	p = new int[MAX_ARROW_RANGE];
		    	for (int i=0; i<MAX_ARROW_RANGE; i++) {
		    		p[i] = 0;
		    	}
		    	pathSpecified = 0;

		    	// TODO strings
	    		appendDebugToEventLog("getting path for range " + range + "...");
	    		
		    	createPromptPathAlert();
		    	alert.show();
		    }
		});
		alert = builder.create();
	}
	
	protected void createPromptPathAlert() {
		final int[] destinations = new int[] {
				map.get(currentLocation)[0],
				map.get(currentLocation)[1],
				map.get(currentLocation)[2]
		};
		
    	if (pathSpecified > 2) {
    		for (int i=0; i<MAX_EDGES_PER_ROOM; i++) {
	    		if (destinations[i] == p[pathSpecified - 2]) {
	    			destinations[i] = -1;
	    		}
    		}
    	}
    	
    	final ArrayList<Integer> newDestsArr = new ArrayList<Integer>();
    	for (int i=0; i<MAX_EDGES_PER_ROOM; i++) {
    		if (destinations[i] != -1) {
    			newDestsArr.add(destinations[i]);
    		}
    	}
		
    	if (newDestsArr.size() < 1) {
    		throw new UnknownError();
    	}
    	
		final CharSequence[] items = new CharSequence[newDestsArr.size()];

    	for (int i=0; i<newDestsArr.size(); i++) {
    		items[i] = getString(R.string.room) + " " + newDestsArr.get(i);
    	}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select target " + (pathSpecified + 1) + " of " + range);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
	    		if (pathSpecified > 0) {
			    	if (p[pathSpecified-1]==0) {
			    		// TODO strings
			    		appendDebugToEventLog("Shoot routine canceled by user");
		    			return;
		    		}
	    		}
	    		
		    	p[pathSpecified] = newDestsArr.get(item);
		    	currentLocation = newDestsArr.get(item);
		    	pathSpecified++;
		    	
		    	if (pathSpecified == range) {
		    		// TODO strings
		    		appendDebugToEventLog("shooting...");
		    		shoot();
		    	} else if (pathSpecified < range) {
			    	createPromptPathAlert();
			    	alert.show();
		    	}
		    }
		});
		alert = builder.create();
	}
	
	private void shoot() {
//		currentLocation = location[PLAYER];
		
		appendToEventLog(getString(R.string.shotArrow) + " " + p[range - 1] + ".");
		arrows--;
		moveNumber++;
		
		updateArrowView();
		updateMoveView();
		
		// check for hit here
		currentLocation = location[PLAYER];
		for (int i = 0; i < range; i++) {
			currentLocation = p[i];
			checkForHit();
		}
		
		currentLocation = location[PLAYER];
		if (!gameOver) {
			appendToEventLog(R.string.missed);
			moveWumpus();
		}
		
		if (arrows == 0) {
			arrowView.setTextColor(getResources().getColor(R.color.outOfArrowsFontColor));
			appendToEventLog(R.string.outOfArrows);
			appendToEventLog(R.string.deathByWumpus);
			shootButton.setEnabled(false);
			if (mustHaveArrows) {
				stopGame();
			}
		}
				
		if (!gameOver) {
			printInfo();
		}
	}
	
	private void checkForHit() {
		if (currentLocation == location[WUMPUS]) {
			// TODO strings
			appendToEventLog("AHA! YOU GOT THE WUMPUS!");
			gameOver = true;
			stopGame();
//			f = 1;
			return;
		} else {
			if (currentLocation == location[PLAYER]) {
				// TODO strings
				appendToEventLog("OUCH! ARROW GOT YOU!");
				gameOver = true;
				stopGame();
//				f = -1;
				return;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// ---save whatever you need to persist—
//		outState.putString("ID", "1234567890");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// ---retrieve the information persisted earlier---
//		String ID = savedInstanceState.getString("ID");
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			// Launch Preference activity
			Intent preferencesIntent = new Intent(AWumpus.this, Preferences.class);
			startActivity(preferencesIntent);
			break;
		case R.id.about:
			//launch about activity
			Intent aboutIntent = new Intent(AWumpus.this, About.class);
			startActivity(aboutIntent);
			break;
		case R.id.highScoresContent:
			Intent highScoresIntent = new Intent(AWumpus.this, HighScores.class);
			startActivity(highScoresIntent);
			break;
		}
		return true;
	}
	
	private void updateArrowView() {
		arrowView.setText(getString(R.string.arrowsLabel) + " " + arrows); 
	}
	
	private void updateMoveView() {
		moveView.setText(getString(R.string.moveLabel) + moveNumber);
	}
	
	private void appendToEventLog(int res) {
		appendToEventLog(getString(res));
	}
	
	private void appendToEventLog(String str) {
		masterLog += str + "\n";
		
		int count = countLines(masterLog);
		
//		System.out.println("debug: log = {" + masterLog + "},");
//		System.out.println("counted " + count + " newlines, max scrollback = " + maxLogLines);
		
		if (count > maxLogLines) {
			for (int i = 0; i < count - maxLogLines; i++) {
				masterLog = masterLog.replaceFirst("[^\n]*\n", "");
			}
		}
		
//		textView.append(str + "\n");
		textView.setText(masterLog);
		
		autoScroll(textView, scrollView);
		
//		if (useDebug) {
//			titleBar.setText(m[1] + " " + m[2] + " " + m[3] + " " + m[4] + " " + m[5] + " " + m[6]);
//		}
	}
	
	private static int countLines(String str) {
		String[] lines = str.split("\r\n|\r|\n");
		return lines.length;
	}
	
	private void startNewGame() {
		useDebug = preferences.getBoolean("enableDebug", true);
		canSmellWumpus = preferences.getBoolean("canSmellWumpus", true);
		mustHaveArrows = preferences.getBoolean("mustHaveArrows", true);
		clearLogOnReshuffle = preferences.getBoolean("clearLogOnReshuffle", true);
		mobileBats = preferences.getBoolean("mobileBats", false);
		
		try {
			maxLogLines = Integer.parseInt(preferences.getString("maxLogLines", "" + DEFAULT_MAX_LOG_LINES));
			if (maxLogLines < 30 || maxLogLines > 1000) {
				throw new NumberFormatException();
			}
			limitChanged = true;
		} catch (NumberFormatException e) {
			maxLogLines = DEFAULT_MAX_LOG_LINES;
			
			if (limitChanged) {
				Toast.makeText(AWumpus.this,
						R.string.malformedMaxLogLinesPref,
						Toast.LENGTH_SHORT).show();
				limitChanged = false;
			}
		}
		
		gameOver = false;
		
		setupMap();
		
		if (firstRun) {
			requestInit = true;
			firstRun = false;
			runGame();
		} else {
			createReshuffleMapAlert();
			alert.show();
		}
		
	}
	
	private void runGame() {
		
		if (requestInit) {
			if (clearLogOnReshuffle) {
				initializeLog(true);
			}
			setLocations();
		} else {
			setOldLocations();
		}
		
		arrows = MAX_ARROWS;
		moveNumber = 1;
		currentLocation = location[PLAYER];
		arrowView.setTextColor(getResources().getColor(R.color.normalArrowsFontColor));
		
		updateArrowView();
		updateMoveView();
		
		shootButton.setEnabled(true);
		moveButton.setEnabled(true);
		
		initializeLog();
		
		gameStarted = Calendar.getInstance();
		appendToEventLog(getString(R.string.newGameStarted) + " " + getDateString(gameStarted) + ".\n");
		
		
		appendDebugToEventLog(R.string.debugMessage);
		
		appendDebugToEventLog(R.string.smellableDebug);
		
		printInfo();
		
		if (gameOver) {
			return;
		} else {
			startGameButton.setText(R.string.stopGame);
			clearLogButton.setEnabled(true);
		}
	}
	
	private void printInfo() {
		settled = false;
		while (!settled) {
			appendToEventLog(getString(R.string.youAreInRoom) + " " + location[PLAYER] + ".");
			
			checkForHazards();
		}
		
		if (gameOver) {
			return;
		}
		
		pitsNearby = 0;
		batsNearby = 0;
		
		// for each NPC...
		for (int j = 1; j < MAX_ENTITIES; j++) {

			boolean notAdjacent = true;
			for (int i=0; i<MAX_EDGES_PER_ROOM; i++) {
				if (map.get(location[PLAYER])[i] == location[j]) {
					notAdjacent = false;
					break;
				}
			}
			
			// if this NPC is not adjacent to the player, move on to the next NPC
			if (notAdjacent) {
				continue;
				
				//otherwise it is adjacent to the player, so print a notification
			} else {
				if (j == WUMPUS) {
					if (canSmellWumpus) {
						appendToEventLog(R.string.wumpusNearby);
					}
					continue;
				}
				if (j == PIT1 || j == PIT2) {
					pitsNearby++;
					continue;
				}
				if (j == BAT1 || j == BAT2) {
					batsNearby++;
					continue;
				}
				appendDebugToEventLog(R.string.warningFailure);
			}
		}
		
		if (pitsNearby == 1) {
			appendToEventLog(R.string.pitNearby);
		} else if (pitsNearby > 1) {
			appendToEventLog(R.string.pitsNearby);
		}
		
		if (batsNearby == 1) {
			appendToEventLog(R.string.batNearby);
		} else if (batsNearby > 1) {
			appendToEventLog(R.string.batsNearby);
		}
		
		int[] rooms = new int[MAX_EDGES_PER_ROOM];
		
		for (int i=0; i<MAX_EDGES_PER_ROOM; i++) {
			rooms[i] = map.get(location[PLAYER])[i];
		}
		
		appendToEventLog(getString(R.string.adjacentRooms) + getSortedRoomString(rooms));
	}

	private String getSortedRoomString(int[] rooms) {
		String retVal = " ";
		
		Arrays.sort(rooms);
		
		for (int i=0; i<rooms.length; i++) {
			retVal += "" + rooms[i];
			if (i < rooms.length - 1) {
				retVal += ", ";
			}
		}
		
		retVal += ".";
		
		return retVal;
	}

	private void stopGame() {
		gameOver = true;
		
		gameEnded = Calendar.getInstance();
		Calendar cal = gameEnded;
		cal.setTimeInMillis(gameEnded.getTimeInMillis() - gameStarted.getTimeInMillis());
		appendToEventLog(getString(R.string.gameEnded) + " (" + getElapsedTimeHoursMinutesSecondsString(cal) + " " + getString(R.string.elapsed) + ").");
		
		startGameButton.setText(R.string.startGame);
		shootButton.setEnabled(false);
		moveButton.setEnabled(false);
	}
	
	private void initializeLog() {
		if (textView.getText() == getString(R.string.defaultLogMessage)) {
			initializeLog(true);
		}
	}
	
	private void initializeLog(boolean doIt) {
		if (doIt) {
			masterLog = "";
			textView.setText("");
		}
	}
	
	private static String getDateString(Calendar cal) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	private String getElapsedTimeHoursMinutesSecondsString(Calendar cal) {       
	    long elapsedTime = cal.getTimeInMillis(); 
	    String format = String.format("%%0%dd", 2);  
	    elapsedTime = elapsedTime / 1000;  
	    String seconds = String.format(format, elapsedTime % 60);  
	    String minutes = String.format(format, (elapsedTime % 3600) / 60);  
	    String hours = String.format(format, elapsedTime / 3600);  
	    String time =  hours + ":" + minutes + ":" + seconds;  
	    return time;  
	}
	
	private void autoScroll(final TextView tv, final ScrollView sv) {
	    sv.post(new Runnable() {
	        public void run() {
	            sv.scrollTo(0, tv.getLineHeight()*tv.getLineCount());
	        }
	    });
	}
	
	private void setOldLocations() {
		System.arraycopy(m, 0, location, 0, MAX_ENTITIES);
		
		// TODO strings
//		appendDebugToEventLog("rolled back to stored locations");
	}
	
	private void setLocations() {
		// locate items
		for (int i = 0; i < MAX_ENTITIES; i++) {
			location[i] = a_getRandomRoom();
		}
		System.arraycopy(location, 0, m, 0, MAX_ENTITIES);
		
		while (checkForOverlappedLocations()) {
			setLocations();
		}
		
		// TODO strings
//		appendDebugToEventLog("new locations set");
	}

	private int a_getRandomRoom() {
		return (int) (Math.random() * MAX_ROOMS + 1);
	}

	private int b_getRandomEdge() {
		return (int) (Math.random() * 2 + 1);
	}

	private int c_getRandomWumpusAction() {
		return (int) (Math.random() * MAX_EDGES_PER_ROOM);
	}
	
	/**
	 * @return False if locations do not overlap, true if they do.
	 */
	private boolean checkForOverlappedLocations() {
		for (int i = 0; i < MAX_ENTITIES; i++) {
			for (int j = 0; j < MAX_ENTITIES; j++) {
				if (i == j) {
					continue;
				} else if (location[i] == location[j]) {
//					setLocations();
					return true;
				}
			}
		}
		
//		appendDebugToEventLog(R.string.locationsSet);
		return false;

	}
	
	private void appendDebugToEventLog(int res) {
		appendDebugToEventLog(getString(res));
	}
	
	private void appendDebugToEventLog(String string) {
		if (useDebug) {
			appendToEventLog(string);
		}
	}

	private void moveWumpus() {
		//TODO reset wumpus tracking marks
		int k = c_getRandomWumpusAction();
		if (k == MAX_EDGES_PER_ROOM) {
			if (location[WUMPUS] != currentLocation) {
				return;
			}
		} else {
			location[WUMPUS] = map.get(location[WUMPUS])[k];
		}
		if (location[WUMPUS] == location[PLAYER]) {
			appendToEventLog(R.string.deathByWumpus);

			stopGame();
			return;
		}
	}
	
	private void checkForHazards() {
		currentLocation = location[PLAYER];
		if (currentLocation == location[WUMPUS]) {
			appendToEventLog(R.string.bumpedTheWumpus);
			moveWumpus();
		} else if (currentLocation == location[PIT1]
				|| currentLocation == location[PIT2]) {
			appendToEventLog(R.string.deathByPit);
			
			stopGame();
			settled = true;
			return;
			
		} else if (currentLocation == location[BAT1]
				|| currentLocation == location[BAT2]) {
			
			appendToEventLog(R.string.batSnatch);
			location[PLAYER] = a_getRandomRoom();
			
			if (mobileBats) {
				// TODO strings
				System.out.println("shifting a bat... currently it is in "  + currentLocation);
				if (currentLocation == location[BAT1]) {
					while (true) {
						location[BAT1] = a_getRandomRoom();
						if (!checkForOverlappedLocations()) {
							// TODO strings
							System.out.println("found a non-overlapped configuration; new bat loc = " + location[BAT1]);
							break;
						}
						// TODO strings
						System.out.println("looking for another configuration...");
					}
				} else if (currentLocation == location[BAT2]) {
					while (true) {
						location[BAT2] = a_getRandomRoom();
						if (!checkForOverlappedLocations()) {
							// TODO strings
							System.out.println("found a non-overlapped configuration; new bat loc = " + location[BAT2]);
							break;
						}
						// TODO strings
						System.out.println("looking for another configuration...");
					}
				}
			}
			
			currentLocation = location[PLAYER];
			
			return;
		}
		settled = true;
	}
}
