package com.yupi.yupicturebackend.manager;

import ch.qos.logback.classic.spi.STEUtil;
import cn.hutool.Hutool;
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
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.config.CosClientConfig;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 文件管理服务类
 * 提供文件上传、校验等功能
 * 已废弃，改用upload包里的模板方法优化
 */
@Slf4j
@Service
@Deprecated //表示废弃了
public class FileManager {
    /**
     * 腾讯云COS管理器
     * 用于处理对象存储相关操作
     */
    @Resource
    private CosManager cosManager;

    /**
     * 腾讯云COS客户端配置
     * 包含访问对象存储所需的各种配置信息
     */
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传图片方法
     * @param multipartFile 要上传的图片文件
     * @param uploadPathPrefix 上传路径前缀
     * @return UploadPictureResult 上传结果，包含图片URL、尺寸等信息
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        //加区分，uuid，时间戳,后缀
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        //拼接文件上传路径，不用原始文件名，增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix,uploadFileName);

        //解析结果并返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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

            //返回可访问的地址
            return uploadPictureResult;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败 = ", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);

        }
    }



    /**
     * 校验图片方法
     * @param multipartFile 要校验的图片文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
//        校验文件大小  几M
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024*1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2M");

        //校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许的后缀集合
        final List<String> ALLOW_LIST = Arrays.asList("jpeg","png","jpg","webp");
        ThrowUtils.throwIf(!ALLOW_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"文件类型错误");

    }
    /**
     * 删除临时文件方法
     * @param file 要删除的临时文件
     */
    private static void deleteTempFile(File file) {
        if (file != null) {
            boolean deleteResult = file.delete();
            //删除临时文件
            if (!deleteResult) {
                log.error("file delete error,filepath = {}", file.getAbsolutePath());
            }
        }
    }


//    // 新增方法
//    public UploadPictureResult uploadPictureByUrl(String fileUrl,String uploadPathPrefix) {
//        //校验图片url
//        //todo
//        validPicture(fileUrl);
//        //图片上传地址
//        //加区分，uuid，时间戳,后缀
//        String uuid = RandomUtil.randomString(16);
//        //todo
//        String originalFilename = FileUtil.mainName(fileUrl);
//        //拼接文件上传路径，不用原始文件名，增加安全性
//        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
//        String uploadPath = String.format("/%s/%s", uploadPathPrefix,uploadFileName);
//
//        //解析结果并返回
//        File file = null;
//        try {
//            file = File.createTempFile(uploadPath, null);
//            //下载文件
//            HttpUtil.downloadFile(fileUrl,file);
////            multipartFile.transferTo(file);
//            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
//            //获取图片信息对象
//            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
//            //封装返回结果
//            int picWidth = imageInfo.getWidth();
//            int picHeight = imageInfo.getHeight();
//            double picScale = NumberUtil.round(picWidth*1.0/picHeight,2).doubleValue();
//
//
//
//            UploadPictureResult uploadPictureResult = new UploadPictureResult();
//            uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+uploadPath);
//            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
//            uploadPictureResult.setPicSize(FileUtil.size(file));
//            uploadPictureResult.setPicWidth(picWidth);
//            uploadPictureResult.setPicHeight(picHeight);
//            uploadPictureResult.setPicScale(picScale);
//            uploadPictureResult.setPicFormat(imageInfo.getFormat());
//
//            //返回可访问的地址
//            return uploadPictureResult;
//
//        } catch (Exception e) {
//            log.error("图片上传到对象存储失败 = ", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            deleteTempFile(file);
//
//        }
//    }
//
//    /**
//     *根据url校验数据
//     * @param fileUrl
//     */
//    private void validPicture(String fileUrl){
//
//        //校验非空
//        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl),ErrorCode.PARAMS_ERROR,"文件地址为空");
//        //校验url格式
//        try {
//            new URL(fileUrl);
//        } catch (MalformedURLException e) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件地址不正确");
//        }
//        //校验url协议
//        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
//                ErrorCode.PARAMS_ERROR,"仅支持http或https协议的文件地址");
//
//        //发送head请求验证文件是否存在
//        HttpResponse httpResponse = null;
//        try {
//            HttpUtil.createRequest(Method.HEAD, fileUrl)
//                    .execute();
//            if(httpResponse.getStatus() != HttpStatus.HTTP_OK){
//                return;
//            }
//
//            //文件存在，文件类型校验
//            String contentType = httpResponse.header("Content-Type");
//            //不空再校验是否合法
//            if(StrUtil.isNotBlank(contentType)){
//                final List<String> ALLOW_LIST = Arrays.asList("image/jpeg","image/png","image/jpg","image/webp");
//                ThrowUtils.throwIf(!ALLOW_LIST.contains(fileUrl),
//                        ErrorCode.PARAMS_ERROR,"文件类型错误");
//
//            }
//
//            //文件存在，文件大小校验
//            String contentLengthStr = httpResponse.header("Content-Length");
//            if(StrUtil.isNotBlank(contentLengthStr)){
//                try {
//                    long contentLength = Long.parseLong(contentLengthStr);
//                    final long ONE_M = 1024*1024;
//                    ThrowUtils.throwIf(contentLength > 2*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
//
//                }catch (NumberFormatException e){
//                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小格式异常");
//                }
//            }
//
//
//        }finally {
//            //释放资源
//            if(httpResponse!=null){
//                httpResponse.close();
//            }
//        }
//    }
}
