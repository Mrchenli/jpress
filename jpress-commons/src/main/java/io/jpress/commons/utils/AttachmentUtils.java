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

import com.jfinal.log.Log;
import com.jfinal.upload.UploadFile;
import io.jboot.utils.FileUtil;
import io.jboot.utils.StrUtil;
import io.jpress.JPressConfig;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AttachmentUtils {

    private static final Log LOG = Log.getLog(AttachmentUtils.class);

    /**
     * @param uploadFile
     * @return new file relative path
     */
    public static String moveFile(UploadFile uploadFile) {
        if (uploadFile == null) {
            return null;
        }

        File file = uploadFile.getFile();
        if (!file.exists()) {
            return null;
        }

        File newfile = newAttachemnetFile(FileUtil.getSuffix(file.getName()));

        if (!newfile.getParentFile().exists()) {
            newfile.getParentFile().mkdirs();
        }

        try {
            //如果是jpg就进行一次压缩
            String suffix = FileUtil.getSuffix(uploadFile.getFileName());
            if ((suffix.equals(".jpg") || suffix.equals(".jpeg")) && Files.size(file.toPath()) > 300 * 1024) {
                Thumbnails.of(file).scale(1f).outputQuality(0.5f).toFile(newfile);
            } else {
                org.apache.commons.io.FileUtils.moveFile(file, newfile);
            }
            newfile.setReadable(true, false);
        } catch (IOException e) {
            LOG.error(e.toString(), e);
        }
        String attachmentRoot = JPressConfig.me.getAttachmentRootOrWebRoot();
        return FileUtil.removePrefix(newfile.getAbsolutePath(), attachmentRoot);
    }

    public static File newAttachemnetFile(String suffix) {

        String attachmentRoot = JPressConfig.me.getAttachmentRootOrWebRoot();

        String uuid = UUID.randomUUID().toString().replace("-", "");

        StringBuilder newFileName = new StringBuilder(attachmentRoot)
                .append(File.separator).append("attachment")
                .append(File.separator).append(new SimpleDateFormat("yyyyMMdd").format(new Date()))
                .append(File.separator).append(uuid)
                .append(suffix);

        return new File(newFileName.toString());
    }

    public static File file(String path) {
        String attachmentRoot = JPressConfig.me.getAttachmentRootOrWebRoot();
        return new File(attachmentRoot, path);
    }

    public static String getAttachmentPath(String path) {
        String attachmentRoot = JPressConfig.me.getAttachmentRootOrWebRoot();
        return path != null && path.startsWith(attachmentRoot)
                ? path.substring(attachmentRoot.length())
                : path;
    }

    static List<String> imageSuffix = new ArrayList<>();

    static {
        imageSuffix.add(".jpg");
        imageSuffix.add(".jpeg");
        imageSuffix.add(".png");
        imageSuffix.add(".bmp");
        imageSuffix.add(".gif");
        imageSuffix.add(".webp");
    }

    public static boolean isImage(String path) {
        String sufffix = FileUtil.getSuffix(path);
        if (StrUtil.isNotBlank(sufffix)) {
            return imageSuffix.contains(sufffix.toLowerCase());
        }
        return false;
    }

    static List<String> unSafeFilesSuffix = new ArrayList<>();

    static {
        unSafeFilesSuffix.add(".jsp");
        unSafeFilesSuffix.add(".jspx");
        unSafeFilesSuffix.add(".php");
        unSafeFilesSuffix.add(".html");
        unSafeFilesSuffix.add(".htm");
        unSafeFilesSuffix.add(".css");
        unSafeFilesSuffix.add(".js");
        unSafeFilesSuffix.add(".exe");
        unSafeFilesSuffix.add(".sh");
        unSafeFilesSuffix.add(".bat");
        unSafeFilesSuffix.add(".jar");
        unSafeFilesSuffix.add(".war");
    }

    public static boolean isUnSafe(File file) {
        String sufffix = FileUtil.getSuffix(file.getName());
        if (StrUtil.isNotBlank(sufffix)) {
            return unSafeFilesSuffix.contains(sufffix.toLowerCase());
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(FileUtil.getSuffix("xxx.jpg"));
    }

}
