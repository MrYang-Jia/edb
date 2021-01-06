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

package com.jfinal.config;

import java.util.HashMap;
import java.util.Map;
import com.jfinal.aop.AopManager;
import com.jfinal.captcha.CaptchaManager;
import com.jfinal.captcha.ICaptchaCache;
import com.jfinal.core.ActionReporter;
import com.jfinal.core.Const;
import com.jfinal.core.ControllerFactory;
import com.jfinal.i18n.I18n;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.JsonManager;
import com.jfinal.kit.StrKit;
import com.jfinal.log.ILogFactory;
import com.jfinal.log.LogManager;
import com.jfinal.proxy.ProxyFactory;
import com.jfinal.proxy.ProxyManager;
import com.jfinal.render.IRenderFactory;
import com.jfinal.render.RenderManager;
import com.jfinal.render.ViewType;
import com.jfinal.token.ITokenCache;

/**
 * The constant for JFinal runtime.
 */
final public class Constants {
	
	private boolean devMode = Const.DEFAULT_DEV_MODE;
	
	private String baseUploadPath = Const.DEFAULT_BASE_UPLOAD_PATH;
	private String baseDownloadPath = Const.DEFAULT_BASE_DOWNLOAD_PATH;
	
	private String encoding = Const.DEFAULT_ENCODING;
	private String urlParaSeparator = Const.DEFAULT_URL_PARA_SEPARATOR;
	private ViewType viewType = Const.DEFAULT_VIEW_TYPE;
	private String viewExtension = Const.DEFAULT_VIEW_EXTENSION;
	private int maxPostSize = Const.DEFAULT_MAX_POST_SIZE;
	private int freeMarkerTemplateUpdateDelay = Const.DEFAULT_FREEMARKER_TEMPLATE_UPDATE_DELAY;	// just for not devMode
	
	private ControllerFactory controllerFactory = Const.DEFAULT_CONTROLLER_FACTORY;
	private ActionReporter actionReporter = Const.DEFAULT_ACTION_REPORTER;
	private int configPluginOrder = Const.DEFAULT_CONFIG_PLUGIN_ORDER;
	
	private boolean denyAccessJsp = true;	// 默认拒绝直接访问 jsp 文件
	
	private ITokenCache tokenCache = null;
	
