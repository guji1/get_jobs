package liepin;

import boss.BossEnum;
import lombok.Data;
import lombok.SneakyThrows;
import utils.JobUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class LiepinConfig {
    /**
     * 搜索关键词列表
     */
    private List<String> keywords;

    /**
     * 城市编码
     */
    private List<String>  cityCodes;

    /**
     * 薪资范围
     */
    private String salary;
    /**
     * 行业分类
     */
    private String industryDirectory;

    @SneakyThrows
    public static LiepinConfig init() {
        LiepinConfig config = JobUtils.getConfig(LiepinConfig.class);
        // 转换城市编码
        config.setCityCodes(config.getCityCodes().stream().map(value -> LiepinEnum.CityCode.forValue(value).getCode()).collect(Collectors.toList()));
        return config;
    }

}
