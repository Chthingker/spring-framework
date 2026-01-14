/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 *
 * 统一容器抽象
 * ApplicationContext 在 BeanFactory 之上提供更丰富的语义：配置、生命周期管理、事件发布、消息源与资源加载等，目的是把应用运行时的“上下文”集中管理。
 * 生命周期与模板方法（可定制的启动/关闭流程）
 * 通过 refresh() / close()、以及模板方法链（如 prepareRefresh()、obtainFreshBeanFactory()、prepareBeanFactory()、finishBeanFactoryInitialization()、finishRefresh()）把启动流程分解成可被子类/扩展点插入的步骤，以保证可扩展与可观察性。
 *
 * 分层/层次化上下文（父子 Context）
 * 支持父上下文合并 Environment 与消息/Bean 继承，便于模块化和共享基础 Bean/资源。
 *
 * 可配置的 Environment 与 PropertySource（外部配置解耦）
 * 通过 Environment、PropertySource、占位符解析等，把外部配置与 profile 管理抽象出来，并在容器刷新早期提供钩子（initPropertySources()）供子类（如 Web 上下文）插入特定属性源。
 *
 * Bean 定义与延迟/预实例化策略分离
 * 由子类负责加载 BeanDefinition（refreshBeanFactory() / XmlBeanDefinitionReader 等），容器负责后续的后处理器执行与单例的预实例化（preInstantiateSingletons()）。
 *
 * 扩展点：BeanFactoryPostProcessor / BeanPostProcessor / Aware 接口
 * 在实例化 Bean 前后分别提供修改 Bean 定义和拦截实例化的插入点；ApplicationContextAware、EnvironmentAware 等通过容器注入运行时资源。
 *
 * 事件发布与监听机制
 * 内置 ApplicationEventMulticaster，在初始化期间缓冲早期事件，组件可以发布/订阅事件（publishEvent(...)、registerListeners()）。
 *
 * 资源加载与类加载器管理
 * 继承 DefaultResourceLoader 并使用 ResourcePatternResolver，同时在需要时设置临时 ClassLoader（如 LoadTimeWeaver 场景）以支持类型匹配与字节码织入。
 *
 * 线程安全与关闭钩子
 * 用锁保护 refresh/close，支持 JVM shutdown hook，确保有序销毁单例并清理缓存（防止类加载器泄露）。
 *
 * 可观测性与运行时指标
 * 支持 ApplicationStartup、startup steps，用于收集启动性能指标。
 *
 * 总结一句话：ApplicationContext 把“配置、生命周期、外部配置、扩展点与运行时基础设施”组织成一个可扩展、可插拔的模板化启动/运行环境，借助明确的钩子和职责分离，支持不同运行场景（普通、Web、原生镜像等）的定制化实现。
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the unique id of this application context.
	 * @return the unique id of the context (never null as of 7.0.2)
	 */
	String getId();

	/**
	 * Return a name for the deployed application that this context belongs to.
	 * @return a name for the deployed application, or the empty String by default
	 */
	String getApplicationName();

	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context (never {@code null})
	 */
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or {@code null} if there is no parent
	 */
	@Nullable ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * <p>This is not typically used by application code, except for the purpose of
	 * initializing bean instances that live outside the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * {@link AutowireCapableBeanFactory} interface too. The present method mainly
	 * serves as a convenient, specific facility on the ApplicationContext interface.
	 * <p><b>NOTE: As of 4.2, this method will consistently throw IllegalStateException
	 * after the application context has been closed.</b> In current Spring Framework
	 * versions, only refreshable application contexts behave that way; as of 4.2,
	 * all application context implementations will be required to comply.
	 * @return the AutowireCapableBeanFactory for this context
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (for example, if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
