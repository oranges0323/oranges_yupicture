package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.yupi.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.Space;


 import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author chen zhi
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-10-27 16:50:49
*/
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);
    /**
     * 验证校验空间
     * @param space 空间实体对象
     * @param add
     */
    void validSpace(Space space,boolean add);

    /**
     * 获取空间视图对象
     * @param space 空间实体对象
     * @param request HTTP请求对象
     * @return 返回空间视图对象
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间视图对象分页
     * @param spacePage 空间分页对象
     * @param request HTTP请求对象
     * @return 返回空间视图对象分页
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询条件包装器
     * @param spaceQueryRequest 空间查询请求参数
     * @return 返回查询条件包装器
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
