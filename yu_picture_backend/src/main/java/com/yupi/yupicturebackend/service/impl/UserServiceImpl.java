package com.yupi.yupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yupi.yupicturebackend.config.CosClientConfig;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.manager.CosManager;
import com.yupi.yupicturebackend.manager.auth.StpKit;
import com.yupi.yupicturebackend.manager.upload.FilePictureUpload;
import com.yupi.yupicturebackend.model.dto.user.UserQueryRequest;
import com.yupi.yupicturebackend.model.dto.user.UserUpdateInfoRequest;
import com.yupi.yupicturebackend.model.dto.user.UserUpdateRequest;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.enums.UserRoleEnum;
import com.yupi.yupicturebackend.model.vo.LoginUserVO;
import com.yupi.yupicturebackend.model.vo.UserVO;
import com.yupi.yupicturebackend.service.UserService;
import com.yupi.yupicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author chen zhi
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-09-22 20:08:00
*/
@Slf4j
@Service
public class  UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验参数
            if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
            }
            if(userAccount.length() < 4){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
            }
            if(userPassword.length() < 8 || checkPassword.length() < 8){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
            }
            if(!userPassword.equals(checkPassword)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
            }
        //检查用户账号是否和数据库已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        //密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);

        //插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(userPassword);
        //10.30加的
        user.setUserAvatar("https://img.ixintu.com/download/jpg/20201121/e8a5f006ec63a963c04a2b3116997e89_512_512.jpg!ys");
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        }

        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号错误");
        }
        if(userPassword.length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码错误");
        }
        //加密，加密之后才能和数据库中的秘文一一对应
        String encryptPassword = getEncryptPassword(userPassword);
        //查询是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",userPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //不存在抛异常
        if(user == null)
        {
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在，或者密码错误");
        }
        //保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        //保存用户登录态到StpKit，便于空间鉴权使用，注意确保过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取修改登录用户信息
     * @param userUpdateInfoRequest
     * @param file
     * @param request
     * @return
     */
    @Override
    public boolean updateMyInfo(UserUpdateInfoRequest userUpdateInfoRequest,MultipartFile  file, HttpServletRequest request) {
        //判断用户是否登录，得到登录用户的用户信息
        User loginUser = this.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long userId = loginUser.getId();
        User user = this.getById(userId);

        //要修改的昵称，个人简介，修改的密码原始密码要修改的密码两次
        String userName = userUpdateInfoRequest.getUserName();
        String userProfile = userUpdateInfoRequest.getUserProfile();
        String userPassword = userUpdateInfoRequest.getUserPassword();
        String checkPassword = userUpdateInfoRequest.getCheckPassword();
        String newPassword = userUpdateInfoRequest.getNewPassword();

        //上传头像（从本地上传到腾讯云，再获取腾讯云返回的url）
        // 上传头像到腾讯云COS并获取URL
        String userAvatar = null;
        if (file != null) {
            try {
                // 将MultipartFile转换为临时File
                File tempFile = File.createTempFile("avatar", ".tmp");
                file.transferTo(tempFile);

                // 生成唯一的文件路径
                String originalFilename = file.getOriginalFilename();
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                String key = "avatar/" + userId + "/" + System.currentTimeMillis() + suffix;

                // 使用CosManager上传图片并处理
                PutObjectResult result = cosManager.putPictureObject(key, tempFile);

                // 构建访问URL
                // 从cosClientConfig获取bucket和region
                String bucket = cosClientConfig.getBucket();
                String region = cosClientConfig.getRegion();
                userAvatar = "https://" + bucket + ".cos." + region + ".myqcloud.com/" + key;

                // 删除临时文件
                tempFile.deleteOnExit(); // 使用deleteOnExit而不是delete，更安全
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "头像上传失败：" + e.getMessage());
            }
        }

        //校验密码（传了密码再校验，不然会影响其他的）
        if(!StrUtil.hasBlank(userPassword,checkPassword,newPassword)){
            String oldPassword = user.getUserPassword();
            if(!userPassword .equals(oldPassword)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"原密码错误");
            }
            if(newPassword.length() < 8 || checkPassword.length() < 8){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
            }
            if(!newPassword.equals(checkPassword)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
            }
            if(newPassword.equals(oldPassword)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"与原密码一致");
            }
        }

        //修改用户信息
        //插入数据到数据库中
        // 修改用户信息
        if (userAvatar != null) {
            user.setUserAvatar(userAvatar);
        }
        //这里不能使用！=null 因为""不是null的 所以会误判

        if (StrUtil.isNotBlank(userName)) {
            user.setUserName(userName);
        }
        if (StrUtil.isNotBlank(userProfile)) {
            user.setUserProfile(userProfile);
        }
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        //5.返回结果
        return true;
    }



    @Override
    public User getLoginUser(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //从数据库查询确认，也可以不查，但查了靠谱
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return currentUser;
    }

    public LoginUserVO getLoginUserVO(User user) {
        if(user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        //这个方法直接把一个对象的值全复制给新对象，新对象没有的属性不会获得
        BeanUtils.copyProperties(user,loginUserVO);

        return loginUserVO;
    }

    //  加盐方法
    public String getEncryptPassword(String userPassword){
        //加盐混淆密码
        final  String SALT = "yupi";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {

        if(user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        //这个方法直接把一个对象的值全复制给新对象，新对象没有的属性不会获得
        BeanUtils.copyProperties(user,userVO);

        return userVO;
    }

    /**
     *获取脱敏后的用户列表
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    //通过构造这个来生成sql查询
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest){
        if(userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(ObjUtil.isNotNull(id),"id",id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),"userRole",userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount),"userAccount",userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName),"userName",userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile),"userProfile",userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);

        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




