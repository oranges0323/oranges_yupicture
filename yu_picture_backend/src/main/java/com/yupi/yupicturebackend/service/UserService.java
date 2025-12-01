package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupicturebackend.model.dto.user.UserQueryRequest;
import com.yupi.yupicturebackend.model.dto.user.UserUpdateInfoRequest;
import com.yupi.yupicturebackend.model.dto.user.UserUpdateRequest;
import com.yupi.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.vo.LoginUserVO;
import com.yupi.yupicturebackend.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author chen zhi
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-09-22 20:08:00
*/
public interface UserService extends IService<User> {


/**
 * 用户注册方法
 */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    //这个返回值是为了返回前端的对象
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    boolean updateMyInfo(UserUpdateInfoRequest userUpdateInfoRequest, MultipartFile file, HttpServletRequest request);

    /**
 * 根据HTTP请求获取登录用户信息
 * @param request HTTP请求对象，包含客户端请求信息
 * @return User 登录用户对象，如果用户未登录则可能返回null
 */
    User getLoginUser(HttpServletRequest request);

    /**
     * 转换user为LoginUserVO
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

/**
 * 加密用户密码的方法
 */
    String getEncryptPassword(String userPassword);

    /**
     * 用户注销,登录态注销
     */
    boolean   userLogout(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    //通过构造这个来生成sql查询
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User user);
}