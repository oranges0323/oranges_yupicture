package com.yupi.yupicturebackend.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/")
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("OK");
    }

//    @PostMapping("/feedback")
//    public BaseResponse<String> feedback(@RequestBody JSONObject feedBack){
//        String jsonStr = JSONUtil.toJsonStr(feedBack);
//        log.info("收到反馈：{}",jsonStr);
//        return ResultUtils.success("OK");
//    }
}
