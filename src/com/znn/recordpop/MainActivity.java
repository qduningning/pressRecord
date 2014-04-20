package com.znn.recordpop;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.znn.recordpop.widget.AmrPlayBto;
import com.znn.recordpop.widget.RecordButton;
import com.znn.recordpop.widget.RecordButton.OnFinishedRecordListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private AmrPlayBto mPlayButton;
	private RecordButton mRecordButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mRecordButton = (RecordButton) findViewById(R.id.record_button);
		mPlayButton = (AmrPlayBto) findViewById(R.id.play_button);
		mPlayButton.setVisibility(View.GONE);

		mRecordButton.setOnFinishedRecordListener(new OnFinishedRecordListener() {

			@Override
			public void onFinishedRecord(String audioPath) {
				if (audioPath != null) {
					// Toast.makeText(context, "录音完成：" + audioPath,
					// Toast.LENGTH_SHORT).show();
					mPlayButton.setVisibility(View.VISIBLE);
					audioPath = audioPath;
					try {
						mPlayButton.setFD(new FileInputStream(new File(
								audioPath)).getFD());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					mPlayButton.setVisibility(View.GONE);
					audioPath = null;
				}
			}
		});
	}

//	static class ShowVolumeHandler extends Handler {
//		@Override
//		public void handleMessage(Message msg) {
//			if (msg.what > -1 && msg.what < 7) {
//				volume_anim.setImageResource(res[msg.what]);
//			} else if (7 == msg.what) {
//				recordIndicator.dismiss();
//			}
//		}
//	}

}
