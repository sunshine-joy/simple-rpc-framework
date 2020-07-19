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
package com.github.liyue2008.rpc.client;

import com.github.liyue2008.rpc.NameService;
import com.github.liyue2008.rpc.RpcAccessPoint;
import com.github.liyue2008.rpc.hello.HelloService;
import com.github.liyue2008.rpc.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * RPC客户端
 *
 * @author LiYue
 * Date: 2019/9/20
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {
        // HelloService类规范名称
        String serviceName = HelloService.class.getCanonicalName();
        // URI create
        URI nameServiceUri = URI.create("jdbc:hsqldb:hsql://localhost/nameservice");
        // 账号密码
        System.setProperty("nameservice.jdbc.username", "SA");
        System.setProperty("nameservice.jdbc.password", "");
        String name = "Master MQ";

        /**
         * 在ServiceLoader.load的时候，根据传入的接口类，
         * 遍历META-INF/services目录下的以该类命名的文件中的所有类，并实例化返回。
         * 实际获取 NettyRpcAccessPoint 的实例
         */
        try (RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class)) {
            // 获取注册中心的引用
            NameService nameService = rpcAccessPoint.getNameService(nameServiceUri);
            assert nameService != null;
            // 查询服务地址
            URI uri = nameService.lookupService(serviceName);
            assert uri != null;
            logger.info("找到服务{}，提供者: {}.", serviceName, uri);
            HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class);
            logger.info("请求服务, name: {}...", name);
            String response = helloService.hello(name);
            logger.info("收到响应: {}.", response);
        }


    }
}
