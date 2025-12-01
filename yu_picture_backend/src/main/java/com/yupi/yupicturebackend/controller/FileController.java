package com.yupi.yupicturebackend.controller;

import cn.hutool.core.io.IoUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
@Slf4j
@Controller
@RequestMapping("/file")
public class FileController {
    private final CosManager cosManager;

    public FileController(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 测试文件上传
     * @param multipartFile
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @RequestMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile)  {
        log.info("Starting file upload");

        //文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s",filename);
        File file = null;
        try {
            file = File.createTempFile(filepath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);

            return ResultUtils.success(filepath);

        } catch (Exception e) {
            log.error("file upload error,filepath = "+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            if(file != null){
                boolean delete = file.delete();
                //删除临时文件
                if(!delete){
                    log.error("file delete error,filepath = {}",filepath);
                }
            }
            log.info("Finished file upload");

        }
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @RequestMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObject cosObject = cosManager.getObject(filepath);
        COSObjectInputStream cosObjectInput = cosObject.getObjectContent();
        try {
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            log.error("file download error,filepath = "+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载失败");
        }finally {
            //关闭流
            if(cosObjectInput != null){
                cosObjectInput.close();
            }
        }


    }

}
