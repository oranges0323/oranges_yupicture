package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        // 将输入源转换为MultipartFile对象
        MultipartFile multipartFile = (MultipartFile) inputSource;

        // 校验文件是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
//        校验文件大小  几M
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024*1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过3M");

        //校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许的后缀集合
        final List<String> ALLOW_LIST = Arrays.asList("jpeg","png","jpg","webp");
        ThrowUtils.throwIf(!ALLOW_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"文件类型错误");

    }
    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;

        return multipartFile.getOriginalFilename();
    }

    @Override
    /**
     * 处理文件的方法
     * @param inputSource 输入源，这里是一个MultipartFile对象
     * @param file 目标文件对象，表示要保存到的文件
     * @throws IOException 可能发生的IO异常
     */
    protected void processFile(Object inputSource, File file) throws IOException {
        // 将输入源强制转换为MultipartFile类型
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 将MultipartFile的内容传输到指定的文件中
        multipartFile.transferTo(file);

    }




}
