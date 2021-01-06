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

import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.ast.SharedMethodKit.SharedMethodInfo;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * SharedMethod
 * 
 * 用法：
 * engine.addSharedMethod(new StrKit());
 * engine.addSharedStaticMethod(MyKit.class);
 * 
 * #if (notBlank(para))
 *     ....
 * #end
 * 
 * 上面代码中的 notBlank 方法来自 StrKit
 */
public class SharedMethod extends Expr {
	
	private SharedMethodKit sharedMethodKit;
	private String methodName;
	private ExprList exprList;
	
	public SharedMethod(SharedMethodKit sharedMethodKit, String methodName, ExprList exprList, Location location) {
		if (MethodKit.isForbiddenMethod(methodName)) {
			throw new ParseException("Forbidden method: " + methodName, location); 
		}
		this.sharedMethodKit = sharedMethodKit;
		this.methodName = methodName;
		this.exprList = exprList;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Object[] argValues = exprList.evalExprList(scope);
		
		try {
			SharedMethodInfo sharedMethodInfo = sharedMethodKit.getSharedMethodInfo(methodName, argValues);
			if (sharedMethodInfo != null) {
				return sharedMethodInfo.invoke(argValues);
			} else {
				// ShareMethod 相当于是固定的静态的方法，不支持 null safe，null safe 只支持具有动态特征的用法
				throw new TemplateException(Method.buildMethodNotFoundSignature("Shared method not found: ", methodName, argValues), location);
			}
			
		} catch (TemplateException | ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}




