package com.yupi.yupicturebackend.common;

import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应类，用于封装API返回的统一数据结构
 * @param <T> 泛型类型，表示返回数据的类型
 */
@Data  // 使用Lombok注解自动生成getter、setter等方法
public class BaseResponse<T> implements Serializable {
    private int code;        // 响应状态码，表示请求处理的状态
    private T data;          // 响应数据，泛型类型，可根据不同接口返回不同类型的数据
    private String message;  // 响应消息，对请求处理结果的描述信息

    /**
     * 全参构造方法，用于创建包含所有字段的BaseResponse实例
     * @param code 响应状态码
     * @param data 响应数据
     * @param message 响应消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 部分参数构造方法，用于创建不包含message的BaseResponse实例
     * @param code 响应状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this(code,data,"");  // 调用全参构造方法，message默认为空字符串
    }
    /**
     * 枚举参数构造方法，根据ErrorCode枚举创建BaseResponse实例
     * @param errorCode 错误码枚举，包含code和message信息
     */
    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage());  // 使用枚举中的code和message，data默认为null
    }
}
