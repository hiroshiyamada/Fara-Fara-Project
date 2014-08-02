package jp.recognize.cameraSample;


import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.overdriverobotics.smartbotsdk.SmartBot;



public class ImageProcessingSample extends FaceRecognitionActivity{
	int mPreviewWidth=640,mPreviewHeight=480;		//カメラのプレビューサイズ
	float mScaleWidth, mScaleHeight;				//画面サイズとプレビューサイズの比率
	int mIntImage[]=null;							//ARGB8888の画像格納用
	ColorConvert mColorConvert=new ColorConvert();	//色変換用クラス
	ColorConvert.YUVtoRGB mYUV2RGB;
	int count=0;

	CameraCallback mCameraCallback=null;
	
	SmartBot mySmartBot;
	
	UsbManager manager;
	UsbSerialDriver driver;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);//元々の表示画面をコメントアウト
		//画面の向きを横で固定
		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
		mCameraCallback=new CameraCallback(this);
		setContentView(mCameraCallback, layoutParams);//CameraCallbackを描画画面に設定
		//独自プレビュー描画画面
		addContentView(new GraphicsView(this), layoutParams);

	}

	int mZoom=0;
	//独自プレビュー画面
	private class GraphicsView extends View{
		Paint paint;
		public GraphicsView(Context c){
			super(c);
			paint=new Paint();
		}
		@Override
		protected void onDraw(Canvas canvas){
			if(mIntImage==null)return;
			canvas.drawRGB(128, 128, 128);//SurfaceViewのカメラプレビューは使わないので塗りつぶしておく
			
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

	}
		
	
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
	boolean mLightFlag=false;
	//画面にタッチされた際の処理
	public boolean onTouchEvent(MotionEvent event) {
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
	}
	
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

	Bitmap mRecogBitmap=null;


	//画像処理
	public void recognition(byte yuv[]) {
		mYUV2RGB.getFast(yuv, mIntImage);
		if (mRecogBitmap == null)
			mRecogBitmap = Bitmap.createBitmap(mPreviewWidth,mPreviewHeight, Config.ARGB_8888);
		mRecogBitmap.setPixels(mIntImage,0, mPreviewWidth,
				0, 0, mRecogBitmap.getWidth(), mRecogBitmap.getHeight());

		
		count--;
		if(count==0)sendCommand("5");//stop
	}

}
