package com.znn.recordpop.widget;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.znn.recordpop.R;
import com.znn.recordpop.R.drawable;
import com.znn.recordpop.R.id;
import com.znn.recordpop.R.layout;
import com.znn.recordpop.R.style;

/***
 * 录音按钮
 * 
 * @author Administrator
 * 
 */
public class RecordButton extends Button {

	Context context;

	public RecordButton(Context context) {
		super(context);
		init(context);
	}

	public RecordButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public RecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
		finishedListener = listener;
	}

	@SuppressLint("SimpleDateFormat")
	private static DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-M-dd_HH:mm:ss");
	private String mFileName = null;
	private String dir_path = null;

	private OnFinishedRecordListener finishedListener;

	private static final int MIN_INTERVAL_TIME = 2000;// 2s
	private long startTime;

	private static Dialog recordIndicator;

	private static int[] res = { R.drawable.amp1, R.drawable.amp2,
			R.drawable.amp3, R.drawable.amp4, R.drawable.amp5, R.drawable.amp6,
			R.drawable.amp7 };

	private static View view;
	private static ViewFlipper viewFlipper;
	private static ImageView volume_anim;
	private static TextView result_text;

	private MediaRecorder recorder;

	private ObtainDecibelThread thread;

	private Handler volumeHandler;

	private void init(Context context) {
		this.context = context;
		volumeHandler = new ShowVolumeHandler();
	}

	boolean outside_move = false;
	boolean start_record = false;
	float remY = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			setBackgroundResource(R.drawable.btn_rcd_press);
			if (!start_record) {
				start_record = true;
				initDialogAndStartRecord();
				viewFlipper.setDisplayedChild(0);
				outside_move = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			setBackgroundResource(R.drawable.btn_rcd_def);
			if (start_record) {
				start_record = false;
				if (outside_move) {
					cancelRecord();
				} else {
					finishRecord();
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:// 当手指上滑，取消录音
			if (start_record) {
				if (event.getY() > 0 && event.getX() > 0
						&& event.getX() < getWidth()) {
					outside_move = false;
					viewFlipper.setDisplayedChild(0);
				} else {
					outside_move = true;
					viewFlipper.setDisplayedChild(1);
				}
			}
			break;
		}

		return true;
	}

	private void initDialogAndStartRecord() {

		startTime = System.currentTimeMillis();
		if (dir_path == null) {
			dir_path = context.getFilesDir().getAbsolutePath() + File.separator
					+ "RecordRemDir";
			File dir = new File(dir_path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		view = View.inflate(context, R.layout.voice_rcd_hint_window, null);
		viewFlipper = (ViewFlipper) view
				.findViewById(R.id.voice_rcd_viewflipper);
		result_text = (TextView) view.findViewById(R.id.voice_rcd_result_text);
		viewFlipper.setDisplayedChild(0);
		volume_anim = (ImageView) view.findViewById(R.id.voice_rcd_hint_anim);
		recordIndicator = new Dialog(context, R.style.like_toast_dialog_style);
		recordIndicator.setContentView(view);

		// mFileName = dir_path + File.separator + dateFormat.format(new Date())
		// + ".amr";
		mFileName = dir_path + File.separator + "record.amr";

		if (startRecording()) {
			recordIndicator.show();
		}else{
			start_record = false;
		}
	}

	private void finishRecord() {
		stopRecording();

		long intervalTime = System.currentTimeMillis() - startTime;
		if (intervalTime < MIN_INTERVAL_TIME) {

			result_text.setText("录音时间太短");
			viewFlipper.setDisplayedChild(2);
			Message msg = new Message();
			msg.what = 7;
			volumeHandler.sendMessageDelayed(msg, 800);

			File file = new File(mFileName);
			file.delete();
			if (finishedListener != null)
				finishedListener.onFinishedRecord(null);
			return;
		}
		result_text.setText("录音完成");
		viewFlipper.setDisplayedChild(2);
		Message msg = new Message();
		msg.what = 7;
		volumeHandler.sendMessageDelayed(msg, 800);
		if (finishedListener != null)
			finishedListener.onFinishedRecord(mFileName);
	}

	private void cancelRecord() {
		stopRecording();
		result_text.setText("录音已经取消");
		viewFlipper.setDisplayedChild(2);
		Message msg = new Message();
		msg.what = 7;
		volumeHandler.sendMessageDelayed(msg, 800);
		File file = new File(mFileName);
		file.delete();
		if (finishedListener != null)
			finishedListener.onFinishedRecord(null);
	}

	@SuppressWarnings("deprecation")
	private boolean startRecording() {
		try {
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(mFileName);

			recorder.prepare();

			recorder.start();
			thread = new ObtainDecibelThread();
			thread.start();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void stopRecording() {
		if (thread != null) {
			thread.exit();
			thread = null;
		}
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}

	private class ObtainDecibelThread extends Thread {

		private volatile boolean running = true;

		public void exit() {
			running = false;
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (recorder == null || !running) {
					break;
				}
				int x = recorder.getMaxAmplitude();
				if (x != 0) {
					int f = (int) (10 * Math.log(x) / Math.log(10));
					if (f < 26)
						volumeHandler.sendEmptyMessage(0);
					else if (f < 29)
						volumeHandler.sendEmptyMessage(1);
					else if (f < 32)
						volumeHandler.sendEmptyMessage(2);
					else if (f < 35)
						volumeHandler.sendEmptyMessage(3);
					else if (f < 38)
						volumeHandler.sendEmptyMessage(4);
					else if (f < 41)
						volumeHandler.sendEmptyMessage(5);
					else
						volumeHandler.sendEmptyMessage(6);

				}

			}
		}

	}

	static class ShowVolumeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what > -1 && msg.what < 7) {
				volume_anim.setImageResource(res[msg.what]);
			} else if (7 == msg.what) {
				recordIndicator.dismiss();
			}
		}
	}

	public interface OnFinishedRecordListener {
		public void onFinishedRecord(String audioPath);
	}

}
