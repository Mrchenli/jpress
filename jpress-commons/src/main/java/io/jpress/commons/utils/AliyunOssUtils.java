/**
 * Copyright (c) 2016-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.commons.utils;

import io.jpress.JPressOptions;

import java.io.File;


public class AliyunOssUtils {

    private static final String KEY_ENDPOINT = "attachment_aliyunoss_endpoint";


    public static boolean aliYun(){
        return JPressOptions.get(KEY_ENDPOINT).endsWith(".aliyuncs.com");
    }

    /**
     * 同步本地文件到阿里云OSS
     *
     * @param path
     * @param file
     * @return
     */
    public static void upload(String path, File file) {
        if(aliYun()){
            AliyunOssRealUtils.upload(path, file);
        }else {
            QiniuOssUtils.upload(path, file);
        }
    }

    /**
     * 同步本地文件到阿里云OSS
     *
     * @param path
     * @param file
     * @return
     */
    public static boolean uploadsync(String path, File file) {
        if(aliYun()){
            return AliyunOssRealUtils.uploadsync(path, file);
        }else {
            return QiniuOssUtils.uploadsync(path, file);
        }
    }


    /**
     * 同步 阿里云OSS 到本地
     *
     * @param path
     * @param toFile
     * @return
     */
    public static boolean download(String path, File toFile) {
        if(aliYun()){
            return AliyunOssRealUtils.download(path, toFile);
        }else{
            return QiniuOssUtils.download(path, toFile);
        }

    }

    /**
     * 删除一个OSS中的文件
     * @param objectName
     */
    public static void delete(String objectName){
        if(aliYun()){
            AliyunOssRealUtils.delete(objectName);
        }else {
            QiniuOssUtils.delete(objectName);
        }
    }

}
