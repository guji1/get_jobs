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
 * @Description: up_response
 * @Author: jeecg-boot
 * @Date:   2024-08-09
 * @Version: V1.0
 */
@Data
@TableName("up_response")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UpResponse implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Id*/
    @TableId(type = IdType.AUTO)
    private Integer id;
	/**公司名称*/
    private String campanyName;
	/**公司Id*/
    private Integer refuse;
	/**回复信息*/
    private String responseInfo;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createDate;
	/**来源*/
    private String source;
	/**主档Id*/
    private Integer accountId;
}
