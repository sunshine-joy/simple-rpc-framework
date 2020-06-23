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
package com.github.liyue2008.rpc.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * SPI类加载器帮助类
 *
 * @author LiYue
 * Date: 2019-03-26
 */
public class ServiceSupport {

    // 单例实现实例map
    private final static Map<String, Object> singletonServices = new HashMap<>();

    /**
     * 在ServiceLoader.load的时候，根据传入的接口类，
     * 遍历META-INF/services目录下的以该类命名的文件中的所有类，并实例化返回。
     *
     * @param service
     * @param <S>
     * @return
     */
    public synchronized static <S> S load(Class<S> service) {
        return StreamSupport.
                stream(ServiceLoader.load(service).spliterator(), false)
                .map(ServiceSupport::singletonFilter)
                .findFirst().orElseThrow(ServiceLoadException::new);
    }

    /**
     * 在ServiceLoader.load的时候，根据传入的接口类，
     * 遍历META-INF/services目录下的以该类命名的文件中的所有类，并实例化返回。
     *
     * @param service
     * @param <S>
     * @return
     */
    public synchronized static <S> Collection<S> loadAll(Class<S> service) {
        return StreamSupport.
                stream(ServiceLoader.load(service).spliterator(), false)
                .map(ServiceSupport::singletonFilter).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <S> S singletonFilter(S service) {
        // 是否存在注解
        if (service.getClass().isAnnotationPresent(Singleton.class)) {
            // 获取类的规范名称
            String className = service.getClass().getCanonicalName();
            // 如果缺失就放入单例实现实例map
            Object singletonInstance = singletonServices.putIfAbsent(className, service);
            return singletonInstance == null ? service : (S) singletonInstance;
        } else {
            // 不存在注解直接返回
            return service;
        }
    }
}
