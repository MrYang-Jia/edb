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

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.handler.Handler;
import com.jfinal.log.Log;

/**
 * JFinal framework filter
 */
public class JFinalFilter implements Filter {
	
	protected JFinalConfig jfinalConfig;
	protected int contextPathLength;
	protected Constants constants;
	protected String encoding;
	protected Handler handler;
	protected static Log log;
	
	protected static final JFinal jfinal = JFinal.me();
	
	public JFinalFilter() {
		this.jfinalConfig = null;
	}
	
	/**
	 * 支持 web 项目无需 web.xml 配置文件，便于嵌入式整合 jetty、undertow
	 */
	public JFinalFilter(JFinalConfig jfinalConfig) {
		this.jfinalConfig = jfinalConfig;
	}
	
	@SuppressWarnings("deprecation")
	public void init(FilterConfig filterConfig) throws ServletException {
		if (jfinalConfig == null) {
			createJFinalConfig(filterConfig.getInitParameter("configClass"));
		}
		
		jfinal.init(jfinalConfig, filterConfig.getServletContext());
		
		String contextPath = filterConfig.getServletContext().getContextPath();
		contextPathLength = (contextPath == null || "/".equals(contextPath) ? 0 : contextPath.length());
		
		constants = Config.getConstants();
		encoding = constants.getEncoding();
		
		jfinalConfig.onStart();
		jfinalConfig.afterJFinalStart();
		
		handler = jfinal.getHandler();		// 开始接受请求
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		request.setCharacterEncoding(encoding);
		
		String target = request.getRequestURI();
		if (contextPathLength != 0) {
			target = target.substring(contextPathLength);
		}
		
		boolean[] isHandled = {false};
		try {
			handler.handle(target, request, response, isHandled);
		}
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
		}
		
		if (isHandled[0] == false) {
			// 默认拒绝直接访问 jsp 文件，加固 tomcat、jetty 安全性
			if (constants.getDenyAccessJsp() && isJsp(target)) {
				com.jfinal.kit.HandlerKit.renderError404(request, response, isHandled);
				return ;
			}
			
			chain.doFilter(request, response);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void destroy() {
		handler = null;		// 停止接受请求
		
		jfinalConfig.onStop();
		jfinalConfig.beforeJFinalStop();
		
		jfinal.stopPlugins();
	}
	
	protected void createJFinalConfig(String configClass) {
		if (configClass == null) {
			throw new RuntimeException("The configClass parameter of JFinalFilter can not be blank");
		}
		
		try {
			Object temp = Class.forName(configClass).getDeclaredConstructor().newInstance();
			jfinalConfig = (JFinalConfig)temp;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Can not create instance of class: " + configClass, e);
		}
	}
	
	static void initLog() {
		log = Log.getLog(JFinalFilter.class);
	}
	
	boolean isJsp(String t) {
		char c;
		int end = t.length() - 1;
		
		if ( (end > 3) && ((c = t.charAt(end)) == 'x' || c == 'X') ) {
			end--;
		}
		
		if ( (end > 2) && ((c = t.charAt(end)) == 'p' || c == 'P') ) {
			end--;
			if ( (end > 1) && ((c = t.charAt(end)) == 's' || c == 'S') ) {
				end--;
				if ( (end > 0) && ((c = t.charAt(end)) == 'j' || c == 'J') ) {
					end--;
					if ( (end > -1) && ((c = t.charAt(end)) == '.') ) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
