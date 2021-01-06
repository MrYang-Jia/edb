/**
 * Copyright (c) 2011-2021, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.core;

import java.lang.reflect.Method;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.paragetter.ParaProcessor;
import com.jfinal.core.paragetter.ParaProcessorBuilder;

/**
 * Action
 */
public class Action {
	
	private final Class<? extends Controller> controllerClass;
	private final String controllerPath;
	private final String actionKey;
	private final Method method;
	private final String methodName;
	private final Interceptor[] interceptors;
	private final String viewPath;
	
	private final ParaProcessor parameterGetter;
	
	public Action(String controllerPath, String actionKey, Class<? extends Controller> controllerClass, Method method, String methodName, Interceptor[] interceptors, String viewPath) {
		this.controllerPath = controllerPath;
		this.actionKey = actionKey;
		this.controllerClass = controllerClass;
		this.method = method;
		this.methodName = methodName;
		this.interceptors = interceptors;
		this.viewPath = viewPath;
		
		this.parameterGetter = ParaProcessorBuilder.me.build(controllerClass, method);
	}
	
	public Class<? extends Controller> getControllerClass() {
		return controllerClass;
	}
	
	public String getControllerPath() {
		return controllerPath;
	}
	
	/**
	 * 该方法已改名为 getControllerPath()
	 */
	@Deprecated
	public String getControllerKey() {
		return controllerPath;
	}
	
	public String getActionKey() {
		return actionKey;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public Interceptor[] getInterceptors() {
		return interceptors;
	}
	
	public String getViewPath() {
		return viewPath;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public ParaProcessor getParameterGetter() {
		return parameterGetter;
	}
}









