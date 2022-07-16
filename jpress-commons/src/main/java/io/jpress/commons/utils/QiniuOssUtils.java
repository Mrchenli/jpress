package io.jpress.commons.utils;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.LogKit;
import com.jfinal.log.Log;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import io.jboot.utils.NamedThreadPools;
import io.jboot.utils.StrUtil;
import io.jpress.JPressOptions;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class QiniuOssUtils {

    static Log log = Log.getLog(QiniuOssUtils.class);


    private static final String KEY_ENABLE = "attachment_aliyunoss_enable";
    private static final String KEY_ENDPOINT = "attachment_aliyunoss_endpoint";
    private static final String KEY_ACCESSKEYID = "attachment_aliyunoss_accesskeyid";
    private static final String KEY_ACCESSKEYSECRET = "attachment_aliyunoss_accesskeysecret";
    private static final String KEY_BUCKETNAME = "attachment_aliyunoss_bucketname";
    private static final String KEY_OSS_DEL = "attachment_aliyunoss_del";


    private static ExecutorService fixedThreadPool = NamedThreadPools.newFixedThreadPool(3,"qiniuyun-oss-upload");

    /**
     * 同步本地文件到阿里云OSS
     *
     * @param path
     * @param file
     * @return
     */
    public static void upload(String path, File file) {
        fixedThreadPool.execute(() -> {
            uploadsync(path, file);
        });
    }

    /**
     * 同步本地文件到阿里云OSS
     *
     * @param path
     * @param file
     * @return
     */
    public static boolean uploadsync(String path, File file) {

        boolean enable = JPressOptions.getAsBool(KEY_ENABLE);

        if (!enable || StrUtil.isBlank(path)) {
            return false;
        }

        path = removeFileSeparator(path);
        path = path.replace('\\', '/');
        try {
            UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone0()));
            Auth auth = Auth.create(JPressOptions.get(KEY_ACCESSKEYID), JPressOptions.get(KEY_ACCESSKEYSECRET));
            String upToken = auth.uploadToken(JPressOptions.get(KEY_BUCKETNAME));
            Response response = uploadManager.put(new FileInputStream(file), path, upToken, null, null);
            DefaultPutRet putRet = JSONObject.parseObject(response.bodyString(), DefaultPutRet.class);
            LogKit.error("qiniuyun oss upload success! path:" + path + "\nfile:" + file +"\nret:"+ JSONObject.toJSONString(putRet));
            return true;
        } catch (Throwable e) {
            log.error("aliyun oss upload error!!!", e);
            return false;
        }
    }

    /**
     * 如果文件以 / 或者 \ 开头，去除 / 或 \ 符号
     */
    private static String removeFileSeparator(String path) {
        while (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1, path.length());
        }
        return path;
    }


    public static String getPrivateFile(String objectName) throws UnsupportedEncodingException {
        String encodeFileName = URLEncoder.encode(objectName, "utf-8").replace("+", "%20");
        String publicUrl = String.format("%s/%s", JPressOptions.get(KEY_ENDPOINT), encodeFileName);
        Auth auth = Auth.create(JPressOptions.get(KEY_ACCESSKEYID), JPressOptions.get(KEY_ACCESSKEYSECRET));
        long expireInSeconds = 3600;
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        return finalUrl;
    }

    public static boolean download(String objectName, File toFile) {
        boolean enable = JPressOptions.getAsBool(KEY_ENABLE);
        if (!enable || StrUtil.isBlank(objectName)) {
            return false;
        }
        objectName = removeFileSeparator(objectName);
        OutputStream os = null;
        InputStream is = null;
        try {

            if (!toFile.getParentFile().exists()) {
                toFile.getParentFile().mkdirs();
            }

            if (!toFile.exists()) {
                toFile.createNewFile();
            }
            String filePath = getPrivateFile(objectName);
            URL url = new URL(filePath);
            URLConnection connection = url.openConnection();
            is = connection.getInputStream();
            os = Files.newOutputStream(toFile.toPath());
            byte[] tmp = new byte[1024];
            int length = -1;
            while ((length=is.read(tmp)) != -1){
                os.write(tmp, 0, length);
            }
            os.flush();
            return true;
        } catch (Throwable e) {
            log.error("qiniuyun oss download error!!!  path:" + objectName + "   toFile:" + toFile, e);
            if (toFile.exists()) {
                toFile.delete();
            }
            return false;
        } finally {
            IOUtils.closeQuietly(os, is);
        }
    }

    /**
     * 删除一个OSS中的文件
     * @param objectName
     */
    public static void delete(String objectName){
        boolean ossDelEnable = JPressOptions.getAsBool(KEY_OSS_DEL);
        if (ossDelEnable){
            try {
                Auth auth = Auth.create(JPressOptions.get(KEY_ACCESSKEYID), JPressOptions.get(KEY_ACCESSKEYSECRET));
                BucketManager bucketManager = new BucketManager(auth, new Configuration(Zone.zone0()));
                Response delete = bucketManager.delete(JPressOptions.get(KEY_BUCKETNAME), objectName);
                LogKit.error("qiniuyun oss delete resp:"+ delete.bodyString());
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }



    public static void main(String[] args) throws IOException {
        String endPoint = "https://oss.touchfishfamily.com";
        String key = "VWu9oN5ZQy67lLMO0U-R7ps8rXGNLAZDC9vD9_fg";
        String secret = "ycXfKwB1IjQ6s2N7lM6Xz-2VeHIvDEnwPX1mPFZC";
        String bucket = "cms-tf";
//        Auth auth = Auth.create(key, secret);
//        String upToken = auth.uploadToken(bucket);
//        JSONObject object = new JSONObject();
//        object.put("token", upToken);
//        object.put("hosts", endPoint);
//        System.out.println(object.toJSONString());
//
//        String encodeFileName = URLEncoder.encode("wxgzh.jpg", "utf-8").replace("+", "%20");
//        String publicUrl = String.format("%s/%s", "http://oss.touchfishfamily.com", encodeFileName);
//        auth = Auth.create(key, secret);
//        long expireInSeconds = 10;
//        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
//        System.out.println(finalUrl);

        JPressOptions.set(KEY_ACCESSKEYID, key);
        JPressOptions.set(KEY_ACCESSKEYSECRET, secret);
        JPressOptions.set(KEY_BUCKETNAME, bucket);
        JPressOptions.set(KEY_ENDPOINT, endPoint);
        JPressOptions.set(KEY_ENABLE, "true");
        JPressOptions.set(KEY_OSS_DEL, "true");

//        String path = "D:\\softwares\\ideapj\\jpress\\jpress-commons\\src\\main\\java\\io\\jpress\\commons\\utils\\wxgzh2.jpg";
//        File file = new File(path);

//        String objName = "test/wxgzh.jpg";
       // uploadsync(objName, file);

        //String getPath = getPrivateFile(objName);
        //System.out.println(getPath);
        //delete(objName);
//        download("wxgzh.jpg", file);
    }


    public static class InitialUpLoad{
        public static void main(String[] args) throws InterruptedException {
            String endPoint = "https://oss.touchfishfamily.com";
            String key = "VWu9oN5ZQy67lLMO0U-R7ps8rXGNLAZDC9vD9_fg";
            String secret = "ycXfKwB1IjQ6s2N7lM6Xz-2VeHIvDEnwPX1mPFZC";
            String bucket = "cms-tf";
            JPressOptions.set(KEY_ACCESSKEYID, key);
            JPressOptions.set(KEY_ACCESSKEYSECRET, secret);
            JPressOptions.set(KEY_BUCKETNAME, bucket);
            JPressOptions.set(KEY_ENDPOINT, endPoint);
            JPressOptions.set(KEY_ENABLE, "true");
            JPressOptions.set(KEY_OSS_DEL, "true");

            String root = "D:\\softwares\\ideapj\\jpress\\starter\\target\\starter-4.0\\starter-4.0\\webapp\\";
            iterUpload(new File(root), root);
        }

        private static void iterUpload(File file, String rootPath) throws InterruptedException {
            if(file.isFile()){
                String filePath = file.getPath();
                filePath = filePath.replace(rootPath, "");
                filePath = filePath.replace("\\","/");
                uploadsync(filePath, file);
                TimeUnit.MILLISECONDS.sleep(200);
            }else{
                File[] dirFiles = file.listFiles();
                for (File n: dirFiles){
                    iterUpload(n, rootPath);
                }
            }
        }
    }


}
