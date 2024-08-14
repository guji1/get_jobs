package utils;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import mapper.*;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static utils.Constant.UNLIMITED_CODE;

public class JobUtils {

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



}
