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

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import com.jfinal.template.stat.Scope;

/**
 * Map
 * 
 * 1：定义 map 常量
 *   {k1:123, k2:"abc", 'k3':true, "k4":[1,2,3], k5:1+2, 1:12, true:"Y", null:"abc"}
 *   如上所示，map定义的 key 可以为 id 标识符或者 String、Integer、Long、Boolean、null
 *   等常量值 (详见 ExprParser.buildMapEntry(...) 方法)，
 *   右侧的 value 可以是任意的常量与表达式
 * 
 * 2：取值
 *   先将 Map 常量赋值给某个变量： #set(map = {...})
 *   map['k1']
 *   map["k1"]
 *   map[expr]
 *   map.get("k1")
 *   map.k1
 * 
 * 3：不通过中间变量取值
 *   {1:'自买', 2:'跟买'}.get(1)
 *   {1:'自买', 2:'跟买'}[2]
 *   
 *   如上所示，当以下标方式取值时，下标参数可以是 string 与  expr，而 expr 求值以后的值必须也为 string类型
 *   当用 map.k1 这类 field 字段取值形式时，则是使用 id 标识符，而不是 string 形参数
 *   
 *   注意：即便是定义的时候 key 用的是 id 标识符，但在取值时也要用 string 类型下标参数或 expr 求值后为 string
 *        定义时 key 可以使用 id 标识符是为了书写方便，本质上仍然是 string
 * 
 * 3：可创建空 map，如： #(map = {})
 */
public class Map extends Expr {
	
	private LinkedHashMap<Object, Expr> map;
	
	public Map(LinkedHashMap<Object, Expr> map) {
		this.map = map;
	}
	
	public Object eval(Scope scope) {
		LinkedHashMap<Object, Object> valueMap = new LinkedHashMap<Object, Object>(map.size());
		for (Entry<Object, Expr> e : map.entrySet()) {
			valueMap.put(e.getKey(), e.getValue().eval(scope));
		}
		return valueMap;
	}
}






