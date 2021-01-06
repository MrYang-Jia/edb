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

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfinal.kit.ReflectKit;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.template.ext.extensionmethod.ByteExt;
import com.jfinal.template.ext.extensionmethod.DoubleExt;
import com.jfinal.template.ext.extensionmethod.FloatExt;
import com.jfinal.template.ext.extensionmethod.IntegerExt;
import com.jfinal.template.ext.extensionmethod.LongExt;
import com.jfinal.template.ext.extensionmethod.ShortExt;
import com.jfinal.template.ext.extensionmethod.StringExt;

/**
 * MethodKit
 */
public class MethodKit {
	
	private static final Class<?>[] NULL_ARG_TYPES = new Class<?>[0];
	private static final Set<String> forbiddenMethods = new HashSet<String>(64);
	private static final Set<Class<?>> forbiddenClasses = new HashSet<Class<?>>(64);
	private static final Map<Class<?>, Class<?>> primitiveMap = new HashMap<Class<?>, Class<?>>(64);
	private static final SyncWriteMap<Long, MethodInfo> methodCache = new SyncWriteMap<Long, MethodInfo>(2048, 0.25F);
	
	// 初始化在模板中调用 method 时所在的被禁止使用类
	static {
		Class<?>[] cs = {
			System.class, Runtime.class, Thread.class, Class.class, ClassLoader.class, File.class,
			Compiler.class, InheritableThreadLocal.class, Package.class, Process.class,
			RuntimePermission.class, SecurityManager.class, ThreadGroup.class, ThreadLocal.class,
			
			java.lang.reflect.Method.class,
			java.lang.reflect.Proxy.class
		};
		for (Class<?> c : cs) {
			forbiddenClasses.add(c);
		}
	}
	
	// 初始化在模板中被禁止使用的 method name
	static {
		String[] ms = {
			"getClass", "getDeclaringClass", "forName", "newInstance", "getClassLoader",
			"invoke", // "getMethod", "getMethods", // "getField", "getFields",
			"notify", "notifyAll", "wait",
			"exit", "loadLibrary", "halt", // "load",
			"stop", "suspend", "resume" // "setDaemon", "setPriority"
		};
		for (String m : ms) {
			forbiddenMethods.add(m);
		}
	}
	
	// 初始化 primitive type 与 boxed type 双向映射关系
	static {
		primitiveMap.put(byte.class, Byte.class);
		primitiveMap.put(short.class, Short.class);
		primitiveMap.put(int.class, Integer.class);
		primitiveMap.put(long.class, Long.class);
		primitiveMap.put(float.class, Float.class);
		primitiveMap.put(double.class, Double.class);
		primitiveMap.put(char.class, Character.class);
		primitiveMap.put(boolean.class, Boolean.class);
		
		primitiveMap.put(Byte.class, byte.class);
		primitiveMap.put(Short.class, short.class);
		primitiveMap.put(Integer.class, int.class);
		primitiveMap.put(Long.class, long.class);
		primitiveMap.put(Float.class, float.class);
		primitiveMap.put(Double.class, double.class);
		primitiveMap.put(Character.class, char.class);
		primitiveMap.put(Boolean.class, boolean.class);
	}
	
	public static boolean isForbiddenClass(Class<?> clazz) {
		return forbiddenClasses.contains(clazz);
	}
	
	public static void addForbiddenClass(Class<?> clazz) {
		forbiddenClasses.add(clazz);
	}
	
	public static void removeForbiddenClass(Class<?> clazz) {
		forbiddenClasses.remove(clazz);
	}
	
	public static boolean isForbiddenMethod(String methodName) {
		return forbiddenMethods.contains(methodName);
	}
	
	public static void addForbiddenMethod(String methodName) {
		forbiddenMethods.add(methodName);
	}
	
	public static void removeForbiddenMethod(String methodName) {
		forbiddenMethods.remove(methodName);
	}
	
	public static void clearCache() {
		methodCache.clear();
	}
	
	public static MethodInfo getMethod(Class<?> targetClass, String methodName, Object[] argValues) {
		Class<?>[] argTypes = getArgTypes(argValues);
		Long key = getMethodKey(targetClass, methodName, argTypes);
		MethodInfo method = methodCache.get(key);
		if (method == null) {
			// 已确保不会返回 null，对于不存在的 Method，只进行一次获取操作
			// 提升 null safe 表达式性能，未来需要考虑内存泄漏风险
			method = doGetMethod(key, targetClass, methodName, argTypes);
			methodCache.putIfAbsent(key, method);
		}
		
		return method;
	}
	
	static Class<?>[] getArgTypes(Object[] argValues) {
		if (argValues == null || argValues.length == 0) {
			return NULL_ARG_TYPES;
		}
		Class<?>[] argTypes = new Class<?>[argValues.length];
		for (int i=0; i<argValues.length; i++) {
			argTypes[i] = argValues[i] != null ? argValues[i].getClass() : null;
		}
		return argTypes;
	}
	
