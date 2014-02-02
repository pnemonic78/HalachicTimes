package net.sf.times;

import java.util.Random;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.widget.ImageView;

/**
 * Flicker animation for 1 candle.
 * 
 * @author Moshe W
 */
public class CandleAnimation implements Runnable {

	private static final int SPRITES_COUNT = 2;
	private static final int PERIOD = (int) (DateUtils.SECOND_IN_MILLIS >> 1);

	private final Handler mHandler;
	private final ImageView mView;
	private final Drawable[] mSprites = new Drawable[SPRITES_COUNT];
	private int mSpriteIndex;
	/** Randomizer. */
	private final Random mRandom = new Random();

	/**
	 * Create a new animation.
	 * 
	 * @param handler
	 *            the timer.
	 * @param view
	 *            the image view.
	 */
	public CandleAnimation(Handler handler, ImageView view) {
		mHandler = handler;
		if (view == null)
			throw new IllegalArgumentException("view required");
		mView = view;

		Resources res = view.getResources();
		mSprites[0] = res.getDrawable(R.drawable.candle_0);
		mSprites[1] = res.getDrawable(R.drawable.candle_1);
	}

	@Override
	public void run() {
		int index = mSpriteIndex;
		mView.setImageDrawable(mSprites[index]);
		index = (index + 1) % SPRITES_COUNT;
		mSpriteIndex = index;
		mHandler.postDelayed(this, mRandom.nextInt(PERIOD));
	}

}
