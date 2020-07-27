

import com.github.liyue2008.rpc.NameService;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 *
 * @author lidongyang
 * @date 2020/7/28
 */
public class MySQLNameService implements NameService, Closeable {
    /**
     * 所有支持的协议
     *
     * @return 支持的协议
     */
    @Override
    public Collection<String> supportedSchemes() {
        return null;
    }

    /**
     * 连接注册中心
     *
     * @param nameServiceUri 注册中心地址
     */
    @Override
    public void connect(URI nameServiceUri) {

    }

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param uri         服务地址
     */
    @Override
    public void registerService(String serviceName, URI uri) throws IOException {

    }

    /**
     * 查询服务
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    @Override
    public URI lookupService(String serviceName) throws IOException {
        return null;
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {

    }
}
