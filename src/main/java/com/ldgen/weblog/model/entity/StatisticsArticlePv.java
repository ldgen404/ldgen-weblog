package com.ldgen.weblog.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("statistics_article_pv")
public class StatisticsArticlePv implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 被统计的日期
     */
    private LocalDate pvDate;

    /**
     * pv 访问量
     */
    private Long pvCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
