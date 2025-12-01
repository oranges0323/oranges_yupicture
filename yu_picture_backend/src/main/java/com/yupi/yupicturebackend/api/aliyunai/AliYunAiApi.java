package com.yupi.yupicturebackend.api.aliyunai;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResource;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {
    //读取apiKey 就是配置文件里的那个
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    //创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = " https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    //查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = " https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    //创建任务
    /**
     * 创建AI扩图任务 - 主要业务方法
     *
     * @param createOutPaintingTaskRequest 包含扩图任务所有必要参数的请求对象
     * @return CreateOutPaintingTaskResponse 阿里云返回的任务创建响应
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if(createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"扩图参数为空");
        }

        //发送请求（建议用ai写）

//        curl --location --request POST 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting' \
//        --header "Authorization: Bearer $DASHSCOPE_API_KEY" \
//        --header 'X-DashScope-Async: enable' \
//        --header 'Content-Type: application/json' \
//        --data '{
//        "model": "image-out-painting",
//                "input": {
//            "image_url": "http://xxx/image.jpg"
//        },
//        "parameters":{
//            "angle": 45,
//                    "x_scale":1.5,
//                    "y_scale":1.5
//        }
//    }'
        //使用hutool的HttpRequest;
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                //必须开启异步处理
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));

        //处理响应
         try(HttpResponse httpResource = httpRequest.execute()){
             if(!httpResource.isOk()) {
                 log.error("请求异常，响应码：{}",httpResource.body());
                 throw new BusinessException(ErrorCode.OPERATION_ERROR,"AI扩图失败");
             }

             CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResource.body(), CreateOutPaintingTaskResponse.class);
             //文档中写的code string
             //请求失败的错误码。请求成功时不会返回此参数，详情请参见错误信息。
             if(createOutPaintingTaskResponse.getCode() != null) {
                 String errorMessage = createOutPaintingTaskResponse.getMessage();
                 log.error("请求异常，响应码：{}",errorMessage);
                 throw new BusinessException(ErrorCode.OPERATION_ERROR,"AI扩图失败"+errorMessage);
             }
             return createOutPaintingTaskResponse;
         }

    }

    /**
     * 查询创建的任务结果
     *
     * @param taskId
     * @return
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 ID 不能为空");
        }
        // 处理响应
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
