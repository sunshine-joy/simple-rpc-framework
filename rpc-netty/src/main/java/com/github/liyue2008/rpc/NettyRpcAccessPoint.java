/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.liyue2008.rpc;

import com.github.liyue2008.rpc.client.StubFactory;
import com.github.liyue2008.rpc.server.ServiceProviderRegistry;
import com.github.liyue2008.rpc.spi.ServiceSupport;
import com.github.liyue2008.rpc.transport.RequestHandlerRegistry;
import com.github.liyue2008.rpc.transport.Transport;
import com.github.liyue2008.rpc.transport.TransportClient;
import com.github.liyue2008.rpc.transport.TransportServer;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * RPC框架对外提供的服务接口的实现
 *
 * @author LiYue
 * Date: 2019/9/20
 */
public class NettyRpcAccessPoint implements RpcAccessPoint {
    private final String host = "localhost";
    private final int port = 9999;
    private final URI uri = URI.create("rpc://" + host + ":" + port);
    private TransportServer server = null;
    private TransportClient client = ServiceSupport.load(TransportClient.class);
    private final Map<URI, Transport> clientMap = new ConcurrentHashMap<>();
    private final StubFactory stubFactory = ServiceSupport.load(StubFactory.class);
    private final ServiceProviderRegistry serviceProviderRegistry = ServiceSupport.load(ServiceProviderRegistry.class);

    /**
     * 客户端获取远程服务的引用（桩）
     *
     * @param uri          远程服务地址
     * @param serviceClass 服务的接口类的Class
     * @param <T>服务接口的类型
     * @return 远程服务引用（桩）
     */
    @Override
    public <T> T getRemoteService(URI uri, Class<T> serviceClass) {
        Transport transport = clientMap.computeIfAbsent(uri, this::createTransport);
        return stubFactory.createStub(transport, serviceClass);
    }

    private Transport createTransport(URI uri) {
        try {
            return client.createTransport(new InetSocketAddress(uri.getHost(), uri.getPort()), 30000L);
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized <T> URI addServiceProvider(T service, Class<T> serviceClass) {
        serviceProviderRegistry.addServiceProvider(serviceClass, service);
        return uri;
    }

    @Override
    public synchronized Closeable startServer() throws Exception {
        if (null == server) {
            server = ServiceSupport.load(TransportServer.class);
            server.start(RequestHandlerRegistry.getInstance(), port);

        }
        return () -> {
            if (null != server) {
                server.stop();
            }
        };
    }

    @Override
    public void close() {
        if (null != server) {
            server.stop();
        }
        client.close();
    }
}
