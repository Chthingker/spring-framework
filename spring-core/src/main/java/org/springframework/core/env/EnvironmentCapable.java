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

package org.springframework.core.env;

/**
 * Interface indicating a component that contains and exposes an {@link Environment} reference.
 *
 * <p>All Spring application contexts are EnvironmentCapable, and the interface is used primarily
 * for performing {@code instanceof} checks in framework methods that accept BeanFactory
 * instances that may or may not actually be ApplicationContext instances in order to interact
 * with the environment if indeed it is available.
 *
 * <p>As mentioned, {@link org.springframework.context.ApplicationContext ApplicationContext}
 * extends EnvironmentCapable, and thus exposes a {@link #getEnvironment()} method; however,
 * {@link org.springframework.context.ConfigurableApplicationContext ConfigurableApplicationContext}
 * redefines {@link org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * getEnvironment()} and narrows the signature to return a {@link ConfigurableEnvironment}.
 * The effect is that an Environment object is 'read-only' until it is being accessed from
 * a ConfigurableApplicationContext, at which point it too may be configured.
 *
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */
public interface EnvironmentCapable {

	/**
	 * Return the {@link Environment} associated with this component.
	 * 概念
	 * Environment 表示运行时的“外部配置与活动配置文件”视图，负责提供属性（property）和 profile（激活/接受）信息，供容器与 bean 在初始化/解析时使用。
	 *
	 *
	 * 主要职责
	 *
	 *
	 * 持有一组有序的 PropertySource（属性来源），例如系统属性、环境变量、配置文件、Servlet context 参数等。
	 * 提供属性解析接口（PropertyResolver），包括 getProperty、resolvePlaceholders、resolveRequiredPlaceholders、setRequiredProperties 等。
	 * 管理 profile：getActiveProfiles()、getDefaultProfiles()、acceptsProfiles(...)。
	 * 关键接口 / 类（设计要点）
	 *
	 *
	 * Environment（只读行为：属性/Profiles 查询）
	 * ConfigurableEnvironment（可配置：添加/移除 PropertySource、激活 profile）
	 * PropertyResolver / ConfigurablePropertyResolver（属性解析与占位符解析）
	 * PropertySource（单个属性来源，键/值对）与 MutablePropertySources / PropertySources（有序集合）
	 * 常见实现：StandardEnvironment（普通 Java 应用），StandardServletEnvironment（web：会加入 servlet init/context params）
	 * 扩展点：EnvironmentPostProcessor（Spring Boot）、ApplicationContextInitializer（普通 Spring）用于在容器刷新前修改 Environment。
	 * 容器如何使用 Environment（举例）
	 *
	 *
	 * AbstractApplicationContext.prepareBeanFactory(...) 会把 getEnvironment() 注册为 bean（ENVIRONMENT_BEAN_NAME），并把系统属性/环境变量也注册为单例（SYSTEM_PROPERTIES_BEAN_NAME / SYSTEM_ENVIRONMENT_BEAN_NAME）。
	 * 当需要解析注解属性或 @Value、注入字符串占位符时，容器会使用 Environment（例如：在 finishBeanFactoryInitialization 若无嵌入值解析器，容器会添加 str -> environment.resolvePlaceholders(str)）。
	 * PropertySourcesPlaceholderConfigurer 等 BeanFactoryPostProcessor 可以基于 Environment 的 PropertySources 做占位符解析或转换。
	 * Web 子类会覆盖 initPropertySources() 把 ServletContext 的 init‑params / context‑params 添加进 PropertySources。
	 * 常见资源（PropertySource 类型与来源）
	 *
	 *
	 * SystemPropertiesPropertySource（System.getProperties()）
	 * SystemEnvironmentPropertySource（System.getenv()）
	 * ResourcePropertySource（从 properties/yaml 文件加载）
	 * ServletContextParameterPropertySource（web：servlet 参数）
	 * 自定义的 MapPropertySource、EnumerablePropertySource 等
	 * 使用建议（扩展与定制）
	 *
	 *
	 * 程序化：在创建 ApplicationContext 并调用 refresh() 之前，调用 context.getEnvironment().getPropertySources().addFirst(...)。
	 * Spring Boot：实现 EnvironmentPostProcessor 在 SpringApplication 启动早期注册额外 PropertySource。
	 * 在 initPropertySources() 中，子类可以把特定资源加入 Environment。
	 */
	Environment getEnvironment();

}
