/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
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
package com.edbplus.db.query;


import java.io.Serializable;

/**
 * @program: Order
 * @description: 排序辅助类
 * @author: 杨志佳
 * @create: 2020-04-19 15:05
 **/
public class Order implements Serializable {

    private static final long serialVersionUID = -3078342809727773232L;

    /**
     * 方向
     */
    public enum Direction {

        /** 递增 */
        asc,

        /** 递减 */
        desc
    }

    /** 默认方向 */
    private static final Order.Direction DEFAULT_DIRECTION = Order.Direction.desc;

    /** 属性 */
    private String property;

    /** 方向 */
    private Order.Direction direction = DEFAULT_DIRECTION;

    @Override
    public String toString() {
        return property+" " + direction.name();
    }

    /**
     * 构造方法
     */
    public Order() {
    }

    /**
     * 构造方法
     *
     * @param property
     *            属性
     * @param direction
     *            方向
     */
    public Order(String property, Order.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    /**
     * 返回递增排序
     *
     * @param property
     *            属性
     * @return 递增排序
     */
    public static Order asc(String property) {
        return new Order(property, Order.Direction.asc);
    }

    /**
     * 返回递减排序
     *
     * @param property
     *            属性
     * @return 递减排序
     */
    public static Order desc(String property) {
        return new Order(property, Order.Direction.desc);
    }

    /**
     * 获取属性
     *
     * @return 属性
     */
    public String getProperty() {
        return property;
    }

    /**
     * 设置属性
     *
     * @param property
     *            属性
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * 获取方向
     *
     * @return 方向
     */
    public Order.Direction getDirection() {
        return direction;
    }

    /**
     * 设置方向
     *
     * @param direction
     *            方向
     */
    public void setDirection(Order.Direction direction) {
        this.direction = direction;
    }


}
