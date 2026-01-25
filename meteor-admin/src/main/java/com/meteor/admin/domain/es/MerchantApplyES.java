package com.meteor.admin.domain.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * 商家申请ES实体
 *
 * @author Programmer
 */
@Data
@Document(indexName = "merchant_apply")
public class MerchantApplyES {

    @Id
    @Field(type = FieldType.Long, name = "id")
    private Long id;

    @Field(type = FieldType.Long, name = "applyId")
    private Long applyId;

    @Field(type = FieldType.Long, name = "userId")
    private Long userId;

    @Field(type = FieldType.Text, name = "shopName", analyzer = "ik_max_word")
    private String shopName;

    @Field(type = FieldType.Text, name = "applyReason", analyzer = "ik_max_word")
    private String applyReason;

    @Field(type = FieldType.Integer, name = "status")
    private Integer status;

    @Field(type = FieldType.Text, name = "rejectReason", analyzer = "ik_max_word")
    private String rejectReason;

    @Field(type = FieldType.Long, name = "reviewedBy")
    private Long reviewedBy;

    @Field(type = FieldType.Date, name = "reviewedTime", format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedTime;

    @Field(type = FieldType.Date, name = "createTime", format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
