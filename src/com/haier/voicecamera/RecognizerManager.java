package com.haier.voicecamera;

import android.content.Context;

/**
 * recognize actions through XF engineer. 
 * @author zhangJunjie
 * @since 20130305
 */
public class RecognizerManager {
	
	public static CameraActions recognize(int commandID,int recognizerResultConfidence){
		CameraActions ca = null;
		
		if(recognizerResultConfidence<50){
			ca=CameraActions.noAction;
			return ca;
		}
		
		switch(commandID){
		case 100:  //拍照!id(100)  参考 camera.bnf
		case 101:  //smile!id(101)
		case 102:  //茄子!id(102)
		case 103:  //来一张!id(103)
		case 104:  //拍一张!id(104)
			ca=CameraActions.takePic;
			break;
		case 150:  //放大!id(150)
			ca=CameraActions.zoomOut;
			break;
		case 160:  //缩小!id(160) 
			ca=CameraActions.zoomIn;
			break;			
		case 200:  //正常色!id(200)
		case 201:  //正常色彩!id(201)
			ca=CameraActions.color_none;
			break;
		case 202:  //单色!id(202)
			ca=CameraActions.color_mono;
			break;	
		case 203:  //棕褐色!id(203)
			ca=CameraActions.color_sepia;
			break;	
		case 204:  //负片!id(204)
			ca=CameraActions.color_negative;
			break;	
		case 205:  //水蓝色!id(205)
			ca=CameraActions.color_aqua;
			break;	
		case 206:  //黑板!id(206)
			ca=CameraActions.color_blackboard;
			break;		
		case 207:  //白板!id(207)
			ca=CameraActions.color_whiteboard;
			break;				
		case 999:
			ca=CameraActions.quitCamera;
			break;
		default:
			ca=CameraActions.noAction;
			break;
		}
		
		return ca;
	}

}