	/**
	 * Set development mode.
	 * @param devMode the development mode
	 */
	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}
	
	public boolean getDevMode() {
		return devMode;
	}
	
	/**
	 * 配置 configPlugin(Plugins me) 在 JFinalConfig 中被调用的次序.
	 * 
	 * 取值 1、2、3、4、5 分别表示在 configConstant(..)、configInterceptor(..)、
	 * configRoute(..)、configEngine(..)、configHandler(...)
	 * 之后被调用
	 * 
	 * 默认值为 3，那么 configPlugin(..) 将在 configRoute(...) 调用之后被调用
	 * @param 取值只能是 1、2、3、4、5
	 */
	public void setConfigPluginOrder(int configPluginOrder) {
		if (configPluginOrder < 1 || configPluginOrder > 5) {
			throw new IllegalArgumentException("configPluginOrder 只能取值为：1、2、3、4、5");
		}
		this.configPluginOrder = configPluginOrder;
	}
	
	public int getConfigPluginOrder() {
		return configPluginOrder;
	}
	
	/**
	 * Set the renderFactory
	 */
	public void setRenderFactory(IRenderFactory renderFactory) {
		if (renderFactory == null) {
			throw new IllegalArgumentException("renderFactory can not be null.");
		}
		RenderManager.me().setRenderFactory(renderFactory);
	}
	
	/**
	 * 设置 Json 转换工厂实现类，目前支持：JFinalJsonFactory(默认)、JacksonFactory、FastJsonFactory
	 * 分别支持 JFinalJson、Jackson、FastJson
	 */
	public void setJsonFactory(IJsonFactory jsonFactory) {
		if (jsonFactory == null) {
			throw new IllegalArgumentException("jsonFactory can not be null.");
		}
		JsonManager.me().setDefaultJsonFactory(jsonFactory);
	}
	
	/**
	 * 设置json转换时日期格式，常用格式有："yyyy-MM-dd HH:mm:ss"、 "yyyy-MM-dd"
	 */
	public void setJsonDatePattern(String datePattern) {
		if (StrKit.isBlank(datePattern)) {
			throw new IllegalArgumentException("datePattern can not be blank.");
		}
		JsonManager.me().setDefaultDatePattern(datePattern);
	}
	
	public void setCaptchaCache(ICaptchaCache captchaCache) {
		CaptchaManager.me().setCaptchaCache(captchaCache);
	}
	
	public void setLogFactory(ILogFactory logFactory) {
		if (logFactory == null) {
			throw new IllegalArgumentException("logFactory can not be null.");
		}
		LogManager.me().setDefaultLogFactory(logFactory);
	}
	
	/**
	 * 切换到 slf4j 日志框架，需要引入 slf4j 相关依赖
	 * 切换过去以后的用法参考 slf4j 文档
	 */
	public void setToSlf4jLogFactory() {
		LogManager.me().setToSlf4jLogFactory();
	}
	
	/**
	 * 配置 ProxyFactory 用于切换代理实现
	 * <pre>
	 * 例如：
	 * me.setProxyFactory(new CglibProxyFactory());
	 * </pre>
	 */
	public void setProxyFactory(ProxyFactory proxyFactory) {
		ProxyManager.me().setProxyFactory(proxyFactory);
	}
	
	/**
	 * proxy 模块需要 JDK 环境，如果运行环境为 JRE，可以调用本配置方法支持
	 * 
	 * 该配置需要引入 cglib-nodep 依赖：
	 * <pre>
	 *   <dependency>
   	 *     <groupId>cglib</groupId>
   	 *     <artifactId>cglib-nodep</artifactId>
   	 *     <version>3.2.5</version>
	 *   </dependency>
	 * </pre>
	 */
	public void setToCglibProxyFactory() {
		setProxyFactory(new com.jfinal.ext.proxy.CglibProxyFactory());
	}
	
	/**
	 * Set encoding. The default encoding is UTF-8.
	 * @param encoding the encoding
	 */
	public void setEncoding(String encoding) {
		if (StrKit.isBlank(encoding)) {
			throw new IllegalArgumentException("encoding can not be blank.");
		}
		this.encoding = encoding;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * 设置自定义的 ControllerFactory 用于创建 Controller 对象
	 */
	public void setControllerFactory(ControllerFactory controllerFactory) {
		if (controllerFactory == null) {
			throw new IllegalArgumentException("controllerFactory can not be null.");
		}
		this.controllerFactory = controllerFactory;
	}
	
	public ControllerFactory getControllerFactory() {
		controllerFactory.setInjectDependency(getInjectDependency());
		return controllerFactory;
	}
	
	/**
	 * 设置对 Controller、Interceptor、Validator 进行依赖注入，默认值为 false
	 * 
	 * 被注入对象默认为 singleton，可以通过 AopManager.me().setSingleton(boolean) 配置
	 * 该默认值。
	 * 
	 * 也可通过在被注入的目标类上使用 Singleton 注解覆盖上述默认值，注解配置
	 * 优先级高于默认配置
	 */
	public void setInjectDependency(boolean injectDependency) {
		AopManager.me().setInjectDependency(injectDependency);
	}
	
	public boolean getInjectDependency() {
		return AopManager.me().isInjectDependency();
	}
	
	/**
	 * 设置是否对超类进行注入
	 */
	public void setInjectSuperClass(boolean injectSuperClass) {
		AopManager.me().setInjectSuperClass(injectSuperClass);
	}
	
	public boolean getInjectSuperClass() {
		return AopManager.me().isInjectSuperClass();
	}
	
	/**
	 * Set ITokenCache implementation otherwise JFinal will use the HttpSesion to hold the token.
	 * @param tokenCache the token cache
	 */
	public void setTokenCache(ITokenCache tokenCache) {
		this.tokenCache = tokenCache;
	}
	
	public ITokenCache getTokenCache() {
		return tokenCache;
	}
	
	public String getUrlParaSeparator() {
		return urlParaSeparator;
	}
	
	public ViewType getViewType() {
		return viewType;
	}
	
	/**
	 * Set view type. The default value is ViewType.JFINAL_TEMPLATE
	 * Controller.render(String view) will use the view type to render the view.
	 * @param viewType the view type 
	 */
	public void setViewType(ViewType viewType) {
		if (viewType == null) {
			throw new IllegalArgumentException("viewType can not be null");
		}
		this.viewType = viewType;
	}
	
	/**
	 * Set urlPara separator. The default value is "-"
	 * @param urlParaSeparator the urlPara separator
	 */
	public void setUrlParaSeparator(String urlParaSeparator) {
		if (StrKit.isBlank(urlParaSeparator) || urlParaSeparator.contains("/")) {
			throw new IllegalArgumentException("urlParaSepartor can not be blank and can not contains \"/\"");
		}
		this.urlParaSeparator = urlParaSeparator;
	}
	
	public String getViewExtension() {
		return viewExtension;
	}
	
	/**
	 * Set view extension for the IRenderFactory.getDefaultRender(...)
	 * The default value is ".html"
	 * 
	 * Example: ".html" or ".ftl"
	 * @param viewExtension the extension of the view, it must start with dot char "."
	 */
	public void setViewExtension(String viewExtension) {
		this.viewExtension = viewExtension.startsWith(".") ? viewExtension : "." + viewExtension;
	}
	
	/**
	 * Set error 404 view.
	 * @param error404View the error 404 view
	 */
	public void setError404View(String error404View) {
		errorViewMapping.put(404, error404View);
	}
	
	/**
	 * Set error 500 view.
	 * @param error500View the error 500 view
	 */
	public void setError500View(String error500View) {
		errorViewMapping.put(500, error500View);
	}
	
	/**
	 * Set error 401 view.
	 * @param error401View the error 401 view
	 */
	public void setError401View(String error401View) {
		errorViewMapping.put(401, error401View);
	}
	
	/**
	 * Set error 403 view.
	 * @param error403View the error 403 view
	 */
	public void setError403View(String error403View) {
		errorViewMapping.put(403, error403View);
	}
	
	private Map<Integer, String> errorViewMapping = new HashMap<Integer, String>();
	
	public void setErrorView(int errorCode, String errorView) {
		errorViewMapping.put(errorCode, errorView);
	}
	
	public String getErrorView(int errorCode) {
		return errorViewMapping.get(errorCode);
	}
	
	public String getBaseDownloadPath() {
		return baseDownloadPath;
	}
	
	/**
	 * Set file base download path for Controller.renderFile(...)
	 * 设置文件下载基础路径，当路径以 "/" 打头或是以 windows 磁盘盘符打头，
	 * 则将路径设置为绝对路径，否则路径将是以应用根路径为基础的相对路径
	 * <pre>
	 * 例如：
	 * 1：参数 "/var/www/download" 为绝对路径，下载文件存放在此路径之下
	 * 2：参数 "download" 为相对路径，下载文件存放在 PathKit.getWebRoot() + "/download" 路径之下
	 * </pre>
	 */
	public void setBaseDownloadPath(String baseDownloadPath) {
		if (StrKit.isBlank(baseDownloadPath)) {
			throw new IllegalArgumentException("baseDownloadPath can not be blank.");
		}
		this.baseDownloadPath = baseDownloadPath;
	}
	
	/**
	 * Set file base upload path.
	 * 设置文件上传保存基础路径，当路径以 "/" 打头或是以 windows 磁盘盘符打头，
	 * 则将路径设置为绝对路径，否则路径将是以应用根路径为基础的相对路径
	 * <pre>
	 * 例如：
	 * 1：参数 "/var/www/upload" 为绝对路径，上传文件将保存到此路径之下
	 * 2：参数 "upload" 为相对路径，上传文件将保存到 PathKit.getWebRoot() + "/upload" 路径之下
	 * </pre>
	 */
	public void setBaseUploadPath(String baseUploadPath) {
		if (StrKit.isBlank(baseUploadPath)) {
			throw new IllegalArgumentException("baseUploadPath can not be blank.");
		}
		this.baseUploadPath = baseUploadPath;
	}
	
	public String getBaseUploadPath() {
		return baseUploadPath;
	}
	
	public int getMaxPostSize() {
		return maxPostSize;
	}
	
	/**
	 * Set max size of http post. The upload file size depend on this value.
	 */
	public void setMaxPostSize(int maxPostSize) {
		this.maxPostSize = maxPostSize;
	}
	
	/**
	 * Set default base name to load Resource bundle.
	 * The default value is "i18n".<tr>
	 * Example:
	 * setI18nDefaultBaseName("i18n");
	 */
	public void setI18nDefaultBaseName(String defaultBaseName) {
		I18n.setDefaultBaseName(defaultBaseName);
	}
	
	/**
	 * Set default locale to load Resource bundle.
	 * The locale string like this: "zh_CN" "en_US".<br>
	 * Example:
	 * setI18nDefaultLocale("zh_CN");
	 */
	public void setI18nDefaultLocale(String defaultLocale) {
		I18n.setDefaultLocale(defaultLocale);
	}
	
	/**
	 * 设置 devMode 之下的 action report 是否在 invocation 之后，默认值为 true
	 */
	public void setReportAfterInvocation(boolean reportAfterInvocation) {
		ActionReporter.setReportAfterInvocation(reportAfterInvocation);
	}
	
	/**
	 * FreeMarker template update delay for not devMode.
	 */
	public void setFreeMarkerTemplateUpdateDelay(int delayInSeconds) {
		if (delayInSeconds < 0) {
			throw new IllegalArgumentException("template_update_delay must more than -1.");
		}
		this.freeMarkerTemplateUpdateDelay = delayInSeconds;
	}
	
	public int getFreeMarkerTemplateUpdateDelay() {
		return freeMarkerTemplateUpdateDelay;
	}
	
	public void setDenyAccessJsp(boolean denyAccessJsp) {
		this.denyAccessJsp = denyAccessJsp;
	}
	
	public boolean getDenyAccessJsp() {
		return denyAccessJsp;
	}
	
	/**
	 * 设置自定义的 ActionReporter 用于定制 action report 输出功能
	 */
	public void setActionReporter(ActionReporter actionReporter) {
		this.actionReporter = actionReporter;
	}
	
	public ActionReporter getActionReporter() {
		return actionReporter;
	}
	
	/**
	 * 设置为 Headless Mode，否则在缺少显示设备时验证码功能不能使用，并抛出异常
	 * java.awt.HeadlessException
	 * 
	 * Headless 模式是系统的一种配置模式。在该模式下，系统缺少显示设备、键盘或鼠标。
	 * 配置为 "true" 时 Graphics、Font、Color、ImageIO、Print、Graphics2D
	 * 等等 API 仍然能够使用
	 */
	public void setToJavaAwtHeadless() {
		System.setProperty("java.awt.headless", "true");
	}
}





