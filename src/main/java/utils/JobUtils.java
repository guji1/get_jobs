package utils;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import entity.JobInfo;
import lombok.SneakyThrows;
import mapper.*;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.json.JSONObject;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static utils.Constant.UNLIMITED_CODE;

public class JobUtils {

    public static void upsetJobInfo(String companyName, String jobPosition, String jobAddress,
                                    String companyNum, String companyStatus, String companyIndustry, String jobTag, String jobHref,
                                    String jobSalary, String jobEducation, String jobExperience, String jobHr,
                                    String jobSource, String city, String IndustryDirectory) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setWorkExperience(jobExperience);
        jobInfo.setEducation(jobEducation);
        jobInfo.setAddress(jobAddress);
        jobInfo.setCompanyName(companyName);
        jobInfo.setJobPosition(jobPosition);
        jobInfo.setTreatment(jobSalary);
        jobInfo.setUpdateDate(new Date());
        jobInfo.setSource(jobSource);
        jobInfo.setHr(jobHr);
        jobInfo.setJobTag(jobTag);
        jobInfo.setHref(jobHref);
        jobInfo.setCompanyIndustry(companyIndustry);
        jobInfo.setCompanyNum(companyNum);
        jobInfo.setCompanyStatus(companyStatus);
        jobInfo.setIndustryDirectory(IndustryDirectory);
        jobInfo.setCity(city);

        SqlSessionFactory jobInfoSessionFactory = getJobInfoSessionFactory();
        //初始化
        try (SqlSession session = jobInfoSessionFactory.openSession(true)) {
            //创建mapper对象
            JobInfoMapper mapper = session.getMapper(JobInfoMapper.class);
            QueryWrapper<JobInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_name", jobInfo.getCompanyName());
            queryWrapper.eq("treatment", jobInfo.getTreatment());
            queryWrapper.eq("job_position", jobInfo.getJobPosition());
            queryWrapper.eq("source", jobInfo.getSource());
            JobInfo info = mapper.selectOne(queryWrapper);
            if (info != null) {
                mapper.updateById(jobInfo);
            } else {
                jobInfo.setCreateDate(new Date());
                mapper.insert(jobInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SqlSessionFactory getUpJobSessionFactory(){
    return initSqlSessionUpJobMapper();
    }
    public static SqlSessionFactory getUpResponseSessionFactory(){
        return initSqlSessionUpResponseMapper();
    }
    public static SqlSessionFactory getUpLogSessionFactory(){
        return initSqlSessionUpLogMapper();
    }
    public static SqlSessionFactory getJobInfoSessionFactory(){
        return initSqlSessionJobInfoMapper();
    }
    public static SqlSessionFactory getLogInfoSessionFactory(){
        return initSqlSessionLogInfoMapper();
    }



    public static String appendParam(String name, String value) {
        return Optional.ofNullable(value)
                .filter(v -> !Objects.equals(UNLIMITED_CODE, v))
                .map(v -> "&" + name + "=" + v)
                .orElse("");
    }

    public static String appendListParam(String name, List<String> values) {
        return Optional.ofNullable(values)
                .filter(list -> !list.isEmpty() && !Objects.equals(UNLIMITED_CODE, list.get(0)))
                .map(list -> "&" + name + "=" + String.join(",", list))
                .orElse("");
    }

    @SneakyThrows
    public static <T> T getConfig(Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream is = clazz.getClassLoader().getResourceAsStream("config.yaml");
        if (is == null) {
            throw new FileNotFoundException("无法找到 config.yaml 文件");
        }
        JsonNode rootNode = mapper.readTree(is);
        String key = clazz.getSimpleName().toLowerCase().replaceAll("config", "");
        JsonNode configNode = rootNode.path(key);
        return mapper.treeToValue(configNode, clazz);
    }

    //投递岗位
    public static SqlSessionFactory initSqlSessionUpJobMapper() {
        DataSource dataSource = dataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        //在这里添加Mapper
        configuration.addMapper(UpJobMapper.class);
        configuration.setLogImpl(StdOutImpl.class);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    //投递日志
    public static SqlSessionFactory initSqlSessionUpResponseMapper() {
        DataSource dataSource = dataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        //在这里添加Mapper
        configuration.addMapper(UpResponseMapper.class);
        configuration.setLogImpl(StdOutImpl.class);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    //投递回复
    public static SqlSessionFactory initSqlSessionUpLogMapper() {
        DataSource dataSource = dataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        //在这里添加Mapper
        configuration.addMapper(UpLogMapper.class);
        configuration.setLogImpl(StdOutImpl.class);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    //岗位信息
    public static SqlSessionFactory initSqlSessionJobInfoMapper() {
        DataSource dataSource = dataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        //在这里添加Mapper
        configuration.addMapper(JobInfoMapper.class);
        configuration.setLogImpl(StdOutImpl.class);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    //信息日志
    public static SqlSessionFactory initSqlSessionLogInfoMapper() {
        DataSource dataSource = dataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        //在这里添加Mapper
        configuration.addMapper(LogInfoMapper.class);
        configuration.setLogImpl(StdOutImpl.class);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    //连接进行
    public static DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(com.mysql.cj.jdbc.Driver.class);
        dataSource.setUrl("jdbc:mysql://localhost:3306/jeecg-boot?useUnicode=true&characterEncoding=UTF-8");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }

    /**
     * 将JSON字符串转换为Map
     *
     * @param jsonString JSON字符串
     * @return Map 字符串对应的Map对象
     */
    public static Map<String, String> jsonToMap(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        Map<String, String> map = new HashMap<>();
        // 将JSONObject中的键值对转换为Map
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = null;
            if (jsonObject.get(key) != null){
                value = jsonObject.get(key).toString();
            }
            map.put(key, value);
        }
        return map;
    }



}
