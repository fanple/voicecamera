package com.haier.voicecamera;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.Format.Field;

import android.widget.Toast;

public class utility {
	
	public static String libFileDirPath = android.os.Environment  
            .getExternalStorageDirectory().getAbsolutePath()// 得到外部存储卡的数据库的路径名  
            + "/HaierVoiceCamera";// 我要存储的目录  
	
	public static String DCIMPath = android.os.Environment  
            .getExternalStorageDirectory().getAbsolutePath()// 得到外部存储卡的数据库的路径名  
            + "/DCIM/HaierVoiceCamera";// 我要存储的目录
	/**
	 * check the path folder is already exist in sdcard or not,if not create them. 
	 * @param patch
	 * @return Ture--already exist or create successfully ,False--create failure
	 */
	public static boolean checkAndInitSDFolder(String patch){
		File dir = new File(patch);// 目录路径  
        if (!dir.exists()) {// 如果不存在，则创建路径名  
            //System.out.println("要存储的目录不存在");  
            if (dir.mkdirs()) {// 创建该路径名，返回true则表示创建成功   
            	//Toast.makeText(vc, "已经创建文件存储目录", Toast.LENGTH_SHORT).show();
            	return true;
            } else {  
            	//Toast.makeText(vc, "创建目录失败,请检查SDCard", Toast.LENGTH_LONG).show();
            	return false;
            }  
        }
        return true;
	}
	
	/*
    private static String fileName = "grm.irf";// 要存储的文件名  
	public static void copyOneResToSdcard(VoiceCamera vc) {  
        String filePath = fileDirPath + "/" + fileName;// 文件路径  
        try {  
            File dir = new File(fileDirPath);// 目录路径  
            if (!dir.exists()) {// 如果不存在，则创建路径名  
                //System.out.println("要存储的目录不存在");  
                if (dir.mkdirs()) {// 创建该路径名，返回true则表示创建成功  
                    //System.out.println("已经创建文件存储目录");  
                } else {  
                    //System.out.println("创建目录失败");  
                }  
            }  
            // 目录存在，则将apk中raw中的需要的文档复制到该目录下  
            File file = new File(filePath);  
            if (!file.exists()) {// 文件不存在  
                //System.out.println("要打开的文件不存在");  
                InputStream ins = vc.getResources().openRawResource(  
                        R.raw.grm);// 通过raw得到数据资源  
                //System.out.println("开始读入");  
                FileOutputStream fos = new FileOutputStream(file);  
                //System.out.println("开始写出");  
                byte[] buffer = new byte[8192];  
                int count = 0;// 循环写出  
                while ((count = ins.read(buffer)) > 0) {  
                    fos.write(buffer, 0, count);  
                }  
                //System.out.println("已经创建该文件");  
                fos.close();// 关闭流  
                ins.close();  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    */
	
	/* 
	* 将 raw里的文件copy 到sd卡下   
	* */   
	public static void copyResToSdcard(VoiceCamera vc){//name为sd卡下制定的路径   
		File dir = new File(libFileDirPath);// 目录路径  
        if (!dir.exists()) {// 如果不存在，则创建路径名  
            //System.out.println("要存储的目录不存在");  
            if (dir.mkdirs()) {// 创建该路径名，返回true则表示创建成功  
                //System.out.println("已经创建文件存储目录");  
            } else {  
                //System.out.println("创建目录失败");  
            }  
        }  
		java.lang.reflect.Field[] raw = R.raw.class.getFields();   
		for (java.lang.reflect.Field r : raw) {   
			try {   
			    //System.out.println("R. raw." + r.getName());   
				int id=vc.getResources().getIdentifier(r.getName(), "raw", vc.getPackageName());   
				//if(!r.getName().equals("allapps")){   
				String path=libFileDirPath+"/"+r.getName()+".irf";   
				// 目录存在，则将apk中raw中的需要的文档复制到该目录下  
	            File file = new File(path);  
	            if (!file.exists()) {// 文件不存在  
					BufferedOutputStream bufEcrivain = new BufferedOutputStream((new FileOutputStream(new File(path))));   
					BufferedInputStream VideoReader = new BufferedInputStream(vc.getResources().openRawResource(id));   
					byte[] buff = new byte[5*1024];   
					int len;   
					while( (len = VideoReader.read(buff)) > 0 ){   
						bufEcrivain.write(buff,0,len);   
					}   
						bufEcrivain.flush();   
						bufEcrivain.close();   
						VideoReader.close();   
					}
				//}
			} catch (Exception e) {   
				e.printStackTrace();   
			}   
		}   
	}
	
	public static void copyRes2SDbyNewThread(final VoiceCamera vc){
		class copyres2sd implements Runnable{
			public void run(){
				if(!checkAndInitSDFolder(libFileDirPath)){
					Toast.makeText(vc, "创建目录失败,请检查SDCard", Toast.LENGTH_LONG).show();
					return;
				}
				java.lang.reflect.Field[] raw = R.raw.class.getFields();   
				for (java.lang.reflect.Field r : raw) {   
					try {   
					    //System.out.println("R. raw." + r.getName());   
						int id=vc.getResources().getIdentifier(r.getName(), "raw", vc.getPackageName());   
						//if(!r.getName().equals("allapps")){   
						String path=libFileDirPath+"/"+r.getName()+".irf";   
						// 目录存在，则将apk中raw中的需要的文档复制到该目录下  
			            File file = new File(path);  
			            if (!file.exists()) {// 文件不存在  
							BufferedOutputStream bufEcrivain = new BufferedOutputStream((new FileOutputStream(new File(path))));   
							BufferedInputStream VideoReader = new BufferedInputStream(vc.getResources().openRawResource(id));   
							byte[] buff = new byte[5*1024];   
							int len;   
							while( (len = VideoReader.read(buff)) > 0 ){   
								bufEcrivain.write(buff,0,len);   
							}   
								bufEcrivain.flush();   
								bufEcrivain.close();   
								VideoReader.close();   
							}
						//}
					} catch (Exception e) {   
						e.printStackTrace();   
					}   
				}   
			}
		}
		Thread copyres = (new Thread(new copyres2sd()));
		copyres.start();
	}

}
