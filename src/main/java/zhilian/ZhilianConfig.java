package zhilian;

import job51.Job51Enum;
import lombok.Data;
import lombok.SneakyThrows;
import utils.JobUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class ZhilianConfig {
    /**
     * 搜索关键词列表
     */
    private List<String> keywords;

    /**
     * 城市编码
     */
    private List<String> cityCodes;

    /**
     * 薪资范围
     */
    private String salary;
    /**
     * 行业分类
     */
    private String industryDirectory;

    @SneakyThrows
    public static ZhilianConfig init() {
        ZhilianConfig config = JobUtils.getConfig(ZhilianConfig.class);
        // 转换城市编码
        config.setCityCodes(config.getCityCodes().stream().map(value -> ZhilianEnum.CityCode.forValue(value).getCode()).collect(Collectors.toList()));
        String salary = config.getSalary();
        config.setSalary(Objects.equals("不限", salary) ? "0" : salary);
        return config;
    }

}
