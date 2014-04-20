## 录音效果(类似微信)
按住按钮会显示一个正在录音的动态窗口，自动识别音量

大部分逻辑写在自定义组件RecordButton.java 和 AmrPlayBto.java
xml简单引用即可
``` java
private AmrPlayBto mPlayButton;
private RecordButton mRecordButton;

mRecordButton.setOnFinishedRecordListener(new OnFinishedRecordListener() {
	@Override
	public void onFinishedRecord(String audioPath) {
		if (audioPath != null) {
			// Toast.makeText(context, "录音完成：" + audioPath,
			// Toast.LENGTH_SHORT).show();
			mPlayButton.setVisibility(View.VISIBLE);
			...
		} else {
			mPlayButton.setVisibility(View.GONE);
			...
		}
	}
});



效果图
![img1](https://github.com/qduningning/pressRecord/raw/master/device-2014-04-21-000029.png)
![img2](https://github.com/qduningning/pressRecord/raw/master/device-2014-04-21-000039.png)
![img3](https://github.com/qduningning/pressRecord/raw/master/device-2014-04-21-000047.png)
