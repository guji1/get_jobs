package liepin;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import zhilian.ZhilianEnum;

public class LiepinEnum {

    @Getter
    public enum CityCode {
        NULL("不限", "0"),
        ALL("全国", "410"),
        BEIJING("北京", "010"),
        SHANGHAI("上海", "020"),
        GUANGZHOU("广州", "050020"),
        SHENZHEN("深圳", "050090"),
        CHENGDU("成都", "280020"),
        HANGZHO("杭州", "070020"),
        TIANJIN("天津", "030"),
        XIAN("西安", "270020"),
        SUZHO("苏州", "060080"),
        WUHAN("武汉", "170020"),
        XIAMEN("厦门", "090040"),
        CHANGSHA("长沙", "180020"),
        ZHENGZHO("郑州", "150020"),
        CHONGQ("重庆", "040");

        private final String name;
        private final String code;

        CityCode(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @JsonCreator
        public static CityCode forValue(String value) {
            for (CityCode cityCode : CityCode.values()) {
                if (cityCode.name.equals(value)) {
                    return cityCode;
                }
            }
            return NULL;
        }


        @JsonCreator
        public static String forName(String value) {
            for (LiepinEnum.CityCode cityCode : LiepinEnum.CityCode.values()) {
                if (cityCode.code.equals(value)) {
                    return cityCode.name;
                }
            }
            return "";
        }

    }

}
