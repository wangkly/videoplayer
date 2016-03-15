package cn.com.liandisys.xmedu.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.Range;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.com.liandisys.xmedu.R;
import cn.com.liandisys.xmedu.adapter.ChapterAdapter;
import cn.com.liandisys.xmedu.adapter.ItemAdapter;
import cn.com.liandisys.xmedu.adapter.RemarkAdapter;
import cn.com.liandisys.xmedu.asynctask.AddMessageAsynTask;
import cn.com.liandisys.xmedu.asynctask.AddVideoCollectAsynTask;
import cn.com.liandisys.xmedu.asynctask.DeleteCollectAsynTask;
import cn.com.liandisys.xmedu.asynctask.VideoDetialInfoAsynTask;
import cn.com.liandisys.xmedu.asynctask.VideoMessagesAsynTask;
import cn.com.liandisys.xmedu.asynctask.VideoSeriesAsynTask;
import cn.com.liandisys.xmedu.common.DownloadUtil;
import cn.com.liandisys.xmedu.common.OSSUtil;
import cn.com.liandisys.xmedu.db.DBManager;
import cn.com.liandisys.xmedu.common.SharedPreUtils;

import cn.com.liandisys.xmedu.util.Consts;
import cn.com.liandisys.xmedu.util.StrUtils;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/***
 * 视频明细Activity
 * 和该界面关联需要传入videodetailid参数
 * 传入方法：intent.putExtra(Consts.VIDEO_DETAIL_ID, id);
 */
public class VideoDetailActivity extends BaseActivity implements OnClickListener {

	private View view;

	private ImageView iv_back;
	private ImageView iv_more;

	// "http://dlqncdn.miaopai.com/stream/MVaux41A4lkuWloBbGUGaQ__.mp4";
//	private String video_path = "http://abcd1234nj.oss-cn-shanghai.aliyuncs.com/XiaomiPhone.mp4";// 视频地址
	
	private String video_path = "http://ximanvideo.oss-cn-qingdao.aliyuncs.com/00047.mp4";
	

	private Button btn_price;
	private ImageView btn_collect;
	private ImageView btn_download;
	private ImageView btn_share;
	private TextView tx_title;
	private TextView tx_descripe;
	private LinearLayout layout_chapter;
	private TextView tx_total_chapter;
	private EditText edit_remark;
	private Button btn_remark;
	private ListView list_remark;
	private GridView grid_chapter;
	private ListView grid_commodity;

	// 视频播放的初始化
	private VideoView mVideoView;
	private MediaController mMediaController;
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg, mOperationPercent, ivSpeed;
	private TextView tvSpeed, tv_down_rate, tv_load_rate;
	private RelativeLayout rlSpeed, rlVideo;
	private AudioManager mAudioManager;
	private GestureDetector mGestureDetector;
	private FrameLayout ll_layout;
	private ProgressBar pb;
	private Button btnVideoFullScreen;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 当前缩放模式 */
	private int mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
	/** 指定播放进度 */
	private long setProgress = 0;
	/** 视频高度 */
	private int viewHeight = 0;// 视频控件的初始高度
	private OrientationEventListener mOrientationListener; // 屏幕方向改变监听器
	//private LinearLayout.LayoutParams videoViewParams;// 初始化的videoView
	private ViewGroup.LayoutParams videoViewParams;// 初始化的videoView
	private boolean mClick = false, // 是否点击全屏按钮
			mClickLand = true, // 进入横屏
			mClickPort = true, // 进入竖屏
			isPlayOver, needRestart, isFullScreen;
	
	private String videoDetailId;
	private String userid;
	private boolean isCollect;
	private String collectId;
	private int curPosition;
	private boolean isFree = true;
	private boolean isBuy = false;
	private double price;
	private List<Map<String, Object>> seriesList;
	private List<Map<String, Object>> messageList;
	private SharedPreferences preferences;
	
    //download
	private OSS oss;
    private static final String endpoint = "http://oss-cn-qingdao.aliyuncs.com";
//    private static final String accessKeyId = "";
//    private static final String accessKeySecret = "";
    private static  String testBucket = "ximanvideo";
    private static  String downloadObject = "00047.mp4";//XiaomiPhone.mp4

	@Override
	protected void onCreate(Bundle arg0) {
		Vitamio.isInitialized(getApplicationContext());

		super.onCreate(arg0);
		view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_videodetail, null);
		setContentView(view);

		iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_more = (ImageView) findViewById(R.id.iv_more);

		iv_back.setOnClickListener(this);
		iv_more.setOnClickListener(this);
		
		preferences= this.getSharedPreferences(SharedPreUtils.DB_NAME, Activity.MODE_PRIVATE);
		seriesList = new ArrayList<Map<String,Object>>();
		messageList = new ArrayList<Map<String,Object>>();
		isCollect = false;
		collectId = "";
		videoDetailId= getIntent().getExtras().getString(Consts.VIDEO_DETAIL_ID);
		userid = preferences.getString("userid", "");

		initView();
		initData();
	}

	private void initView() {
		//界面控件初始化
		btn_price = (Button) view.findViewById(R.id.btn_price);
		btn_collect = (ImageView) view.findViewById(R.id.btn_collect);
		btn_download = (ImageView) view.findViewById(R.id.btn_download);
		btn_share = (ImageView) view.findViewById(R.id.btn_share);
		tx_title = (TextView) view.findViewById(R.id.tx_title);
		tx_descripe = (TextView) view.findViewById(R.id.tx_descripe);
		layout_chapter = (LinearLayout) view.findViewById(R.id.layout_chapter);
		tx_total_chapter = (TextView) view.findViewById(R.id.tx_total_chapter);
		edit_remark = (EditText) view.findViewById(R.id.edit_remark);
		btn_remark = (Button) view.findViewById(R.id.btn_remark);
		list_remark = (ListView) view.findViewById(R.id.list_remark);
		grid_chapter = (GridView) view.findViewById(R.id.grid_chapter);
		grid_commodity = (ListView) view.findViewById(R.id.grid_commodity);
		
		btn_price.setOnClickListener(onBtnClick());
		btn_collect.setOnClickListener(onBtnClick());
		btn_download.setOnClickListener(onBtnClick());
		btn_share.setOnClickListener(onBtnClick());
		btn_remark.setOnClickListener(onBtnClick());
		btn_remark.setVisibility(View.INVISIBLE);
		btn_price.setVisibility(View.INVISIBLE);
		//评论框焦点事件监控
		edit_remark.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					btn_remark.setVisibility(View.VISIBLE);
				} else {
					edit_remark.setText("");
					btn_remark.setVisibility(View.INVISIBLE);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
				}
			}
		});
		
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		tvSpeed = (TextView) findViewById(R.id.tv_speed);
		ivSpeed = (ImageView) findViewById(R.id.iv_speed);
		rlSpeed = (RelativeLayout) findViewById(R.id.rl_speed);
		rlVideo = (RelativeLayout) findViewById(R.id.rl_study_video);

		videoViewParams = (ViewGroup.LayoutParams) rlVideo.getLayoutParams();
		viewHeight = videoViewParams.height;

		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		pb = (ProgressBar) findViewById(R.id.probar);// 圆形加载进度
		tv_down_rate = (TextView) findViewById(R.id.tv_down_rate);// 下载速度
		tv_load_rate = (TextView) findViewById(R.id.tv_load_rate);// 加载速度
		ll_layout = (FrameLayout) findViewById(R.id.ll_layout);

		mMediaController = new MediaController(this, true, ll_layout);// 实例化控制器

		// 获得媒体控制器的全屏按钮，并加监听
		btnVideoFullScreen = (Button) mMediaController.findViewById(R.id.btn_fullscreen);
		btnVideoFullScreen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mClick = true;
				if (!isFullScreen) {
					changeToFullScreen(true);
					mClickLand = false;
				} else {
					fullScreenToNormal();
					mClickPort = false;
				}
			}
		});

		// 自定义媒体控制器，需先隐藏，不然会出现卡住不动的控制器，初次触摸后出现的控制器则为正常
		mMediaController.setVisibility(View.GONE);
		mMediaController.show(5000);// 控制器显示5s后自动隐藏
		mVideoView.setMediaController(mMediaController);// 绑定控制器
		mVideoView.setVideoPath(video_path);

		mVideoView.requestFocus();// 取得焦点
		mGestureDetector = new GestureDetector(mVideoView.getContext(), new MyGestureListener());

		mVideoView.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					if (mVideoView.isPlaying()) {
						mVideoView.pause();
						needRestart = true;
						pb.setVisibility(View.VISIBLE);
						tv_down_rate.setText("");
						tv_load_rate.setText("");
						tv_down_rate.setVisibility(View.VISIBLE);
						tv_load_rate.setVisibility(View.VISIBLE);
					}
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					if (needRestart) {
						mVideoView.start();
						needRestart = false;
					}
					pb.setVisibility(View.GONE);
					tv_down_rate.setVisibility(View.GONE);
					tv_load_rate.setVisibility(View.GONE);
					break;
				case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
					tv_down_rate.setText("" + extra + "kb/s" + " ");
					break;
				}
				return true;
			}
		});

		mVideoView.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {
			}
		});

		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				if (setProgress != 0 && setProgress < mp.getDuration()) {
					mp.seekTo(setProgress);
				}
				mp.start();
			}
		});

		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
			}
		});

		mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
			}
		});

		mVideoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				return false;
			}
		});

	}

	private final void startConfigChangeListener() {
		mOrientationListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int rotation) {
				if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
					if (mClick) {
						if (isFullScreen && !mClickLand) {
							return;
						} else {
							mClickPort = true;
							mClick = false;
							isFullScreen = false;
						}
					} else {
						if (isFullScreen) {
							fullScreenToNormal();
							mClick = false;
						}
					}
				} else if (((rotation >= 250) && (rotation <= 290))) {
					if (mClick) {
						if (!isFullScreen && !mClickPort) {
							return;
						} else {
							mClickLand = true;
							mClick = false;
							isFullScreen = true;
						}
					} else {
						if (!isFullScreen) {
							mClick = false;
							changeToFullScreen(true);
						}
					}
				} else if (((rotation >= 70) && (rotation <= 110))) {
					if (mClick) {
						if (!isFullScreen && !mClickPort) {
							return;
						} else {
							mClickLand = true;
							mClick = false;
							isFullScreen = true;
						}
					} else {
						if (!isFullScreen) {
							mClick = false;
							changeToFullScreen(false);
						}
					}
				}
			}
		};
		mOrientationListener.enable();
	}

	private void fullScreenToNormal() {
		isFullScreen = false;
		btnVideoFullScreen.setBackgroundResource(R.drawable.window_to_fullscreen_normal);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		videoViewParams.height = viewHeight;
		videoViewParams.width = LayoutParams.MATCH_PARENT;
		rlVideo.setLayoutParams(videoViewParams);
		mLayout = VideoView.VIDEO_LAYOUT_SCALE;
		mVideoView.setVideoLayout(mLayout, 0);
	}

	private void changeToFullScreen(boolean isLeft) {
		isFullScreen = true;
		btnVideoFullScreen.setBackgroundResource(R.drawable.window_bg);
		if (isLeft) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}
		videoViewParams.height = LayoutParams.MATCH_PARENT;
		videoViewParams.width = LayoutParams.MATCH_PARENT;
		rlVideo.setLayoutParams(videoViewParams);
		mLayout = VideoView.VIDEO_LAYOUT_SCALE;
		mVideoView.setVideoLayout(mLayout, 0);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endGesture();
			break;
		}
		return super.onTouchEvent(event);
	}

	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 500);
	}

	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
			rlSpeed.setVisibility(View.GONE);
		}
	};

	private class MyGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			float mOldX = e1.getX(), mOldY = e1.getY();
			int y = (int) e2.getRawY();
			Display disp = getWindowManager().getDefaultDisplay();
			int windowWidth = disp.getWidth();
			int windowHeight = disp.getHeight();

			if (((e1.getY() - e2.getY()) > 60 || (e2.getY() - e1.getY()) > 60) && mOldX > windowWidth * 2.0 / 3 && Math.abs(distanceY) > Math.abs(distanceX)) {
				onVolumeSlide((mOldY - y) / windowHeight);
			} else if (((e1.getY() - e2.getY()) > 60 || (e2.getY() - e1.getY()) > 60) && mOldX < windowWidth * 1.0 / 3.0 && Math.abs(distanceY) > Math.abs(distanceX)) {
				onBrightnessSlide((mOldY - y) / windowHeight);
			} else if ((mOldX - e2.getX()) > 60 || (e2.getX() - mOldX) > 60 && Math.abs(distanceX) > Math.abs(distanceY)) {
				onVideoSpeed(distanceX);
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	private void onVideoSpeed(float distanceX) {
		int mSpeed = 0;
		long mVideo_total_length = mVideoView.getDuration();
		String total_length = length2time(mVideo_total_length);
		long mVideo_current_length = mVideoView.getCurrentPosition();
		if (distanceX > 0) {
			--mSpeed;
			ivSpeed.setImageResource(R.drawable.duration_back);
			rlSpeed.setVisibility(View.VISIBLE);
			mVolumeBrightnessLayout.setVisibility(View.INVISIBLE);
		} else if (distanceX < 0) {
			++mSpeed;
			ivSpeed.setImageResource(R.drawable.duration_adance);
			rlSpeed.setVisibility(View.VISIBLE);
			mVolumeBrightnessLayout.setVisibility(View.INVISIBLE);
		}
		int i = mSpeed * 1000;
		long mVideo_start_length = mVideo_current_length + i;
		if (mVideo_start_length >= mVideo_total_length) {
			mVideo_start_length = mVideo_total_length;
		} else if (mVideo_start_length <= 0) {
			mVideo_start_length = 0;
		}
		String start_length = length2time(mVideo_start_length);
		tvSpeed.setText(start_length);
		setProgress = mVideo_start_length;
		mVideoView.seekTo(setProgress);
	}

	private String length2time(long length) {
		length /= 1000L;
		long minute = length / 60L;
		long hour = minute / 60L;
		long second = length % 60L;
		minute %= 60L;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			rlSpeed.setVisibility(View.INVISIBLE);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	private void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;

			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			rlSpeed.setVisibility(View.INVISIBLE);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	@Override
	public void onBackPressed() {
		if (isFullScreen) {
			mClickPort = false;
			fullScreenToNormal();
			return;
		}
		finish();
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.iv_more:
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			break;
		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void initData() {
		if (!StrUtils.strIsNull(videoDetailId)) {
			if (StrUtils.strIsNull(userid)) {
				btn_collect.setVisibility(View.INVISIBLE);
				btn_remark.setVisibility(View.GONE);
				edit_remark.setVisibility(View.GONE);
			}
			//视频集
			VideoSeriesAsynTask seriesAsynTask = new VideoSeriesAsynTask(handler);
			Object[] seriesParams = new String[]{videoDetailId};
			seriesAsynTask.execute(seriesParams);
			refreshData();
		}
		//oss
		OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(Consts.ACCESS_KEY_ID, Consts.ACCESS_KEY_SECRET);
		
		ClientConfiguration conf = new ClientConfiguration();
		conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
		conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
		conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
		conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
		OSSLog.enableLog();
		oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
	}
	
	/**
	 * 初始化界面数据
	 */
	private void refreshData() {
		//视频详情相关信息
		VideoDetialInfoAsynTask detailInfoAsynTask = new VideoDetialInfoAsynTask(handler);
		Object[] detailInfoParams = new Object[]{userid, videoDetailId};
		detailInfoAsynTask.execute(detailInfoParams);
		//评论列表
		VideoMessagesAsynTask messageListAsynTask = new VideoMessagesAsynTask(handler);
		Object[] messageParams = new Object[]{videoDetailId};
		messageListAsynTask.execute(messageParams);
	}
	
	/**
	 * 控件点击事件处理
	 * @return
	 */
	public OnClickListener onBtnClick() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.btn_price:
					if (!isFree) {
						Intent intent = new Intent(VideoDetailActivity.this,
								OnlinePayActivity.class);
						intent.putExtra("price", price);
						startActivity(intent);
					}
					break;
				case R.id.btn_collect:
					if (isCollect) {
						if (!StrUtils.strIsNull(collectId)) {
							DeleteCollectAsynTask delCollectAsynTask = new DeleteCollectAsynTask(handler);
							List<String> idList = new ArrayList<String>();
							idList.add(collectId);
							Object[] params = new Object[]{idList};
							delCollectAsynTask.execute(params);
						}
					} else {
						if (!StrUtils.strIsNull(userid)) {
							AddVideoCollectAsynTask addCollectAsynTask = new AddVideoCollectAsynTask(handler);
							Object[] params = new Object[]{userid, videoDetailId};
							addCollectAsynTask.execute(params);
						}
					}
					break;
				case R.id.btn_download:
//					new OSSUtil(oss, testBucket, downloadObject).asyncGetObjectRange(0,Range.INFINITE);
	            	new OSSUtil(oss, testBucket, downloadObject,dhandler).getMetaData();
					break;
				case R.id.btn_share:
					Intent intent=new Intent(Intent.ACTION_SEND);  
					intent.setType("text/plain");  
					intent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
					intent.putExtra(Intent.EXTRA_TEXT, video_path);  
					startActivity(Intent.createChooser(intent, getTitle()));
					break;
				case R.id.btn_remark:
					String textRemark = edit_remark.getText().toString();
					if (!StrUtils.strIsNull(textRemark)) {
						AddMessageAsynTask messageAsynTask = new AddMessageAsynTask(handler);
						String type = "1";
						Object[] params = new Object[]{videoDetailId, userid, textRemark, type};
						messageAsynTask.execute(params);
					}
					break;					
				default:
					break;
				}
			}
		};
	}
	
	/**
	 * 初始化视频详情信息
	 * @param videoMap
	 */
	private void initVideoInfo(Map<String, Object> videoMap) {
		String isfree = (String) videoMap.get("isfree");
		String title = (String) videoMap.get("title");
		String description = (String) videoMap.get("description");
		if ("0".equals(isfree)) {
			isFree = true;
			btn_price.setVisibility(View.INVISIBLE);
		} else if ("1".equals(isfree)) {
			isFree = false;
			price = (Double) videoMap.get("price");
			if (price > 0) {
				btn_price.setVisibility(View.VISIBLE);
				btn_price.setText("￥" + price + " 立即购买");
			} else {
				btn_price.setVisibility(View.VISIBLE);
			}
		} else {
			btn_price.setVisibility(View.INVISIBLE);
		}
		tx_title.setText(title);
		tx_descripe.setText(description);
	}
	
	/**
	 * 刷新评论列表
	 * @param messageList
	 */
	private void initMessageList(List<Map<String, Object>> messageList) {
		RemarkAdapter listItemAdapter = new RemarkAdapter(VideoDetailActivity.this, messageList);
		list_remark.setAdapter(listItemAdapter);
	}
	
	/**
	 * 获取文件信息后进行下载操作
	 */
	private Handler dhandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String code = (String) msg.getData().getSerializable(Consts.RESULT_CODE);
			if (code.equals(Consts.SUCCESS)) {
				Map<String, Object> seriesDetialMap = seriesList.get(curPosition);
				//获取视频缩略图和名称
				String img = (String) seriesDetialMap.get("thumbnail");
				String videotitle = (String) seriesDetialMap.get("title");
				
				DownloadUtil download =new DownloadUtil(VideoDetailActivity.this, 
						(Long) msg.getData().getSerializable("filesize"), 
						testBucket, downloadObject);
				download.download(img,videotitle);
			};
		}
	};
	
	
	//请求的回调数据处理
	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			String code = (String) msg.getData().getSerializable(Consts.RESULT_CODE);
			String messageKey = (String) msg.getData().getSerializable("messageKey");
			if (Consts.SUCCESS.equals(code)) {
				if (VideoSeriesAsynTask.MESSAGE_KEY.equals(messageKey)) {
					seriesList = (List<Map<String, Object>>) msg.getData().getSerializable(messageKey);
					if (seriesList != null) {
						int size = seriesList.size();
						if (size > 0) {
							//如果集数小于等于一集则不显示集数信息
							if (size <= 1) {
								layout_chapter.setVisibility(View.GONE);
							} else {
								layout_chapter.setVisibility(View.VISIBLE);
								String chapterMessage = "共" + size + "集";
								tx_total_chapter.setText(chapterMessage);
								
								final ChapterAdapter listItemAdapter = new ChapterAdapter(VideoDetailActivity.this, seriesList);
								grid_chapter.setAdapter(listItemAdapter);
								//选集，同时改变显示信息
								grid_chapter.setOnItemClickListener(new OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
										curPosition = position;
										Map<String, Object> seriesDetialMap = seriesList.get(curPosition);
										videoDetailId = (String) seriesDetialMap.get("id");
										listItemAdapter.setSelection(position);
										listItemAdapter.notifyDataSetChanged();
										initVideoInfo(seriesDetialMap);
										refreshData();
									}
								});
								//获取当前的页面信息，仅在刚进入页面时起作用
								for (int i = 0; i < size; i++) {
									Map<String, Object> seriesDetialMap = seriesList.get(i);
									String detailId = (String) seriesDetialMap.get("id");
									if (videoDetailId.equals(detailId)) {
										initVideoInfo(seriesDetialMap);
										listItemAdapter.setSelection(i);
										curPosition = i;
										listItemAdapter.notifyDataSetChanged();
									}
								}
								
							//存储视频播放记录
							DBManager dbManager = DBManager.open(VideoDetailActivity.this);
							Map<String, Object> videomap = seriesList.get(curPosition);
							String videoaddress =(String)videomap.get("videoAddress");
							String[] temp =videoaddress.split("/");
							downloadObject =temp[temp.length-1];
							
							dbManager.addPlayHistory(videomap.get("videoid").toString(), videomap.get("title").toString(),
										 videomap.get("videoAddress").toString(), videomap.get("thumbnail").toString(),getSharedPreferences("xmedu", MODE_PRIVATE).getString("userid", ""));
							}
						}
					}
				} else if (VideoDetialInfoAsynTask.MESSAGE_KEY.equals(messageKey)) {//收藏以及商品
					Map<String, Object> videoInfoMap = (Map<String, Object>) msg.getData().getSerializable(messageKey);
					isCollect = (Boolean) videoInfoMap.get("isCollect");
					isBuy = (Boolean) videoInfoMap.get("isBuy");
					//判断当前视频是否已收藏，相应显示不同图标
					if (isCollect) {
						collectId = (String) videoInfoMap.get("collectId");
						btn_collect.setImageResource(R.drawable.ic_collect);
					} else {
						btn_collect.setImageResource(R.drawable.ic_uncollect);
					}
					//商品信息
					final List<Map<String, Object>> commodityList = (List<Map<String, Object>>) videoInfoMap.get("commodity");
					//若有商品则显示商品，没有则清空显示
					if (commodityList != null && commodityList.size() > 0) {
						SimpleAdapter commodityItemAdapter = new ItemAdapter(VideoDetailActivity.this, commodityList,
								R.layout.activity_commodity_item, new String[]{"thumbnail", "commodityName"}, 
								new int[]{R.id.image_commodity, R.id.tx_commodity_title});
						grid_commodity.setAdapter(commodityItemAdapter);
						//点击商品后跳转到相应的商品淘宝页面
						grid_commodity.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
								String commodityUrl = (String) commodityList.get(position).get("commodityUrl");
								if (!StrUtils.strIsNull(commodityUrl)) {
									Intent intent = new Intent(VideoDetailActivity.this,
											CommodityPageActivity.class);
									intent.putExtra("commodityUrl", commodityUrl);
									startActivity(intent);
								}
							}
						});
					} else {
						SimpleAdapter commodityItemAdapter = new ItemAdapter(VideoDetailActivity.this, new ArrayList<Map<String,Object>>(),
								R.layout.activity_commodity_item, new String[]{"thumbnail", "commodityName"}, 
								new int[]{R.id.image_commodity, R.id.tx_commodity_title});
						grid_commodity.setAdapter(commodityItemAdapter);
					}
				} else if (VideoMessagesAsynTask.MESSAGE_KEY.equals(messageKey)) {
					//评论显示
					messageList = (List<Map<String, Object>>) msg.getData().getSerializable(messageKey);
					if(messageList != null && messageList.size() > 0) {
						initMessageList(messageList);
					} else {
						initMessageList(new ArrayList<Map<String,Object>>());
					}
				} else if (AddMessageAsynTask.MESSAGE_KEY.equals(messageKey)) {
					//添加评论
					edit_remark.clearFocus();
					Map<String, Object> messageMap = (Map<String, Object>) msg.getData().getSerializable(messageKey);
					//将评论添加到评论列表中，并刷新显示
					if (messageMap != null && messageMap.size() > 0) {
						messageList.add(0, messageMap);
						initMessageList(messageList);
					}
				} else if (AddVideoCollectAsynTask.MESSAGE_KEY.equals(messageKey)) {
					//添加收藏
					collectId = msg.getData().getString(messageKey);
					btn_collect.setImageResource(R.drawable.ic_collect);
					isCollect = true;
				} else if (DeleteCollectAsynTask.MESSAGE_KEY.equals(messageKey)) {
					//取消收藏，收藏图标改变
					btn_collect.setImageResource(R.drawable.ic_uncollect);
					collectId = "";
					isCollect = false;
				}
			} else {
				if (VideoMessagesAsynTask.MESSAGE_KEY.equals(messageKey)) {
					//如果未获取到评论数据，清空评论显示
					initMessageList(new ArrayList<Map<String,Object>>());
				}
			}
		}
	};

}
