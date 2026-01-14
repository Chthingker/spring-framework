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

/**
 * A common interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 * <b>NOTE: This interface does not imply specific auto-startup semantics.
 * Consider implementing {@link SmartLifecycle} for that purpose.</b>
 *
 * <p>Can be implemented by both components (typically a Spring bean defined in a
 * Spring context) and containers  (typically a Spring {@link ApplicationContext}
 * itself). Containers will propagate start/stop signals to all components that
 * apply within each container, for example, for a stop/restart scenario at runtime.
 *
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the {@link org.springframework.jmx.export.MBeanExporter}
 * will typically be defined with an
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},
 * restricting the visibility of activity-controlled components to the Lifecycle
 * interface.
 *
 * <p>Note that the present {@code Lifecycle} interface is only supported on
 * <b>top-level singleton beans</b>. On any other component, the {@code Lifecycle}
 * interface will remain undetected and hence ignored. Also, note that the extended
 * {@link SmartLifecycle} interface provides sophisticated integration with the
 * application context's startup and shutdown phases.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 *
 * 设计模式：Lifecycle 本身是个面向接口的抽象（Interface-based design），在架构上属于「策略（Strategy）/角色接口」的范畴：它把“可启动/可停止”的行为抽象出来，让不同组件按统一约定实现。容器层面还配合了「模板方法（Template Method）」与「委派/策略（Delegate/Strategy）」——AbstractApplicationContext 在启动/关闭时使用模板方法分步骤执行，并委派给 LifecycleProcessor 去实际管理所有实现 Lifecycle 的 bean。事件发布则属于「观察者（Observer）」模式（publishEvent / listeners）。
 *
 * 为什么 ApplicationContext/AbstractApplicationContext 会实现/依赖该接口：
 *
 * 统一控制：上下文本身也需要可启动/停止（例如在嵌入式/运行时管理场景），实现同一接口可以把容器当作一个可控组件来操作。
 * 统一编排：容器在 start()/stop() 时会通过 LifecycleProcessor 批量启动/停止那些实现 Lifecycle 的顶级单例 bean，从而统一管理资源（线程池、消息监听器、调度器等）。比如 AbstractApplicationContext.start() 会调用 getLifecycleProcessor().start() 并发布 ContextStartedEvent。
 * 可扩展：更高级的 SmartLifecycle 提供自动启动、相位（phase）与顺序控制，适用于有启动顺序依赖的组件（例如先启动消息监听器再启动调度器）。
 *
 *
 * 总结：Lifecycle 是一个轻量的接口式抽象，用来把“生命周期控制”作为可插拔的、可被容器统一管理的职责；ApplicationContext 实现/使用它是为了能把容器自身和容器内的可管理组件以一致的方式启动/停止并支持更丰富的编排（SmartLifecycle、LifecycleProcessor 等）。
 */
public interface Lifecycle {

	/**
	 * Start this component.
	 * <p>Should not throw an exception if the component is already running.
	 * <p>In the case of a container, this will propagate a hard start signal to all
	 * components that apply, even to non-auto-startup components.
	 * @see SmartLifecycle#isAutoStartup()
	 */
	void start();

	/**
	 * Stop this component, typically in a synchronous fashion, such that the component is
	 * fully stopped upon return of this method. Consider implementing {@link SmartLifecycle}
	 * and its {@code stop(Runnable)} variant when asynchronous stop behavior is necessary.
	 * <p>Note that this stop notification is not guaranteed to come before destruction:
	 * On regular shutdown, {@code Lifecycle} beans will first receive a stop notification
	 * before the general destruction callbacks are being propagated; however, on hot
	 * refresh during a context's lifetime or on aborted refresh attempts, a given bean's
	 * destroy method will be called without any consideration of stop signals upfront.
	 * <p>Should not throw an exception if the component is not running (not started yet).
	 * <p>In the case of a container, this will propagate the stop signal to all components
	 * that apply.
	 * @see SmartLifecycle#stop(Runnable)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	void stop();

	/**
	 * Check whether this component is currently running.
	 * <p>In the case of a container, this will return {@code true} only if <i>all</i>
	 * components that apply are currently running.
	 * @return whether the component is currently running
	 */
	boolean isRunning();

}
