package com.github.liyue2008.rpc.nameservice.jdbc;

import com.github.liyue2008.rpc.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 以HSQLDB为数据库实现的
 * JdbcNameService
 *
 * @author LiYue
 * Date: 2019/10/9
 */
public class JdbcNameService implements NameService, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(JdbcNameService.class);

    // schemes
    private static final Collection<String> schemes = Collections.singleton("jdbc");

    // DDL SQL文件名称
    private static final String DDL_SQL_FILE_NAME = "ddl";

    // 查询服务 SQL文件名称
    private static final String LOOKUP_SERVICE_SQL_FILE_NAME = "lookup-service";
    // 注册服务 SQL文件名称
    private static final String ADD_SERVICE_SQL_FILE_NAME = "add-service";

    // DB连接
    private Connection connection = null;
    // 协议 hsqldb
    private String subprotocol = null;

    /**
     * 所有支持的协议
     *
     * @return 支持的协议
     */
    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    /**
     * 连接注册中心
     *
     * @param nameServiceUri 注册中心地址
     */
    @Override
    public void connect(URI nameServiceUri) {
        try {
            close();
            // 从URI中获取协议
            subprotocol = nameServiceUri.toString().split(":")[1];
            logger.info("Database: {}.", subprotocol);
            String username = System.getProperty("nameservice.jdbc.username");
            String password = System.getProperty("nameservice.jdbc.password");
            logger.info("Connecting to database: {}...", nameServiceUri);
            if (null == username) {
                connection = DriverManager.getConnection(nameServiceUri.toString());
            } else {
                connection = DriverManager.getConnection(nameServiceUri.toString(), username, password);
            }
            logger.info("Maybe execute ddl to init database...");
            // 尝试执行DDL语句
            maybeExecuteDDL(connection);
            logger.info("Database connected.");

        } catch (SQLException | IOException e) {
            logger.warn("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param uri         服务地址
     * @throws IOException
     */
    @Override
    public void registerService(String serviceName, URI uri) throws IOException {
        // 获取 PreparedStatement 对象
        try (PreparedStatement statement = connection.prepareStatement(readSql(ADD_SERVICE_SQL_FILE_NAME))) {
            // 设置参数
            statement.setString(1, serviceName);
            statement.setString(2, uri.toString());
            // 执行更新SQL
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Exception: ", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 查询服务地址
     *
     * @param serviceName 服务名称
     * @return
     * @throws IOException
     */
    @Override
    public URI lookupService(String serviceName) throws IOException {
        try (PreparedStatement statement = connection.prepareStatement(readSql(LOOKUP_SERVICE_SQL_FILE_NAME))) {
            // 设置参数
            statement.setString(1, serviceName);
            // 执行查询SQL
            ResultSet resultSet = statement.executeQuery();
            // 服务URI集合
            List<URI> uriList = new ArrayList<>();
            // 遍历查询结果
            while (resultSet.next()) {
                uriList.add(URI.create(resultSet.getString(1)));
            }
            // 从服务URI集合中随机选择一个
            return uriList.get(ThreadLocalRandom.current().nextInt(uriList.size()));
        } catch (SQLException e) {
            logger.warn("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 尝试执行DDL语句
     *
     * @param connection
     * @throws IOException
     * @throws SQLException
     */
    private void maybeExecuteDDL(Connection connection) throws IOException, SQLException {
        try (Statement statement = connection.createStatement()) {
            // 读取SQL
            String ddlSqlString = readSql(DDL_SQL_FILE_NAME);
            // 执行SQL
            statement.execute(ddlSqlString);
        }

    }

    /**
     * 读物SQL
     *
     * @param filename
     * @return
     * @throws IOException
     */
    private String readSql(String filename) throws IOException {
        // 获取文件名名称
        String ddlFile = toFileName(filename);
        // 返回读取指定资源的输入流
        try (InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(ddlFile)) {
            if (null != in) {
                // 输入流转成字符串
                return inputStreamToString(in);
            } else {
                throw new IOException(ddlFile + " not found in classpath!");
            }
        }

    }

    /**
     * 拼接文件名称
     *
     * @param filename
     * @return
     */
    private String toFileName(String filename) {
        return filename + "." + subprotocol + ".sql";
    }

    /**
     * 输入流转成字符串
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private String inputStreamToString(InputStream inputStream) throws IOException {
        // 获取输入流中字节数量 构建字节数组大小
        byte[] bytes = new byte[inputStream.available()];
        // 将输入流中的直接读取到字节数组中
        inputStream.read(bytes);
        // 构造字符串
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        try {
            if (null != connection) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warn("Close exception: ", e);
        }
    }
}
