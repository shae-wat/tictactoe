package scottmd3.tictactoe;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BoardView extends View {

	private static final String TAG = "Board View";

	// Width of the board grid lines
	public static final int GRID_LINE_WIDTH = 6;

	private Bitmap mHumanBitmap;
	private Bitmap mComputerBitmap;
	private Paint mPaint;
	private Rect mImageRect;
	
	
	private TicTacToeGame mGame;

	public BoardView(Context context) {
		super(context);		
		initialize();
	}


	public BoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		 
		initialize();
	}


	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);	            	
		initialize();
	}


	public void initialize() {   	
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mHumanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.x); 
		mComputerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.o);
		mImageRect = new Rect();
	}


	public void setGame(TicTacToeGame game) {
		mGame = game;
	}		


	public int getBoardCellWidth() {
		return getWidth() / 3;
	}


	public int getBoardCellHeight() {
		return getHeight() / 3;
	}


	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);		 

		Log.d(TAG, "in on draw");
		
		// Determine the width and height of the View
		int boardWidth = getWidth();
		int boardHeight = getHeight();

		// Make thick, light gray lines
		mPaint.setColor(Color.LTGRAY);        
		mPaint.setStrokeWidth(GRID_LINE_WIDTH);

		// Draw the two vertical board lines
		int cellWidth = boardWidth / 3;
		canvas.drawLine(cellWidth, 0, cellWidth, boardHeight, mPaint);
		canvas.drawLine(cellWidth * 2, 0, cellWidth * 2, boardHeight, mPaint);

		// draw the two horizontal lines
		canvas.drawLine(0, cellWidth, boardWidth, cellWidth, mPaint);
		canvas.drawLine(0, cellWidth * 2, boardWidth, cellWidth * 2, mPaint);

		// Draw all the X and O images
		for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {
			int col = i % 3;
			int row = i / 3;

			// Log.d(TAG, "row: " + row + " col: " + col);
			
			// Define the boundaries of a destination rectangle for the image
			mImageRect.left = col * cellWidth;
			mImageRect.top = row * cellWidth;
			mImageRect.right = mImageRect.left + cellWidth;
			mImageRect.bottom = mImageRect.top + cellWidth;

			
//			Log.d(TAG, "xtopLeft " + xTopLeft + " yTopLEft" + yTopLeft + " xBottomRight " + xBottomRight +
//					" yBottomRight" + yBottomRight);
//
//			Log.d(TAG, "mGame: " + mGame + " human " + (mGame.getBoardOccupant(i) == TicTacToeGame.HUMAN_PLAYER));
//			Log.d(TAG, "mGame: " + mGame + " human " + (mGame.getBoardOccupant(i) == TicTacToeGame.COMPUTER_PLAYER));
//			Log.d(TAG, "occupant:" + mGame.getBoardOccupant(i) + "- i" + i);
			if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.HUMAN_PLAYER) {	 
				canvas.drawBitmap(mHumanBitmap, 
						null,  // src
						mImageRect,  // dest
						null);
				// Log.d(TAG, "drawing human player");
			}
			else if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.COMPUTER_PLAYER) {
				canvas.drawBitmap(mComputerBitmap, 
						null,  // src
						mImageRect,  // dest 
						null);	
				// Log.d(TAG, "drawing computer player");
			}
		}

	}


}
