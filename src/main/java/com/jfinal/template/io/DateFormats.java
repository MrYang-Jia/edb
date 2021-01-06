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

package com.jfinal.template.io;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * DateFormats
 * 
 * 备忘：请勿使用 TimeKit.getSimpleDateFormat(String) 优化这里，可减少一次
 *      ThreadLocal.get() 调用
 */
public class DateFormats {
	
	/**
	 * SimpleDateFormat 非线程安全，结合 WriterBuffer 中的 ThreadLocal 确保线程安全
	 */
	private Map<String, SimpleDateFormat> map = new HashMap<String, SimpleDateFormat>(16, 0.25F);
	
	public SimpleDateFormat getDateFormat(String datePattern) {
		SimpleDateFormat ret = map.get(datePattern);
		if (ret == null) {
			ret = new SimpleDateFormat(datePattern);
			map.put(datePattern, ret);
		}
		return ret;
	}
}






