package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import com.yupi.yupicturebackend.model.dto.picture.*;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author chen zhi
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-09-28 10:36:23
*/
public interface PictureService extends IService<Picture> {

    /**
     * 图片上传
     * @param inputSource 图片输入源
     * @param pictureUploadRequest 图片上传请求参数
     * @param loginUser 登录用户信息
     * @return 返回图片视图对象
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取图片视图对象
     * @param picture 图片实体对象
     * @param request HTTP请求对象
     * @return 返回图片视图对象
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片视图对象分页
     * @param picturePage 图片分页对象
     * @param request HTTP请求对象
     * @return 返回图片视图对象分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询条件包装器
     * @param pictureQueryRequest 图片查询请求参数
     * @return 返回查询条件包装器
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 图片审核
     * @param pictureReviewRequest 图片审核请求参数
     * @param loginUser 登录用户信息
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 验证图片
     * @param picture 图片实体对象
     */
    void validPicture(Picture picture);

    /**
     *填充审核参数
     * @param picture 图片实体对象
     * @param loginUser 登录用户信息
     */
    void fillReviewParams(Picture picture, User loginUser);

/**
 * 批量上传图片的方法
 * @param  包含批量上传图片相关请求参数的对象
 * @param loginUser 执行上传操作的用户信息对象
 * @return 返回一个Integer对象，通常表示上传操作的结果状态或成功上传的数量
 */
    Integer uploadPictureByBatch (PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                  User loginUser);

    void clearPictureFile(Picture oldPicture);

    /**
     * 删除图片
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 校验空间图片的权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser,Picture picture);

    /**
     * 根据颜色搜索图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
