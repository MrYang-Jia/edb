/**
 * Copyright (c) 2011-2017, 玛雅牛 (myaniu AT gmail.com).
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
package com.jfinal.core.paragetter;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Action;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

public class LongArrayGetter extends ParaGetter<Long[]> {

	public LongArrayGetter(String parameterName, String defaultValue) {
		super(parameterName,defaultValue);
	}

	@Override
	public Long[] get(Action action, Controller c) {
		Long[] ret =  c.getParaValuesToLong(getParameterName());
		if( null == ret) {
			ret =  this.getDefaultValue();
		}
		return ret;
	}

	@Override
	protected Long[] to(String v) {
		if(StrKit.notBlank(v)){
			String[] ss = v.split(",");
			List<Long> ls = new ArrayList<Long>(ss.length);
			for(String s : ss){
				if(StrKit.notBlank(s)){
					ls.add(Long.parseLong(s.trim()));
				}
			}
			return ls.toArray(new Long[0]);
		}
		return null;
	}
}
