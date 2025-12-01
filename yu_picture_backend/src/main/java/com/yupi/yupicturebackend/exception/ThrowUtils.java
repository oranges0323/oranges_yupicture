package com.yupi.yupicturebackend.exception;

import org.apache.logging.log4j.message.Message;

/**
 * 异常处理工具类
 */
public class ThrowUtils {
/**
 * 静态工具方法：根据条件抛出运行时异常
 * @param condition 判断条件，如果为true则抛出异常
 * @param runtimeException 要抛出的运行时异常对象
 */
    public static void throwIf(boolean condition,RuntimeException runtimeException){
    // 如果条件为真，则抛出指定的运行时异常
        if(condition){
            throw runtimeException;
        }
    }
/**
 * 根据条件抛出业务异常
 * @param condition 判断条件，如果为true则抛出异常
 * @param errorCode 错误码，用于指定异常的具体类型
 * @throws BusinessException 当条件满足时抛出业务异常
 */
    public static void throwIf(boolean condition,ErrorCode errorCode){
        throwIf(condition,new BusinessException(errorCode));
    }

/**
 * 根据条件抛出业务异常
 * @param condition 判断条件，如果为true则抛出异常
 * @param errorCode 错误码，用于标识具体的错误类型
 * @param message 异常信息，用于描述具体的错误内容
 */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message){
    // 如果条件为true，则抛出业务异常
        throwIf(condition,new BusinessException(errorCode,message));
    }

}
