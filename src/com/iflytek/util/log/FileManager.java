package com.iflytek.util.log;
  
  

import android.content.Context;
import android.os.StatFs; 

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.iflytek.util.log.Logging;

public class FileManager {
	private static final String TAG = "ViaFly_FileManager";
	
	

    /**
     * 拷贝asset资源到目的路径
     * @param descPath
     * @param assetPath
     * @return
     */
    public static boolean copyAssetFile(Context context,String descPath, String assetPath) {
        Logging.d(TAG, "desc file = " + descPath + ", asset file = " + assetPath);
        
        boolean ret = true;
        InputStream in = null;
        FileOutputStream fos = null;
        
        try  {
            // 读取程序包中资源
            in = context.getAssets().open(assetPath);
            int len = in.available();
            byte[] buffer = new byte[len];
            in.read(buffer);
            // 转存程序包资源
            fos = new FileOutputStream(descPath);
            fos.write(buffer, 0, len);
        } catch (IOException e) {
            ret = false;
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e1) {
                ret = false;
                e1.printStackTrace();
            }
        }
        
        return ret;
    }
	/**
	 * 文件压缩
	 * @param sourceDir 被压缩文件路径
	 * @param desZipFile 压缩后文件存放路径及文件名 压缩成功,返回true 压缩失败,返回false
	 * @return
	 */
    public static boolean zip(String sourceDir, String desZipFile) {
        OutputStream outStream = null;
        BufferedOutputStream bufOutStream = null;
        ZipOutputStream zipOutStream = null;
        boolean isSucess = false;

        try {
            File desPath = new File(desZipFile);
            if (!desPath.getParentFile().exists()) {
                desPath.getParentFile().mkdirs();
            }

            outStream = new FileOutputStream(desZipFile);
            bufOutStream = new BufferedOutputStream(outStream);
            zipOutStream = new ZipOutputStream(bufOutStream);
            File sourceFile = new File(sourceDir);

            // 判断源文件路径是否存在
            if (!sourceFile.exists()) {//不存在
                //关闭数据流
                try {// 关闭数据流
                    if (outStream != null) {
                        outStream.close();
                    }
                    if (bufOutStream != null) {
                        bufOutStream.close();
                    }
                    if (zipOutStream != null) {
                        zipOutStream.closeEntry();
                        zipOutStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            } else {//存在
                if (sourceFile.isFile()) {
                    isSucess = zipFile(sourceFile, "", zipOutStream);
                    if (!isSucess) {
                        try {// 关闭数据流
                            if (outStream != null) {
                                outStream.close();
                            }
                            if (bufOutStream != null) {
                                bufOutStream.close();
                            }
                            if (zipOutStream != null) {
                                zipOutStream.closeEntry();
                                zipOutStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                } else {
                    // 递归调用zipFile压缩函数
                    File[] files = sourceFile.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        isSucess = zipFile(files[i], "", zipOutStream);
                        if (!isSucess) {
                            //System.out.println("ERROR");
                            try {// 关闭数据流
                                if (outStream != null) {
                                    outStream.close();
                                }
                                if (bufOutStream != null) {
                                    bufOutStream.close();
                                }
                                if (zipOutStream != null) {
                                    zipOutStream.closeEntry();
                                    zipOutStream.close();
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                return false;
                            }
                            return false;
                        }
                    }
                }

                if (zipOutStream != null) {
                    zipOutStream.closeEntry();
                    zipOutStream.close();
                }
            }
        } catch (Exception e) {
            try {// 关闭数据流
                if (outStream != null) {
                    outStream.close();
                }
                if (bufOutStream != null) {
                    bufOutStream.close();
                }
                if (zipOutStream != null) {
                    zipOutStream.closeEntry();
                    zipOutStream.close();
                }
            } catch (IOException e1) {
                e.printStackTrace();
            }

            return false;
        } finally {
            try {// 关闭数据流
                if (outStream != null) {
                    outStream.close();
                }
                if (bufOutStream != null) {
                    bufOutStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!isSucess) {// 未压缩成功，删除压缩文件
                File temFile = new File(desZipFile);
                if (temFile.exists()) {
                    temFile.delete();
                }
            }
        }
        
        return true;
    }

    /**
     * 文件压缩函数
     * @param sourceFile 源文件
     * @param curPath zip输出控制流
     * @param zipOutStream 文件当前路径(相对路径) 压缩成功，返回true 压缩失败,返回false
     * @return
     */
    private static boolean zipFile(File sourceFile, String curPath, ZipOutputStream zipOutStream) {
        InputStream inputStram = null;
        byte[] buffer = new byte[1024];
        int length = 0;
        ZipEntry zipEntry = null;
        boolean isSucess = false;
        
        try {
            if (!sourceFile.isDirectory()) {// 压缩文件

                inputStram = new FileInputStream(sourceFile);
                zipEntry = new ZipEntry(curPath + sourceFile.getName());
                zipOutStream.putNextEntry(zipEntry);

                while ((length = inputStram.read(buffer)) != -1) {
                    zipOutStream.write(buffer, 0, length);
                }
                zipOutStream.closeEntry();
            } else {// 压缩目录

                File[] files = sourceFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    // 递归压缩，更新curPaths,必须以"/"结束，表示目录
                    isSucess = zipFile(files[i], curPath + sourceFile.getName() + "/", zipOutStream);
                    if(!isSucess){
                        if (inputStram != null) {
                            try {
                                inputStram.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            //ex.printStackTrace();
            if (inputStram != null) {
                try {
                    inputStram.close();
                } catch (IOException ex1) {
                    ex.printStackTrace();
                }
            }
            return false;
        }
        
        return true;
    }

    /**
     * 解压函数
     * @param sourceFile 压缩包所在路径
     * @param destDir 解压目的路径 解压成功,返回true 解压失败,返回false
     * @return
     */
    public static boolean unZip(String sourceFile, String destDir) {
        OutputStream outStream = null;
        BufferedOutputStream bufOutStream = null;
        InputStream inputStram = null;
        destDir = destDir.endsWith(File.separator) ? destDir : (destDir + File.separator);
        byte b[] = new byte[1024];
        int length;
        ZipFile readZipFile = null;

        try {
            readZipFile = new ZipFile(new File(sourceFile));
            Enumeration enumeration = readZipFile.entries();
            ZipEntry zipEntry = null;

            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                File outPutFile = new File(destDir + zipEntry.getName());

                if (zipEntry.isDirectory()) {// 当前条目是目录
                    outPutFile.mkdirs();
                } else {// 当前条目是文件

                    if (!outPutFile.getParentFile().exists()) {// 父目录不存在，则创建
                        outPutFile.getParentFile().mkdirs();
                    }

                    outStream = new FileOutputStream(outPutFile);
                    bufOutStream = new BufferedOutputStream(outStream);
                    inputStram = readZipFile.getInputStream(zipEntry);

                    // 解压文件
                    while ((length = inputStram.read(b)) > 0) {
                        bufOutStream.write(b, 0, length);
                    }
                    bufOutStream.flush();

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (bufOutStream != null) {
                    bufOutStream.close();
                }
                if (inputStram != null) {
                    inputStram.close();
                }
                if (readZipFile != null) {
                    readZipFile.close();
                }
            } catch (IOException e1) {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (bufOutStream != null) {
                    bufOutStream.close();
                }
                if (inputStram != null) {
                    inputStram.close();
                }
                if (readZipFile != null) {
                    readZipFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return true;
    }

    /**
     * 查询File剩余空间
     * 
     * @return
     */
    public static long getFileAvailableSize(File file) {
        try {
            StatFs sf = new StatFs(file.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();

            return availCount * blockSize;
        } catch (Exception e) {
            Logging.e(TAG, "getFileAvailableSize error: " + e.toString());
        }
        return 0;
    }
    

    /**
     * 从文件读取文本
     * @param fileName
     * @return
     */
    public static String readString(String fileName) {
        String ret = null;
        try {
            File f = new File(fileName);
            FileInputStream byteOut = new FileInputStream(f);
            int len = byteOut.available();
            byte[] buffer = new byte[len];
            byteOut.read(buffer);
            ret = new String(buffer, "utf-8");
            byteOut.close();
        } catch (IOException e) {
            Logging.d(TAG, "load file failed. " + fileName);
        }
        
        return ret;
    }

    /**
     * 保存文本到文件
     * @param fileName
     * @param text
     * @param isWipe 是否擦除旧内容
     * @return
     */
    public static int writeString(String fileName, String text, boolean isWipe) {
        int file_len = 0;
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
            } else {
                if (isWipe) {
                    file.delete();
                    file.createNewFile();
                }
            }
            RandomAccessFile tmp_file = new RandomAccessFile(file, "rw");
            tmp_file.seek(tmp_file.length());
            tmp_file.write(text.getBytes("utf-8"));
            file_len = (int) tmp_file.length();
            tmp_file.close();
        } catch (IOException e) {
            Logging.d(TAG, "save file failed. " + fileName);
            return file_len;
        } 
        return file_len;
    }
    
 

    /**
     * 检查文件长度
     * 
     * @return
     */
    public static int getFileLenth(String filename) {
        int file_len = 0;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return 0;
            }
            RandomAccessFile tmp_file = new RandomAccessFile(file, "r");
            file_len = (int) tmp_file.length();
            tmp_file.close();
        } catch (IOException e) {
            Logging.d(TAG, "getLogFileLenth file failed. " + filename);
            return file_len;
        }
        return file_len;
    }
}
