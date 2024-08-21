package entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: up_job
 * @Author: jeecg-boot
 * @Date:   2024-08-09
 * @Version: V1.0
 */
@Data
@TableName("job_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)

public class JobInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**Id*/
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**公司名称*/
    private String companyName;
    /**岗位*/
    private String jobPosition;
    /**地址*/
    private String address  ;
    /**待遇*/
    private String treatment;
    /**工作年限*/
    private String workExperience;
    /**行业分类*/
    private String industryDirectory;
    /**学历*/
    private String education;
    /**城市*/
    private String city;
    /**岗位链接*/
    private String href;
    /**职位标签*/
    private String jobTag;

    /**HR*/
    private String hr;
    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createDate;
    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateDate;
    /**来源*/
    private String source;
    /**
     * 公司行业
     */
    private String companyIndustry;
    /**
     * 公司状况
     */
    private String companyStatus;
    /**
     * 公司人数
     */
    private String companyNum;
}