	private static MethodInfo doGetMethod(Long key, Class<?> targetClass, String methodName, Class<?>[] argTypes) {
		if (forbiddenClasses.contains(targetClass)) {
			throw new RuntimeException("Forbidden class: " + targetClass.getName());
		}
		
		// 仅开启 forbiddenClasses 检测
		// Method、SharedMethod、StaticMethod 已用 MethodKit.isForbiddenMethod(...) 检测
		// if (forbiddenMethods.contains(methodName)) {
		// 	throw new RuntimeException("Forbidden method: " + methodName);
		// }
		
		Method[] methodArray = targetClass.getMethods();
		for (Method method : methodArray) {
			if (method.getName().equals(methodName)) {
				Class<?>[] paraTypes = method.getParameterTypes();
				if (matchFixedArgTypes(paraTypes, argTypes)) {	// 无条件优先匹配固定参数方法
					return new MethodInfo(key, targetClass, method);
				}
				if (method.isVarArgs() && matchVarArgTypes(paraTypes, argTypes)) {
					return new MethodInfo(key, targetClass, method);
				}
			}
		}
		
		return NullMethodInfo.me;
	}
	
	static boolean matchFixedArgTypes(Class<?>[] paraTypes, Class<?>[] argTypes) {
		if (paraTypes.length != argTypes.length) {
			return false;
		}
		return matchRangeTypes(paraTypes, argTypes, paraTypes.length);
	}
	
	private static boolean matchRangeTypes(Class<?>[] paraTypes, Class<?>[] argTypes, int matchLength) {
		for (int i=0; i<matchLength; i++) {
			if (argTypes[i] == null) {
				if (paraTypes[i].isPrimitive()) {
					return false;
				}
				continue ;
			}
			if (paraTypes[i].isAssignableFrom(argTypes[i])) {
				continue ;
			}
			// object instanceof Xxx、Class.isAssignableFrom(Class)、Class.isInstance(Object) not works for primitive type
			if (paraTypes[i] == argTypes[i] || primitiveMap.get(paraTypes[i]) == argTypes[i]) {
				continue ;
			}
			return false;
		}
		return true;
	}
	
