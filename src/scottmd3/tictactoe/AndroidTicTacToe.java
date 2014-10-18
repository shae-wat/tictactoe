package scottmd3.tictactoe;


import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTicTacToe extends Activity {

	private static final String TAG = "AndroidTicTacToe";

	// for preferences
	static final int DIALOG_DIFFICULTY_ID = 0;
	static final int DIALOG_QUIT_ID = 1;
	static final int DIALOG_ABOUT_ID = 2;
	static final int DIALOG_RESET_ID = 3;
	private SharedPreferences mPrefs;
	private int mDiffLev;

	// Whose turn is it?
	private char mTurn;
	
	// Who starts the next game?
	private char mGoesFirst; 
	
	// for pausing game
	private Handler mPauseHandler;
	private Runnable myRunnable;

	// Keep track of wins
	private int mHumanWins = 0;
	private int mComputerWins = 0;
	private int mTies = 0;

	// game logic
	private TicTacToeGame mGame;

	// Various text displayed
	private TextView mInfoTextView;
	private TextView mHumanScoreTextView;
	private TextView mComputerScoreTextView;
	private TextView mTieScoreTextView;

	private boolean mGameOver; 
	private BoardView mBoardView;

	// for all the sounds  we play
	private SoundPool mSounds;
	private HashMap<Integer, Integer> mSoundIDMap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		// Restore the scores
		mHumanWins = mPrefs.getInt("mHumanWins", 0); 
		mComputerWins = mPrefs.getInt("mComputerWins", 0); 
		mTies = mPrefs.getInt("mTies", 0);
		mDiffLev = mPrefs.getInt("mDiffLev", 2);
		

		mGame = new TicTacToeGame();
		mBoardView = (BoardView) findViewById(R.id.board);
		mBoardView.setGame(mGame);
		mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[mDiffLev]);
		
		// Listen for touches on the board
		mBoardView.setOnTouchListener(mTouchListener);

		// get the TextViews
		mInfoTextView = (TextView) findViewById(R.id.information);
		mHumanScoreTextView = (TextView) findViewById(R.id.player_score);
		mComputerScoreTextView = (TextView) findViewById(R.id.computer_score);
		mTieScoreTextView = (TextView) findViewById(R.id.tie_score);

		mTurn = TicTacToeGame.HUMAN_PLAYER;
		mGoesFirst = TicTacToeGame.COMPUTER_PLAYER; // computer goes fist next game
		mPauseHandler = new Handler();
		
		if (savedInstanceState == null) { 
			mTurn = TicTacToeGame.HUMAN_PLAYER;
			mGoesFirst = TicTacToeGame.COMPUTER_PLAYER; // computer goes fist next game 
			startNewGame(true);
		}
		else{
			mGame.setBoardState(savedInstanceState.getCharArray("board")); 
			mGameOver = savedInstanceState.getBoolean("mGameOver");
			mTurn = savedInstanceState.getChar("mTurn");
			mGoesFirst = savedInstanceState.getChar("mGoesFirst"); 
			mInfoTextView.setText(savedInstanceState.getCharSequence("info")); 
//			mHumanWins = savedInstanceState.getInt("mHumanWins"); 
//			mComputerWins = savedInstanceState.getInt("mComputerWins");
//			mTies = savedInstanceState.getInt("mTies");
			
			startComputerDelay();
		}
		displayScores();
	}

	@Override
	protected void onResume() {		
		super.onResume();
		createSoundPool();
	}
	
	
	@Override
	protected void onStop() { 
		super.onStop();
		
		// Save the current scores
		SharedPreferences.Editor ed = mPrefs.edit(); 
		ed.putInt("mHumanWins", mHumanWins);
		ed.putInt("mComputerWins", mComputerWins); 
		ed.putInt("mTies", mTies);
		ed.putInt("mDiffLev", mDiffLev);
		ed.apply();
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState);
		outState.putCharArray("board", mGame.getBoardState()); 
		outState.putBoolean("mGameOver", mGameOver); 
		outState.putCharSequence("info", mInfoTextView.getText()); 
		outState.putChar("mTurn", mTurn);
		outState.putChar("mGoesFirst", mGoesFirst);
	}
	
	
	private void displayScores() { 
		mHumanScoreTextView.setText(Integer.toString(mHumanWins)); 
		mComputerScoreTextView.setText(Integer.toString(mComputerWins)); 
		mTieScoreTextView.setText(Integer.toString(mTies));
	}
	
	
	private void startComputerDelay() {
		// If it's the computer's turn, the previous turn was not completed, so go again 
		if (!mGameOver && mTurn == TicTacToeGame.COMPUTER_PLAYER) {
			int move = mGame.getComputerMove();
			setMove(TicTacToeGame.COMPUTER_PLAYER, move); 
		}
	}
	
	
	private void createSoundPool() {
		int[] soundIds = {R.raw.human_move, R.raw.computer_move, R.raw.human_win,
				R.raw.computer_win, R.raw.tie_game};
		mSoundIDMap = new HashMap<Integer, Integer>();
		mSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		for(int id : soundIds) 
			mSoundIDMap.put(id, mSounds.load(this, id, 1));
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.d(TAG, "in onPause");
		
		if(mSounds != null) {
			mSounds.release();
			mSounds = null;
		}		
	}


	// Set up the game baord. 
	private void startNewGame(boolean first) {
		// check if new game after a complete game and if so swap who goes first
		if(mGameOver) {
			mTurn = mGoesFirst;
			mGoesFirst = (mGoesFirst == TicTacToeGame.COMPUTER_PLAYER) ? 
					TicTacToeGame.HUMAN_PLAYER : TicTacToeGame.COMPUTER_PLAYER;
		}
		// if human quit when it was their turn then Android gets to go first
		else if(mTurn == TicTacToeGame.HUMAN_PLAYER && !first) {
			mTurn = TicTacToeGame.COMPUTER_PLAYER;
			mGoesFirst = TicTacToeGame.HUMAN_PLAYER;
		}
		mGameOver = false;

		mGame.clearBoard();  
		mBoardView.invalidate();   // Redraw the board    

		// Who starts?
		if (mTurn == TicTacToeGame.COMPUTER_PLAYER) {
			Log.d(TAG, "Computers turn!!!");
			mInfoTextView.setText(R.string.first_computer);
			int move = mGame.getComputerMove();
			setMove(TicTacToeGame.COMPUTER_PLAYER, move);
		}
		else {
			mInfoTextView.setText(R.string.first_human); 
		}	
	}

	
	// Make a move
	private boolean setMove(char player, int location) {

		if (player == TicTacToeGame.COMPUTER_PLAYER) {    		
			// EXTRA CHALLENGE!
			// Make the computer move after a delay of 1 second
			myRunnable = createRunnable(location);
			mPauseHandler.postDelayed(myRunnable, 1000); 
			return true;
		}
		else if (mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location)) { 
			mTurn = TicTacToeGame.COMPUTER_PLAYER;
			mBoardView.invalidate();   // Redraw the board
			mSounds.play(mSoundIDMap.get(R.raw.human_move), 1, 1, 1, 0, 1);	    	   	
			return true;
		}
		// should never get here
		return false;
	}
	
	
	private Runnable createRunnable(final int location) {
		return new Runnable() {
			public void run() { 

				mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, location);
				// soundID, leftVolume, rightVolume, priority, loop, rate
				mSounds.play(mSoundIDMap.get(R.raw.computer_move), 1, 1, 1, 0, 1);	
				
				mBoardView.invalidate();   // Redraw the board

				int winner = mGame.checkForWinner();
				if (winner == 0) {
					mTurn = TicTacToeGame.HUMAN_PLAYER;	                                	
					mInfoTextView.setText(R.string.turn_human);
				}
				else 
					endGame(winner);
			} 
		};		
	}

	// Game is over logic
	private void endGame(int winner) {
		if (winner == 1) {
			mTies++;
			mTieScoreTextView.setText(Integer.toString(mTies));
			mInfoTextView.setText(R.string.result_tie); 
			mSounds.play(mSoundIDMap.get(R.raw.tie_game), 1, 1, 1, 0, 1);
		}
		else if (winner == 2) {
			mHumanWins++;
			mHumanScoreTextView.setText(Integer.toString(mHumanWins));
			mInfoTextView.setText(R.string.result_human_wins);
			mSounds.play(mSoundIDMap.get(R.raw.human_win), 1, 1, 1, 0, 1);
		}
		else {
			mComputerWins++;
			mComputerScoreTextView.setText(Integer.toString(mComputerWins));
			mInfoTextView.setText(R.string.result_computer_wins);
			mSounds.play(mSoundIDMap.get(R.raw.computer_win), 1, 1, 1, 0, 1);
		}
		mGameOver = true;
	}


	@Override 
	public boolean onCreateOptionsMenu(Menu menu) { 
		super.onCreateOptionsMenu(menu); 

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		Log.d(TAG, "in onOptionsItemSelected selecting");
		switch (item.getItemId()) {
		case R.id.new_game_option:
			if(myRunnable != null);
				mPauseHandler.removeCallbacks(myRunnable);
			// if computer is in pause, stop it.
			startNewGame(false);
			return true;
		}
		return false;
	}   


	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
		case DIALOG_DIFFICULTY_ID:

			builder.setTitle(R.string.difficulty_choose);

			final CharSequence[] levels = {
					getResources().getString(R.string.difficulty_easy),
					getResources().getString(R.string.difficulty_harder), 
					getResources().getString(R.string.difficulty_expert)};

			final int selected = mGame.getDifficultyLevel().ordinal();
			//Log.d(TAG, "selected difficulty value: " + selected + ", level: " + mGame.getDifficultyLevel());

			builder.setSingleChoiceItems(levels, selected, 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					dialog.dismiss();   // Close dialog

					mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[item]);
					Log.d(TAG, "Difficulty level: " + mGame.getDifficultyLevel());
					mDiffLev = mGame.getDifficultyLevel().ordinal();

					// Display the selected difficulty level
					Toast.makeText(getApplicationContext(), levels[item], 
							Toast.LENGTH_SHORT).show();        	    
				}
			});
			Log.d(TAG, "selected difficulty value: " + selected + ", level: " + mGame.getDifficultyLevel());
			dialog = builder.create();
			break;    // this case
		case DIALOG_QUIT_ID:
			// Create the quit confirmation dialog

			builder.setMessage(R.string.quit_question).setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					AndroidTicTacToe.this.finish();
				}
			})
			.setNegativeButton(R.string.no, null);   
			dialog = builder.create();
			break;
		case DIALOG_ABOUT_ID:
			Context context = getApplicationContext();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.about_dialog, null); 		
			builder.setView(layout);
			builder.setPositiveButton("OK", null);	
			dialog = builder.create();
			break;
		case DIALOG_RESET_ID:
			mHumanWins = 0; 
			mComputerWins = 0;
			mTies = 0;
			displayScores();
			break;
		}

		if(dialog == null)
			Log.d(TAG, "Uh oh! Dialog is null");
		else
			Log.d(TAG, "Dialog created: " + id + ", dialog: " + dialog);
		return dialog;        
	}

	// Listen for touches on the board
	private OnTouchListener mTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {

			// Determine which cell was touched	    	
			int col = (int) event.getX() / mBoardView.getBoardCellWidth();
			int row = (int) event.getY() / mBoardView.getBoardCellHeight();
			int pos = row * 3 + col;

			if (!mGameOver && mTurn == TicTacToeGame.HUMAN_PLAYER &&
					setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{        		

				// If no winner yet, let the computer make a move
				int winner = mGame.checkForWinner();
				if (winner == 0) { 
					mInfoTextView.setText(R.string.turn_computer); 
					int move = mGame.getComputerMove();
					setMove(TicTacToeGame.COMPUTER_PLAYER, move);            		
				} 
				else
					endGame(winner);           	

			} 

			// So we aren't notified of continued events when finger is moved
			return false;
		} 
	};

}
