package com.yupi.yupicturebackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.DeleteRequest;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.user.*;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.LoginUserVO;
import com.yupi.yupicturebackend.model.vo.UserVO;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);


        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);

        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     *创建用户（仅管理员）
     * 逻辑简单，就不在service写了
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        //判断空
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        //转换user
        User user = new User();
        BeanUtils.copyProperties(userAddRequest,user);
        //默认密码
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(DEFAULT_PASSWORD);

        //插入
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(user.getId());
    }
    /**
     * 根据id获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User result = userService.getById(id);

        ThrowUtils.throwIf(result == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取脱敏类
     * 两个都可以
     */

//    @GetMapping("/get/vo")
//    public BaseResponse<UserVO> getUserVOById(long id){
//        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
//        User result = userService.getById(id);
//        ThrowUtils.throwIf(result == null, ErrorCode.PARAMS_ERROR);
//        //直接转换成vo
//        return ResultUtils.success(userService.getUserVO(result));
//    }
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * （仅管理员）
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <=0 ,ErrorCode.PARAMS_ERROR);

        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户（仅管理员）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <=0 ,ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);

        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }
    /**
     *分页查询（仅管理员）
    */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest == null,ErrorCode.PARAMS_ERROR);
        //页码和每页大小
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        Page<User> userPage = userService.page(new Page<>(current, pageSize),
            userService.getQueryWrapper(userQueryRequest));
        //创建一个新的 Page<UserVO> 对象，这个对象的页码、每页的大小和总记录数与 userPage 对象相同
        Page<UserVO> userVOPage = new Page<>(current,pageSize,userPage.getTotal());
        //获取 userPage 对象中的所有 User 对象，然后将它们转换为 UserVO 对象
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        //将转换后的 UserVO 对象列表设置为 userVOPage 对象的记录
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }


    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/my_info")
    public BaseResponse<UserVO> getCurrentUserInfo(HttpServletRequest request) {
        // 判断用户是否登录，得到登录用户的用户信息
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 获取用户详细信息
        BaseResponse<UserVO> userVOById = this.getUserVOById(loginUser.getId());
        ThrowUtils.throwIf(userVOById.getCode() != 0, ErrorCode.OPERATION_ERROR, "获取用户信息失败");

        // 返回用户信息
        return ResultUtils.success(userVOById.getData());
    }

    /**
     * 更新用户基本信息（不包含头像）
     */
    @PostMapping("/my_info")
    public BaseResponse<UserVO> updateUserInfo(@RequestBody UserUpdateInfoRequest userUpdateInfoRequest,
                                                HttpServletRequest request) {
        // 判断用户是否登录，得到登录用户的用户信息
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 更新用户信息
        boolean result = userService.updateMyInfo(userUpdateInfoRequest, null,request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新用户信息失败");

        // 获取更新后的用户信息
        BaseResponse<UserVO> userVOById = this.getUserVOById(loginUser.getId());
        ThrowUtils.throwIf(userVOById.getCode() != 0, ErrorCode.OPERATION_ERROR, "获取更新后用户信息失败");

        // 返回更新后的用户信息
        return ResultUtils.success(userVOById.getData());
    }


    /**
     * 更新用户头像
     */
    @PostMapping("/my_info/avatar")
    public BaseResponse<UserVO> updateUserAvatar(@RequestPart(value = "file") MultipartFile file,
                                                 HttpServletRequest request) {
        // 判断用户是否登录，得到登录用户的用户信息
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 创建空的用户信息请求，只更新头像
        UserUpdateInfoRequest userUpdateInfoRequest = new UserUpdateInfoRequest();

        // 更新用户信息（只更新头像）
        boolean result = userService.updateMyInfo(userUpdateInfoRequest, file, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新用户头像失败");

        // 获取更新后的用户信息
        BaseResponse<UserVO> userVOById = this.getUserVOById(loginUser.getId());
        ThrowUtils.throwIf(userVOById.getCode() != 0, ErrorCode.OPERATION_ERROR, "获取更新后用户信息失败");

        // 返回更新后的用户信息
        return ResultUtils.success(userVOById.getData());
    }
}
