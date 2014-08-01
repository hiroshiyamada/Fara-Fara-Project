package jp.recognize.cameraSample;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraCallback extends SurfaceView implements Camera.PreviewCallback,SurfaceHolder.Callback{
	private ImageProcessingSample mClass=null;
	 Camera _camera;
	public CameraCallback(ImageProcessingSample c) {		//描画画面の準備
		super((Context)c);
		mClass=c;
		SurfaceHolder sHolder = getHolder();
		sHolder.addCallback(this);
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		this.setFocusable(true);
		this.requestFocus();
	}
	public void surfaceCreated(SurfaceHolder holder){  //描画画面の生成時にカメラを起動
		_camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
	}
	public void surfaceDestroyed(SurfaceHolder holder){//描画画面の破棄時にカメラを終了
		stopPreviewCallback();
		_camera.stopPreview();
		_camera.release();
		_camera = null;
	}
	public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
		Size previewSize=setPreviewSize(720,480);
		mClass.initImageData(previewSize.width, previewSize.height, width, height);
		try {_camera.setPreviewDisplay(holder);}
		catch (IOException e) {}
		_camera.startPreview();
		startPreviewCallback();
	}
	//プレビューサイズの設定
	public Size setPreviewSize(int width, int height) {
		Camera.Parameters parameters = _camera.getParameters();
		parameters.setPreviewSize(width , height );
		try{
			_camera.setParameters(parameters);
		}catch(Exception e){
			parameters.setPreviewSize(320,240);//例外が発生したらQVGAにしてみる
			_camera.setParameters(parameters);
		}
		return _camera.getParameters().getPreviewSize();
	}
	byte[] mImgaeData=null;
	public void onPreviewFrame(byte[] data, Camera camera) {
		
		stopPreviewCallback();
		mClass.recognition(data);
		startPreviewCallback();
		invalidate();//再描画
	}
	private void stopPreviewCallback(){
		_camera.setPreviewCallback(null);
	}
	private void startPreviewCallback(){
		_camera.setPreviewCallback(this);
	}


	//フォーカス調整
	int mFocusFlag=1;
	public void autoFocus(){
		if(mFocusFlag!=1)return;
		mFocusFlag=0;
	      _camera.autoFocus(new Camera.AutoFocusCallback(){
	         public void onAutoFocus(boolean success,Camera camera){
	            camera.autoFocus(null);//最後にコールバックを解放
	            mFocusFlag=2;
	         }
	      });
	   }

}
