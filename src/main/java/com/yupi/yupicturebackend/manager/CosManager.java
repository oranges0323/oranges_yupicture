package com.yupi.yupicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.yupi.yupicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * CosManager类 - 负责与腾讯云对象存储(COS)交互的组件
 * 该类提供了上传、获取文件以及上传并处理图片的功能
 */
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;  // 腾讯云COS客户端配置

    @Resource
    private COSClient cosClient;  // 腾讯云COS客户端对象

    // 将本地文件上传到 COS
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        return cosClient.putObject(putObjectRequest);
    }

    // 将本地文件上传到 COS
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(),key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传并解析图片的方法
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        //对图片处理(获取基本信息也是图片处理）
        PicOperations picOperations = new PicOperations();
        //1表示返回原图信息
        picOperations.setIsPicInfo(1);

        List<PicOperations.Rule> rules = new ArrayList<>();
        //1.图片压缩（转成为webp格式）
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket() );
        compressRule.setRule("imageMogr2/format/webp");
        //添加thumbnail参数设置最小宽高，至少为512*512 为了适应（ai扩图的最小大小要求）
//        compressRule.setRule("imageMogr2/thumbnail/!512×512r/min/format/webp");
        rules.add(compressRule);


        //构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     *删除对象
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }

}
