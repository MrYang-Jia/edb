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

package com.jfinal.template.expr.ast;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * MethodInfo
 */
public class MethodInfo {
	
	protected final Long key;
	protected final Class<?> clazz;
	protected final Method method;
	
	protected final boolean isVarArgs;
	protected final Class<?>[] paraTypes;
	
	public MethodInfo(Long key, Class<?> clazz, Method method) {
		this.key = key;
		this.clazz = clazz;
		this.method = method;
		this.isVarArgs = method.isVarArgs();
		this.paraTypes = method.getParameterTypes();
	}
	
	public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
		if (isVarArgs) {
			return invokeVarArgsMethod(target, args);
		} else {
			return method.invoke(target, args);
		}
	}
	
	protected Object invokeVarArgsMethod(Object target, Object[] argValues) throws ReflectiveOperationException {
		Object[] finalArgValues = new Object[paraTypes.length];
		
		int fixedParaLength = paraTypes.length - 1;
		System.arraycopy(argValues, 0, finalArgValues, 0, fixedParaLength);
		Class<?> varParaComponentType = paraTypes[paraTypes.length - 1].getComponentType();
		Object varParaValues = Array.newInstance(varParaComponentType, argValues.length - fixedParaLength);
		int p = 0;
		for (int i=fixedParaLength; i<argValues.length; i++) {
			Array.set(varParaValues, p++, argValues[i]);
		}
		finalArgValues[paraTypes.length - 1] = varParaValues;
		return method.invoke(target, finalArgValues);
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getName() {
		return method.getName();
	}
	
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}
	
	public boolean isVarArgs() {
		return isVarArgs;
	}
	
	protected Class<?>[] getParameterTypes() {
		return paraTypes;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder(clazz.getName()).append(".").append(method.getName()).append("(");
		for (int i=0; i<paraTypes.length; i++) {
			if (i > 0) {
				ret.append(", ");
			}
			ret.append(paraTypes[i].getName());
		}
		return ret.append(")").toString();
	}
	
	// --------- 以下代码仅用于支持 NullMethodInfo
	
	/**
	 * 仅供 NullMethodInfo 继承使用
	 */
	protected MethodInfo() {
		this.key = null;
		this.clazz = null;
		this.method = null;
		this.isVarArgs = false;
		this.paraTypes = null;
	}
	
	/**
	 * 仅仅 NullMethodInfo 会覆盖此方法并返回 false
	 *
	 * 1：MethodKit.getMethod(...) 消除 instanceof 判断
	 * 2：Method.exec(...) 消除 null 值判断
	 */
	public boolean notNull() {
		return true;
	}
}


