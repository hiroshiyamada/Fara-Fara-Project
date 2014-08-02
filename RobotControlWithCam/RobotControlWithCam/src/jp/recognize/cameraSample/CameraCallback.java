package jp.recognize.cameraSample;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraCallback extends SurfaceView implements Camera.PreviewCallback,SurfaceHolder.Callback{

	private static final String TAG = "CameraFaceDetect";	

	int faceCount = 0;
	int faceConfidence = 0;
	int faceCenterPointX;
	int faceCenterPointY;

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

		// 顔検出用のリスナーを登録する
		_camera.setFaceDetectionListener(new FaceDetectionListener() {

			// 顔が検出された時の処理を記述する
			@Override
			public void onFaceDetection(Face[] faces, Camera camera) {
				Log.d(TAG, "faces count: " + faces.length);
				faceCount = faces.length;
				for (Face face : faces) {
					// サポートされていなければ-1が常に返ってくる
					Log.d(TAG, "face id: " + face.id);

					// 顔検出の信頼度 1から100までの値が入っており、100が顔として信頼度が一番高い
					Log.d(TAG, "face score: " + face.score);
					faceConfidence = face.score;

					// 検出された顔の範囲
					Log.d(TAG, "face rect: " + face.rect.left + "," + face.rect.top + " - "
							+ face.rect.right + "," + face.rect.bottom);                    
					faceCenterPointX = face.rect.right - face.rect.left;
					faceCenterPointY = face.rect.bottom - face.rect.top;

					// 以下はサポートされていなければnullが入ってくる
					if (face.mouth != null) {
						Log.d(TAG, "face mouth: " + face.mouth.x + "," + face.mouth.y);
						Log.d(TAG, "face leftEye: " + face.leftEye.x + "," + face.leftEye.y);
						Log.d(TAG, "face rightEye: " + face.rightEye.x + "," + face.rightEye.y);
					}
				}
			}
		});

		// カメラに顔検出開始を指示する
		try {
			_camera.startFaceDetection();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException.");
		} catch (RuntimeException e) {
			Log.e(TAG, "the method fails or the face detection is already running.");
		}

	}

	public int getCenterPointX(){
		return faceCenterPointX;
	}
	
	public int getCenterPointY(){
		return faceCenterPointY;
	}
	
	public int faceCount(){
		return faceCount;
	}
	
	public int faceConfidence(){
		return faceConfidence;
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
