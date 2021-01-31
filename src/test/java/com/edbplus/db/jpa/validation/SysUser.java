package com.edbplus.db.jpa.validation;

import com.edbplus.db.annotation.EDbSave;
import com.edbplus.db.annotation.EDbUpdate;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @program: edb
 * @description: 用户表 - 实体
 * @author: MrYang
 * @create:2021-01-14 19:15:25
 **/
@Data
@Table(name = "sys_user")
public class SysUser implements Serializable {

   @Id
   @Column(name="user_id")
   private Long userId;

   /**字段说明:user_account*/
   /**描述说明:用户名;*/
   @NotBlank(message="账号不能为空", groups = {EDbUpdate.class, EDbSave.class,NotBlank.class})
   @Length(message="账号长度最少3位",min = 3, groups = {EDbUpdate.class, EDbSave.class})
   @Column(name="user_account")
   private String userAccount;

   /**字段说明:user_name*/
   /**描述说明:用户中文名;*/
   @Column(name="user_name")
   @NotBlank(message="用户中文名不能为空", groups = {EDbUpdate.class, EDbSave.class})
   @Length(message="用户中文名长度最少3位",min = 3, groups = {EDbUpdate.class, EDbSave.class})
   private String userName;

   /**字段说明:user_pwd*/
   /**描述说明:用户密码;*/
   @Column(name="user_pwd")
   @NotBlank(message="用户密码不能为空", groups = {EDbUpdate.class, EDbSave.class})
   @Length(message="用户密码长度最少6位",min = 6, groups = {EDbUpdate.class, EDbSave.class})
   private String userPwd;

   /**字段说明:user_salt*/
   /**描述说明:用户盐;*/
   @Column(name="user_salt")
   private String userSalt;

   /**字段说明:user_sex*/
   /**描述说明:用户性别;sex;0-待选,1-男,2-女;*/
   @Column(name="user_sex")
   private Integer userSex;

   /**字段说明:user_age*/
   /**描述说明:用户年龄;*/
   @Column(name="user_age")
   private Integer userAge;

   /**字段说明:user_state*/
   /**描述说明:用户状态;state;0-可用,1-禁用;*/
   @Column(name="user_state")
   private Integer userState;

   /**字段说明:create_time*/
   /**描述说明:创建时间*/
   @Column(name="create_time")
   private Date createTime;

   /**字段说明:creater_id*/
   /**描述说明:创建人ID*/
   @Column(name="creater_id")
   private Long createrId;

   /**字段说明:update_time*/
   /**描述说明:更新时间*/
   @Column(name="update_time")
   private Date updateTime;

   /**字段说明:updater_id*/
   /**描述说明:更新人ID*/
   @Column(name="updater_id")
   private Long updaterId;

   /**字段说明:remove_flag*/
   /**描述说明:逻辑删除*/
   @Column(name="remove_flag")
   private Integer removeFlag;


}