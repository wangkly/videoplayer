package io.vov.vitamio.widget;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import io.vov.vitamio.utils.Log;

public class TitleController extends FrameLayout {
	
	public Typeface fontFace;

	private static final int sDefaultTimeout = 6000;
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private Context mContext;
	private PopupWindow mWindow;
	private int mAnimStyle;
	private View mAnchor;
	private View mRoot;
	private String mTitle;
	private boolean mShowing;
	private boolean mFromXml = false;
	private ImageButton mBackButton;
	private TextView mFileName;
	public ImageButton getmBackButton() {
		return mBackButton;
	}

	private OnShownListener mShownListener;
	private OnHiddenListener mHiddenListener;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FADE_OUT:
				hide();
				break;
			default:
				break;
			}
		}
	};

	public TitleController(Context context, AttributeSet attrs) {
		super(context, attrs);
		fontFace = Typeface.createFromAsset(context.getAssets(), "xmedu_cg.ttf");
		mRoot = this;
		mFromXml = true;
		initController(context);
	}

	public TitleController(Context context, boolean fromXml, View container) {
		super(context);
		fontFace = Typeface.createFromAsset(context.getAssets(), "xmedu_cg.ttf");
		initController(context);
		mFromXml = fromXml;
		mRoot = makeControllerView();
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);// ����������MediaController�;���ķ������LayoutParams��
		p.gravity = Gravity.TOP;
		mRoot.setLayoutParams(p);
		((FrameLayout) container).addView(mRoot);
		initControllerView(mRoot);
	}

	public TitleController(Context context) {
		super(context);
		fontFace = Typeface.createFromAsset(context.getAssets(), "xmedu_cg.ttf");
		if (!mFromXml && initController(context))
			initFloatingWindow();
	}

	private boolean initController(Context context) {
		mContext = context;
		return true;
	}

	@Override
	public void onFinishInflate() {
		if (mRoot != null)
			initControllerView(mRoot);
	}

	private void initFloatingWindow() {
		mWindow = new PopupWindow(mContext);
		mWindow.setFocusable(false);
		mWindow.setBackgroundDrawable(null);
		mWindow.setOutsideTouchable(true);
		mAnimStyle = android.R.style.Animation;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setWindowLayoutType() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			try {
				mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", new Class[] { int.class });
				setWindowLayoutType.invoke(mWindow, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
			} catch (Exception e) {
				Log.e("setWindowLayoutType", e);
			}
		}
	}

	/**
	 * Set the view that acts as the anchor for the control view. This can for
	 * example be a VideoView, or your Activity's main view.
	 *
	 * @param view
	 *            The view to which to anchor the controller when it is visible.
	 */
	public void setAnchorView(View view) {
		mAnchor = view;
		if (!mFromXml) {
			removeAllViews();
			mRoot = makeControllerView();
			mWindow.setContentView(mRoot);
			mWindow.setWidth(LayoutParams.MATCH_PARENT);
			mWindow.setHeight(LayoutParams.WRAP_CONTENT);
		}
		initControllerView(mRoot);
	}

	/**
	 * Create the view that holds the widgets that control playback. Derived
	 * classes can override this to create their own.
	 *
	 * @return The controller view.
	 */
	protected View makeControllerView() {
		return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("titlecontroller", "layout", mContext.getPackageName()), this);
	}

	private void initControllerView(View v) {
		mBackButton = (ImageButton) v.findViewById(getResources().getIdentifier("titlecontroller_play_back", "id", mContext.getPackageName()));
//		if (mBackButton != null) {
//			mBackButton.requestFocus();
//			//mBackButton.setOnClickListener(mPauseListener);
//		}

		mFileName = (TextView) v.findViewById(getResources().getIdentifier("titlecontroller_file_name", "id", mContext.getPackageName()));
		if (mFileName != null) {
			mFileName.setText(mTitle);
			mFileName.setTypeface(fontFace);
		}
	}

	public void show() {
		show(sDefaultTimeout);
	}

	/**
	 * Set the content of the file_name TextView
	 *
	 * @param name
	 */
	public void setFileName(String name) {
		mTitle = name;
		if (mFileName != null)
			mFileName.setText(mTitle);
	}

	/**
	 * <p>
	 * Change the animation style resource for this controller.
	 * </p>
	 * <p/>
	 * <p>
	 * If the controller is showing, calling this method will take effect only
	 * the next time the controller is shown.
	 * </p>
	 *
	 * @param animationStyle
	 *            animation style to use when the controller appears and
	 *            disappears. Set to -1 for the default animation, 0 for no
	 *            animation, or a resource identifier for an explicit animation.
	 */
	public void setAnimationStyle(int animationStyle) {
		mAnimStyle = animationStyle;
	}

	/**
	 * Show the controller on screen. It will go away automatically after
	 * 'timeout' milliseconds of inactivity.
	 *
	 * @param timeout
	 *            The timeout in milliseconds. Use 0 to show the controller
	 *            until hide() is called.
	 */
	public void show(int timeout) {
		if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
//			if (mBackButton != null)
//				mBackButton.requestFocus();

			if (mFromXml) {
				setVisibility(View.VISIBLE);
			} else {
				int[] location = new int[2];

				mAnchor.getLocationOnScreen(location);
				Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

				mWindow.setAnimationStyle(mAnimStyle);
				setWindowLayoutType();
				mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
			}
			mShowing = true;
			if (mShownListener != null)
				mShownListener.onShown();
		}

		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
		}
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void hide() {
		if (mAnchor == null)
			return;

		if (mShowing) {
			try {
				mHandler.removeMessages(SHOW_PROGRESS);
				if (mFromXml)
					setVisibility(View.GONE);
				else
					mWindow.dismiss();
			} catch (IllegalArgumentException ex) {
				Log.d("MediaController already removed");
			}
			mShowing = false;
			if (mHiddenListener != null)
				mHiddenListener.onHidden();
		}
	}

	public void setOnShownListener(OnShownListener l) {
		mShownListener = l;
	}

	public void setOnHiddenListener(OnHiddenListener l) {
		mHiddenListener = l;
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		show(sDefaultTimeout);
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		show(sDefaultTimeout);
		return false;
	}

	public interface OnShownListener {
		public void onShown();
	}

	public interface OnHiddenListener {
		public void onHidden();
	}
	
}
