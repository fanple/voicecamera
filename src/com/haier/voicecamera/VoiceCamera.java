package com.haier.voicecamera;

/**
 * The main activity of VoiceCamera
 * @author zhang jun jie
 * @since 20130225
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format.Field;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.haier.voicecamera.R;
import com.iflytek.asr.AsrService.AsrInstance;

public class VoiceCamera extends Activity implements OnClickListener
{
	private static final String TAG = "CamTestActivity";
	Preview preview;
	Camera camera;
	String fileName;
	Activity act;
	Context ctx;
	
	ImageButton voiceClick;
	TextView textView;
	int numbersOfCamera;
	
	Aitalk4 aitalk=null;
	ivw20 ivw20=null;

	//缓存设置参数.
	//private SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		act = this;
		
		aitalk=new Aitalk4(this);
		ivw20=new ivw20(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);
		
		preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
		//preview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		((FrameLayout) findViewById(R.id.preview)).addView(preview);
		preview.setKeepScreenOn(true);
		
		voiceClick = (ImageButton) findViewById(R.id.VoiceClick);
		
		voiceClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				stopAndRestartASRIVW();
			}
		});
		
		textView=(TextView)findViewById(R.id.topStatus);
				
		//aitalk.beginInit();//命令词识别初始化
		
		//textView.setText("请说出\"讯飞语点\"唤醒控制.");
		
		ivw20.beginWaitToWakeUp();//语音唤醒初始化
		
		//numbersOfCamera=Camera.getNumberOfCameras();
		//utility.copyOneResToSdcard(this);
		//utility.copyResToSdcard(this);
		utility.copyRes2SDbyNewThread(this);
	}
	
	/**
	 * 强行重置停止唤醒与识别功能
	 */
	public void stopAndRestartASRIVW(){
		//aitalk.onVoiceButtonClick();
		setTopbarStatus("重置唤醒与识别!");
		aitalk.cancelASR();
		aitalk.destoryASR();
		ivw20.forceStopIVW20();
		ivw20.beginWaitToWakeUp();//语音唤醒初始化
	}
	
	@Override
	public void finish() {
		aitalk.destoryASR();
		super.finish();
	}
	
	public void wakeUpByIVW(){
		setTopbarStatus("己唤醒,请输指令,如拍照,放大,缩小,退出,白板...");
		aitalk.beginInit();//命令词识别初始化
		aitalk.startASRRecognize();
	}
	
	public void setTopbarStatus(CharSequence text){
		textView.setText(text);
	}
	
	public void doCameraActions(CameraActions ca){
		Camera.Parameters p=camera.getParameters();
		int currentZoomValue=p.getZoom();
		switch(ca){
			case takePic:
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				Toast.makeText(this, "己完成拍照", Toast.LENGTH_SHORT).show();
				break;
			case zoomIn:
				if(currentZoomValue>0){
					currentZoomValue--;
					p.setZoom(currentZoomValue);
					camera.setParameters(p);
					camera.startSmoothZoom(currentZoomValue);
					Toast.makeText(this, "相机-缩小", Toast.LENGTH_SHORT).show();					
				}
				else{
					Toast.makeText(this, "己达到最小级别", Toast.LENGTH_SHORT).show();
				}
				break;
			case zoomOut:
				int maxZoomValue=p.getMaxZoom();
				if(currentZoomValue<maxZoomValue){
					currentZoomValue++;
					p.setZoom(currentZoomValue);
					camera.setParameters(p);
					camera.startSmoothZoom(currentZoomValue);
					Toast.makeText(this, "相机-放大", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this, "己达到最大级别", Toast.LENGTH_SHORT).show();
				}
				break;
			case color_none:
				p.setColorEffect("none");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-正常", Toast.LENGTH_SHORT).show();
				break;				
			case color_mono:
				p.setColorEffect("mono");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-单色", Toast.LENGTH_SHORT).show();
				break;
			case color_sepia:
				p.setColorEffect("sepia");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-棕褐色", Toast.LENGTH_SHORT).show();
				break;
			case color_negative:
				p.setColorEffect("negative");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-负片", Toast.LENGTH_SHORT).show();
				break;
			case color_aqua:
				p.setColorEffect("aqua");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-水蓝色", Toast.LENGTH_SHORT).show();
				break;
			case color_blackboard:
				p.setColorEffect("blackboard");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-黑板", Toast.LENGTH_SHORT).show();
				break;
			case color_whiteboard:
				p.setColorEffect("whiteboard");
				camera.setParameters(p);
				Toast.makeText(this, "色彩-白板", Toast.LENGTH_SHORT).show();
				break;				
			case quitCamera:
				System.exit(0);
				break;				
			default:
				Toast.makeText(this, "无法识别你的命令", Toast.LENGTH_SHORT).show();
				break;
		}
		ivw20.beginWaitToWakeUp();//语音唤醒初始化
	}
		
	/** 安全获取Camera对象实例的方法*/ 
	public Camera getCameraInstance(){ 
	    Camera c = null; 
	    try { 
	        c = Camera.open(); // 试图获取Camera实例
	    } 
	    catch (Exception e){ 
	        // 摄像头不可用（正被占用或不存在）
	    } 
	    return c; // 不可用则返回null
	}

	@Override
	protected void onResume() {
		super.onResume();
		//preview.camera = Camera.open();
		camera = getCameraInstance();
		if(camera!=null)
		{
			camera.setDisplayOrientation(90);
			camera.startPreview();
			preview.setCamera(camera);

		}
	}

	@Override
	protected void onPause() {
		if(camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
		super.onPause();
	}

	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
		//showIsrDialog();
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// Log.d(TAG, "onPictureTaken - raw");
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			//roate
			BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 6;
	        options.inDither = false;
	        options.inPurgeable = true;
	        options.inInputShareable = true;
		    options.inTempStorage = new byte[32 * 1024];
	        options.inPreferredConfig = Bitmap.Config.RGB_565;
	        
			Bitmap cameraBitmap;  
			//将byte数组转换成Bitmap对象  
			cameraBitmap = BitmapFactory.decodeByteArray(data, 0,data.length,options);  
			//根据拍摄的方向旋转图像（纵向拍摄时需要将图像旋转90度）  
			if(getWindowManager().getDefaultDisplay().getOrientation() == 0)  
			{  
				Matrix matrix = new Matrix();  
				//matrix.setRotate(90);
				matrix.postRotate(90);
				cameraBitmap = Bitmap.createBitmap(cameraBitmap, 0, 0,cameraBitmap.getWidth(),cameraBitmap.getHeight(),matrix, true);  
			}  
			//roate end
			
			FileOutputStream outStream = null;
			try {
				// Write to SD Card
				fileName = utility.DCIMPath;
				if(utility.checkAndInitSDFolder(fileName)){
					fileName += String.format("/%d.jpg", System.currentTimeMillis());
					outStream = new FileOutputStream(fileName);
					//outStream.write(data);
					cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
					outStream.close();
					Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
				}
				resetCam();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
