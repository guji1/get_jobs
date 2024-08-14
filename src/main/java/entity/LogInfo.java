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
 * @Description: 信息日志
 * @Author: jeecg-boot
 * @Date:   2024-08-10
 * @Version: V1.0
 */
@Data
@TableName("log_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class LogInfo implements Serializable {
    private static final long serialVersionUID = 1L;

	/**Id*/
	@TableId(type = IdType.ASSIGN_ID)
    private Integer id;
	/**主档Id*/

    private Integer accountId;
	/**来源*/

    private String source;

    private String jobTag;

    private String city;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createDate;
	/**结束时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endDate;
	/**耗时*/


    private String spendDate;
	/**总聊天数*/
    private String totalChat;
}
