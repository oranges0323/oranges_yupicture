package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.space.analyze.*;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.space.analyze.*;

import java.util.List;

public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

/**
 * 获取空间分类分析数据的方法
 *
 * @param spaceCategoryAnalyzeRequest 空间分类分析请求参数，包含分析所需的条件信息
 * @param loginUser 当前登录用户信息，用于权限验证等操作
 * @return   返回空间分类分析的结果数据
 */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     *获取空间图片标签分析
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间大小分析数据
     *
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

/**
 * 获取空间用户上传行为分析
 *
 * @param spaceUserAnalyzeRequest 空间用户分析请求参数，包含查询所需的各种条件
 * @param loginUser 当前登录用户信息，用于权限验证
 * @return 返回空间用户分析响应列表，包含分析结果数据
 */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

/**
 * 获取空间排名分析数据
 *
 * @param spaceRankAnalyzeRequest 空间排名分析请求参数，包含分析所需的各项条件
 * @param loginUser 当前登录用户信息，用于权限验证和数据过滤
 * @return 返回空间排名分析结果列表，包含各项分析指标数据
 */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
