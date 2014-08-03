package jp.recognize.cameraSample;


import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint;
import android.hardware.Camera.Face;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.overdriverobotics.smartbotsdk.SmartBot;



public class ImageProcessingSample extends Activity{
	int mPreviewWidth=640,mPreviewHeight=480;		//カメラのプレビューサイズ
	float mScaleWidth, mScaleHeight;				//画面サイズとプレビューサイズの比率
	int mIntImage[]=null;							//ARGB8888の画像格納用
	ColorConvert mColorConvert=new ColorConvert();	//色変換用クラス
	ColorConvert.YUVtoRGB mYUV2RGB;
	int count=0;
	String RIGHT = "1";
	String LEFT = "2";
	String FRONT = "3";
	String BACK = "4";
	String STOP = "5";
	//private final static int TIMER_PERIOD = 100;
	private final static int ROTATION_PERIOD = 500;
	private final static int STRAIGHT_PERIOD = 5000;
	private Handler handler = new Handler();
	private int timeCount=0;
	CameraCallback mCameraCallback=null;

	SmartBot mySmartBot;

	UsbManager manager;
	UsbSerialDriver driver;

	public GraphicsView gView;

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//Android画面サイズ取得
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		// ディスプレイのインスタンス生成
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		//中心のx座標
		int middleX = size.x;
		//中心のy座標
		int middleY = size.y;
		//setContentView(R.layout.main);//元々の表示画面をコメントアウト
		//画面の向きを横で固定
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//全画面表示
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//スリープ無効
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//タイトルの非表示
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//レイアウト設定
		final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);

		mySmartBot = new SmartBot();
		// https://github.com/mik3y/usb-serial-for-android
		// Get UsbManager from Android.
		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		// Find the first available driver.
		driver = UsbSerialProber.acquire(manager);
		//カメラ映像の表示
		mCameraCallback=new CameraCallback(this,middleX,middleY);


		setContentView(mCameraCallback, layoutParams);//CameraCallbackを描画画面に設定
		//独自プレビュー描画画面
		gView = new GraphicsView(this);
		addContentView(gView, layoutParams);
		/*Timer timer = new Timer(false);
		//set the timer schedule
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {

					}
				});
			}
		}, 100, TIMER_PERIOD);*/
	}
	int mZoom=0;

	public void trashControl(){
		//Android画面サイズ取得
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		// ディスプレイのインスタンス生成
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		//中心のx座標
		int middleX = size.x;
		//中心のy座標
		int middleY = size.y;
		int diffX = 20;
		int diffY = 20;


		while(diffX > 10 || diffY > 10){

			//顔中心のx座標
			int cenwidth = mCameraCallback.getFaceCenterPointX();
			//顔中心のy座標
			int cenheight = mCameraCallback.getFaceCenterPointY();

			//x座標の差の絶対値
			diffX = middleX - cenwidth;
			//y座標の差の絶対値
			diffY = Math.abs(middleY - cenheight);
			//横方向に移動
			if(diffX > 0){
				moveTrashMotor(LEFT,ROTATION_PERIOD);
			}else{
				moveTrashMotor(LEFT,ROTATION_PERIOD);
			}
			//縦方向に移動
			if(diffY > 10){
				moveTrashMotor(FRONT,STRAIGHT_PERIOD);
			}
		}
	}


	public void textUpdate(){
		gView.invalidate();
	}

	//独自プレビュー画面
	private class GraphicsView extends View{
		Paint paint;
		public GraphicsView(Context c){
			super(c);
			paint=new Paint();
		}
		@Override
		protected void onDraw(Canvas canvas){
			//if(mIntImage==null)return;
			//canvas.drawRGB(128, 128, 128);//SurfaceViewのカメラプレビューは使わないので塗りつぶしておく
			//Android画面サイズ取得
			WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
			// ディスプレイのインスタンス生成
			Display disp = wm.getDefaultDisplay();
			Point size = new Point();
			disp.getSize(size);
			//中心のx座標
			int middleX = size.x;
			//中心のy座標
			int middleY = size.y;


			paint.setTextSize(40);
			paint.setColor(Color.RED);
			canvas.drawText("画面の中心:"+middleX+":"+middleY,70,100,paint);
			canvas.drawText("検出した顔の中心:"+mCameraCallback.getFaceCenterPointX()+":"+mCameraCallback.getFaceCenterPointY(),70,200,paint);
		}
	}
	/*

			if(mZoom==1){
				canvas.scale(mScaleWidth, mScaleHeight);//画面にあわせて拡大
				if(mRecogBitmap!=null)canvas.drawBitmap(mRecogBitmap,0, 0, paint);
			}
			else{
			canvas.scale(mScaleWidth/2, mScaleHeight/2);//画面にあわせて拡大
			if(mRecogBitmap!=null)canvas.drawBitmap(mRecogBitmap,mPreviewWidth/2, 0, paint);
				canvas.scale(1/(mScaleWidth/2), 1/(mScaleHeight/2));//画面にあわせて拡大
				paint.setTextSize(120);
				paint.setColor(Color.BLACK);
				canvas.drawText("左", 0, mScreenHeight/2+120, paint);
				canvas.drawText("進む", mScreenWidth/4, mScreenHeight/2+120, paint);
				canvas.drawText("右", mScreenWidth-mScreenWidth/4, mScreenHeight/2+120, paint);
				paint.setTextSize(64);
				canvas.drawText("停止", mScreenWidth-mScreenWidth/4, 64, paint);
				canvas.drawText("ﾗｲﾄ", 0, 64, paint);
				paint.setColor(Color.WHITE);									
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(2f);
				canvas.drawRect(0, 0, mScreenWidth/4, mScreenHeight, paint);
				canvas.drawRect(mScreenWidth-mScreenWidth/4, 0, mScreenWidth, mScreenHeight, paint);
				canvas.drawRect(0, mScreenHeight/2, mScreenWidth, mScreenHeight, paint);
			}


			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.FILL);
		}

	}*/


	public void sendCommand(String stCommand){
		if (driver != null) {
			try {
				driver.open();

				driver.setBaudRate(9600);
				/*
			    byte buffer[] = new byte[16];
			    int numBytesRead = driver.read(buffer, 1000);
				 */

				driver.write(stCommand.getBytes(), stCommand.length());

			} catch (Exception e) {
				// Deal with error.
			} finally {

			}
		}
	}

	//左に動かす.
	public void moveTrashMotor(String num, long time) {
		sendCommand(num);
		Log.d("test","moter");
		try {
			Thread.sleep(time);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(STOP);
	}
	boolean mLightFlag=false;
	//画面にタッチされた際の処理
	/*public boolean onTouchEvent(MotionEvent event) {
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	        break;
	    case MotionEvent.ACTION_UP:
	    	int x=(int)event.getX();
	    	int y=(int)event.getY();
	    	if(mScreenWidth/4 < x && x< mScreenWidth-mScreenWidth/4 && y>= mScreenHeight/2)mySmartBot.GoForwardDistance(30);
	    	else if(x<mScreenWidth/4 && y>= mScreenHeight/2)mySmartBot.GoLeftAngle(30);
	    	else if(mScreenWidth-mScreenWidth/4 < x && y>= mScreenHeight/2)mySmartBot.GoRightAngle(30);
	    	else if(x<mScreenWidth/4 && y< mScreenHeight/2){mLightFlag=!mLightFlag;mySmartBot.Frontlight(mLightFlag);}
	    	else if(mScreenWidth-mScreenWidth/4 < x && y< mScreenHeight/2)mySmartBot.Stop();

	    	if(mScreenWidth/4 < x && x< mScreenWidth-mScreenWidth/4 && y>= mScreenHeight/2){sendCommand("3");count=5;}//Front
	    	else if(x<mScreenWidth/4 && y>= mScreenHeight/2){sendCommand("2");count=3;}//left
	    	else if(mScreenWidth-mScreenWidth/4 < x && y>= mScreenHeight/2){sendCommand("1");count=3;}//right
	    	else if(x<mScreenWidth/4 && y< mScreenHeight/2){sendCommand("4");count=5;}//back
	    	else if(mScreenWidth-mScreenWidth/4 < x && y< mScreenHeight/2)sendCommand("5");//stop

	    	if(mScreenWidth/4 < x && x< mScreenWidth-mScreenWidth/4 &&y< mScreenHeight/2)mZoom=(mZoom+1)%2;
	    	//mCameraCallback.autoFocus();

	        break;
	    case MotionEvent.ACTION_MOVE:
	        break;
	    case MotionEvent.ACTION_CANCEL:
	        break;
	    }
	    return super.onTouchEvent(event);
	}*/

	int mScreenWidth=0;
	int mScreenHeight=0;
	//初期化用
	public void initImageData(int preview_width, int preview_height,int screen_width,int screen_height){
		Log.i("initImageData", "preview width:"+preview_width+" height:"+preview_height);
		mPreviewWidth=preview_width;
		mPreviewHeight=preview_height;
		mScreenWidth=screen_width;
		mScreenHeight=screen_height;
		mScaleWidth=(float)screen_width/mPreviewWidth;
		mScaleHeight=(float)screen_height/mPreviewHeight;
		mIntImage=new int[mPreviewWidth*mPreviewHeight];
		mYUV2RGB = mColorConvert.newYUVtoRGB(mPreviewWidth, mPreviewHeight, 2,3,3);
	}
	//

	//Bitmap mRecogBitmap=null;
	//画像処理
	/*
	public void recognition(byte yuv[]) {
		mYUV2RGB.getFast(yuv, mIntImage);
		if (mRecogBitmap == null)
			mRecogBitmap = Bitmap.createBitmap(mPreviewWidth,mPreviewHeight, Config.ARGB_8888);
		mRecogBitmap.setPixels(mIntImage,0, mPreviewWidth,
				0, 0, mRecogBitmap.getWidth(), mRecogBitmap.getHeight());


		count--;
		if(count==0)sendCommand("5");//stop
	}
	 */

}