package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.yupi.yupicturebackend.config.CosClientConfig;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.manager.CosManager;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    public UploadPictureResult uploadPicture(Object inputSource,String uploadPathPrefix) {
        //1.校验图片

        validPicture(inputSource);

        //2.图片上传地址
        //加区分，uuid，时间戳,后缀
        String uuid = RandomUtil.randomString(16);

        String originalFilename = getOriginFilename(inputSource);

        //拼接文件上传路径，不用原始文件名，增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, (FileUtil.getSuffix(originalFilename) == "")? "jpeg" : FileUtil.getSuffix(originalFilename));

        String uploadPath = String.format("/%s/%s", uploadPathPrefix,uploadFileName);

        //解析结果并返回
        File file = null;
        try {

            //3.创建临时文件，获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            //处理文件来源
            processFile(inputSource,file);
            //4.上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //5.获取图片信息对象 封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if(CollUtil.isNotEmpty(objectList)){
                //1.获取压缩后的文件信息
                CIObject compressedCiObject = objectList.get(0);
                //缩略图默认等于压缩图
                CIObject thumbnailCiObject = compressedCiObject;
                //有生成缩略图，才获取缩略图
                if(objectList.size() > 1){
                    thumbnailCiObject = objectList.get(1);
                }

                //封装压缩图的返回结果
                return buildResult(originalFilename,compressedCiObject,thumbnailCiObject,imageInfo);
            }
             
            return buildResult(imageInfo,uploadPath,originalFilename,file);

        } catch (Exception e) {
            log.error("图片上传到对象存储失败 = ", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);

        }
    }

    /**
     * 封装返回结果
     * @param originalFilename 原始文件名
     * @param compressedCiObject 压缩后的对象
     * @param thumbnailCiObject 缩略图对象
     * @param imageInfo 原图对象
     * @return
     */

    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject,CIObject thumbnailCiObject,ImageInfo imageInfo) {
        //封装返回结果
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth*1.0/picHeight,2).doubleValue();

        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //设置压缩后的原图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        //设置缩略图地址
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" +thumbnailCiObject.getKey());
        //返回可访问的地址
        return uploadPictureResult;
    }

    /**
 * 处理输入源生成本地临时文件的抽象方法
 */
    protected abstract void processFile(Object inputSource,File file) throws IOException;

/**
 * 获取原始文件名的方法
 * 这是一个抽象方法，需要由子类来实现具体的获取原始文件名的逻辑
 */
    protected abstract String getOriginFilename(Object inputSource);

/**
 * 校验输入源的抽象方法（本地文件或者url）
 */
    protected abstract void validPicture(Object inputSource);


    /**
     * 封装返回结果
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo,String uploadPath,String originalFilename,File file){
        //封装返回结果
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth*1.0/picHeight,2).doubleValue();

        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());

        //返回可访问的地址
        return uploadPictureResult;
    }

/**
 * 删除临时文件的方法
 * @param file 需要删除的文件对象
 */
    private static void deleteTempFile(File file) {
        if (file != null) {  // 检查文件对象是否为空
            boolean deleteResult = file.delete();  // 尝试删除文件，并将结果保存到deleteResult变量中
            //删除临时文件
            if (!deleteResult) {  // 如果删除失败，记录错误日志
                log.error("file delete error,filepath = {}", file.getAbsolutePath());
            }
        }
    }


}
