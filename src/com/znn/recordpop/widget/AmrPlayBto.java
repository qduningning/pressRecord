package com.znn.recordpop.widget;

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class AmrPlayBto extends ImageView {

	public AmrPlayBto(Context context) {
		super(context);
	}

	public AmrPlayBto(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AmrPlayBto(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// ------------------------------------------------------//
	// String testAMRurl =
	// "http://www.ai-ring.cn/ring/ring/200805041311224.amr";

	MediaPlayer mediaPlayer;

	PlayOnClickListener clickListener;
	String url;
	FileDescriptor fd;

	public int setUrl(String url) {
		stopFlash();
		this.url = url;
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					playing = false;
					stopFlash();
				}
			});
		}
		if (clickListener == null) {
			clickListener = new PlayOnClickListener();
		}
		setOnClickListener(clickListener);
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(url);
			mediaPlayer.prepareAsync();// prepare之后播放
			return mediaPlayer.getDuration();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getContext(), "录音文件不存在！", Toast.LENGTH_SHORT).show();
		}
		return 0;
	}

	public int setFD(FileDescriptor fd) {
		stopFlash();
		this.fd = fd;
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					playing = false;
					stopFlash();
				}
			});
		}
		if (clickListener == null) {
			clickListener = new PlayOnClickListener();
		}
		setOnClickListener(clickListener);
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(fd);
			mediaPlayer.prepareAsync();// prepare之后播放
			return mediaPlayer.getDuration();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getContext(), "录音文件不存在！", Toast.LENGTH_SHORT).show();
		}
		return 0;
	}

	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.seekTo(0);
			mediaPlayer.pause();
		}
		stopFlash();
		playing = false;
	}

	private boolean playing = false;

	class PlayOnClickListener implements OnClickListener {

		public PlayOnClickListener() {
			super();
		}

		@Override
		public void onClick(View arg0) {
			if (mediaPlayer != null) {
				if (playing) {
					stop();
				} else {
					mediaPlayer.start();
					startFlash();
					playing = true;
				}
			} else {
				Toast.makeText(getContext(), "录音文件不存在！", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	// ------------------------------------------------------------------------//
	private Runnable mTicker;
	private Handler mHandler;
	private int i = 5;
	private int[] alpha = new int[] { 55, 105, 205, 255, 205, 105 };

	private boolean mTickerStopped = true;

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mHandler = new Handler();
		/**
		 * requests a tick on the next hard-second boundary
		 */
		mTicker = new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (mTickerStopped) {
					return;
				}
				setAlpha(alpha[i]);
				invalidate();
				i++;
				if (i >= alpha.length)
					i = 0;
				mHandler.postDelayed(mTicker, 200);
			}
		};
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopFlash();
	}

	public void startFlash() {
		if (mTickerStopped) {
			mTickerStopped = false;
			mTicker.run();
		}
	}

	@SuppressWarnings("deprecation")
	public void stopFlash() {
		mTickerStopped = true;
		setAlpha(255);
	}
}