	static boolean matchVarArgTypes(Class<?>[] paraTypes, Class<?>[] argTypes) {
		int fixedParaLength = paraTypes.length - 1;
		if (argTypes.length < fixedParaLength) {
			return false;
		}
		if (!matchRangeTypes(paraTypes, argTypes, fixedParaLength)) {
			return false;
		}
		
		Class<?> varArgType = paraTypes[paraTypes.length - 1].getComponentType();
		for (int i=fixedParaLength; i<argTypes.length; i++) {
			if (argTypes[i] == null) {
				if (varArgType.isPrimitive()) {
					return false;
				}
				continue ;
			}
			if (varArgType.isAssignableFrom(argTypes[i])) {
				continue ;
			}
			if (varArgType == argTypes[i] || primitiveMap.get(varArgType) == argTypes[i]) {
				continue ;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 获取方法用于缓存的 key
	 */
	private static Long getMethodKey(Class<?> targetClass, String methodName, Class<?>[] argTypes) {
		return MethodKeyBuilder.instance.getMethodKey(targetClass, methodName, argTypes);
	}
	
	// 以下代码实现 extension method 功能 --------------------
	
	// 添加 jfinal 官方扩展方法 extension method
	static {
		addExtensionMethod(String.class, new StringExt());
		addExtensionMethod(Integer.class, new IntegerExt());
		addExtensionMethod(Long.class, new LongExt());
		addExtensionMethod(Float.class, new FloatExt());
		addExtensionMethod(Double.class, new DoubleExt());
		addExtensionMethod(Short.class, new ShortExt());
		addExtensionMethod(Byte.class, new ByteExt());
	}
	
	public synchronized static void addExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
		Class<?> extensionClass = objectOfExtensionClass.getClass();
		java.lang.reflect.Method[] methodArray = extensionClass.getMethods();
		for (java.lang.reflect.Method method : methodArray) {
			Class<?> decClass = method.getDeclaringClass();	
			if (decClass == Object.class) {		// 考虑用于优化路由生成那段代码
				continue ;
			}
			
			Class<?>[] extensionMethodParaTypes = method.getParameterTypes();
			String methodName = method.getName();
			if (extensionMethodParaTypes.length == 0) {
				throw new RuntimeException(buildMethodSignatureForException("Extension method requires at least one argument: " + extensionClass.getName() + ".", methodName, extensionMethodParaTypes));
			}
			
			// Extension method 第一个参数必须与当前对象的类型一致，在调用时会将当前对象自身传给扩展方法的第一个参数
			// if (targetClass != extensionMethodParaTypes[0]) {
			if (!extensionMethodParaTypes[0].isAssignableFrom(targetClass)) {	// 支持第一个参数为被扩展类的父类，注意在注册时仍要确切的子类
				throw new RuntimeException(buildMethodSignatureForException("The first argument type of : " + extensionClass.getName() + ".", methodName, extensionMethodParaTypes) + " must be: " + targetClass.getName());
			}
			
			Class<?>[] targetParaTypes = new Class<?>[extensionMethodParaTypes.length - 1];
			System.arraycopy(extensionMethodParaTypes, 1, targetParaTypes, 0, targetParaTypes.length);
			
			try {
				Method error = targetClass.getMethod(methodName, targetParaTypes);
				if (error != null) {
					throw new RuntimeException("Extension method \"" + methodName + "\" is already exists in class \"" + targetClass.getName() + "\"");
				}
			} catch (NoSuchMethodException e) {		// Method 找不到才能添加该扩展方法
				Long key = MethodKit.getMethodKey(targetClass, methodName, toBoxedType(targetParaTypes));
				if (methodCache.containsKey(key)) {
					throw new RuntimeException(buildMethodSignatureForException("The extension method is already exists: " + extensionClass.getName() + ".", methodName, targetParaTypes));
				}
				
				MethodInfoExt mie = new MethodInfoExt(objectOfExtensionClass, key, extensionClass/* targetClass */, method);
				methodCache.putIfAbsent(key, mie);
			}
		}
	}
	
	public static void addExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
		addExtensionMethod(targetClass, ReflectKit.newInstance(extensionClass));
	}
	
	public static void removeExtensionMethod(Class<?> targetClass, Object objectOfExtensionClass) {
		Class<?> extensionClass = objectOfExtensionClass.getClass();
		java.lang.reflect.Method[] methodArray = extensionClass.getMethods();
		for (java.lang.reflect.Method method : methodArray) {
			Class<?> decClass = method.getDeclaringClass();	
			if (decClass == Object.class) {		// 考虑用于优化路由生成那段代码
				continue ;
			}
			
			Class<?>[] extensionMethodParaTypes = method.getParameterTypes();
			String methodName = method.getName();
			Class<?>[] targetParaTypes = new Class<?>[extensionMethodParaTypes.length - 1];
			System.arraycopy(extensionMethodParaTypes, 1, targetParaTypes, 0, targetParaTypes.length);
			
			Long key = MethodKit.getMethodKey(targetClass, methodName, toBoxedType(targetParaTypes));
			methodCache.remove(key);
		}
	}
	
	private static final Map<Class<?>, Class<?>> primitiveToBoxedMap = new HashMap<Class<?>, Class<?>>(64);
	
	// 初始化 primitive type 到 boxed type 的映射
	static {
		primitiveToBoxedMap.put(byte.class, Byte.class);
		primitiveToBoxedMap.put(short.class, Short.class);
		primitiveToBoxedMap.put(int.class, Integer.class);
		primitiveToBoxedMap.put(long.class, Long.class);
		primitiveToBoxedMap.put(float.class, Float.class);
		primitiveToBoxedMap.put(double.class, Double.class);
		primitiveToBoxedMap.put(char.class, Character.class);
		primitiveToBoxedMap.put(boolean.class, Boolean.class);
	}
	
	/**
	 * 由于从在模板中传递的基本数据类型参数只可能是 boxed 类型，当 extension method 中的方法参数是
	 * primitive 类型时，在 getMethod(key) 时无法获取 addExtensionMethod(...) 注册的扩展方法
	 * 所以为扩展方法调用 getMethodKey(...) 生成 key 时一律转成 boxed 类型去生成方法的 key 值
	 * 
	 * 注意：该值仅用于在获取方法是通过 key 能获取到 MethindInfoExt，而 MethindInfoExt.paraType 仍然
	 *      是原来的参数值
	 */
	private static Class<?>[] toBoxedType(Class<?>[] targetParaTypes) {
		int len = targetParaTypes.length;
		if (len == 0) {
			return targetParaTypes;
		}
		
		Class<?>[] ret = new Class<?>[len];
		for (int i=0; i<len; i++) {
			Class<?> temp = primitiveToBoxedMap.get(targetParaTypes[i]);
			if (temp != null) {
				ret[i] = temp;
			} else {
				ret[i] = targetParaTypes[i];
			}
		}
		return ret;
	}
	
	public static void removeExtensionMethod(Class<?> targetClass, Class<?> extensionClass) {
		removeExtensionMethod(targetClass, ReflectKit.newInstance(extensionClass));
	}
	
	private static String buildMethodSignatureForException(String preMsg, String methodName, Class<?>[] argTypes) {
		StringBuilder ret = new StringBuilder().append(preMsg).append(methodName).append("(");
		if (argTypes != null) {
			for (int i = 0; i < argTypes.length; i++) {
				if (i > 0) {
					ret.append(", ");
				}
				ret.append(argTypes[i] != null ? argTypes[i].getName() : "null");
			}
		}
		return ret.append(")").toString();
	}
}










