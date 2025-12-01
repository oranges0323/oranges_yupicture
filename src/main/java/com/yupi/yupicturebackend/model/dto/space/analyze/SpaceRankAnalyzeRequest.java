package com.yupi.yupicturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 空间使用排行分析请求（管理员）
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 排名前n
     */
    private Integer topN = 10;

}
