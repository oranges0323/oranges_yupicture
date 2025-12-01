package com.yupi.yupicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageUrlApi {
    public static String getImagePageUrl(String imageUrl){
        Map<String,Object> formData = new HashMap<>();
        formData.put("image",imageUrl);
        formData.put("tn","pc");
        formData.put("from","pc");
        formData.put("image_source","PC_UPLOAD_URL");
        //获取当前时间戳
        long uptime = System.currentTimeMillis();
        //请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        try {
            //发送请求
            HttpResponse httpResponse = HttpRequest.post(url)
                    .form(formData)
                    .header("acs-token", RandomUtil.randomString(1))
                    .timeout(5000)
                    .execute();
            if(httpResponse.getStatus() != HttpStatus.HTTP_OK){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }

            //解析响应
            String body = httpResponse.body();
            Map<String,Object> result = JSONUtil.toBean(body,Map.class);

            //处理响应结果
            if(result == null || !Integer.valueOf(0).equals(result.get("status"))){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }
            Map<String,Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            //如果url为空
            if(StrUtil.isBlank(searchResultUrl)){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"未返回有效结果地址");
            }

            return searchResultUrl;
        }catch (Exception e){
            log.error("调用百度图片搜索接口失败，imageUrl:{}",imageUrl,e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"搜索失败");
        }
    }
    public static void main(String[] args) {
        //测试以图搜图
//        String imageUrl = "https://www.codefather.cn/logo.png";
        String imageUrl = "https://ts1.tc.mm.bing.net/th/id/OIP-C.qIXD5FFzc1AvMZMUvqqPxQHaHa?rs=1&pid=ImgDetMain&o=7&rm=3";

        String searchResultUrl = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果URL:"+ searchResultUrl);
    }
}

