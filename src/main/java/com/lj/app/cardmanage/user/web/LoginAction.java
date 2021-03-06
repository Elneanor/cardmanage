package com.lj.app.cardmanage.user.web;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.lj.app.cardmanage.sysconfig.service.CMPermissionService;
import com.lj.app.cardmanage.user.model.User;
import com.lj.app.cardmanage.user.service.UserService;
import com.lj.app.core.common.audit.CMCode;
import com.lj.app.core.common.security.CMSecurityContext;
import com.lj.app.core.common.security.SecurityConstants;
import com.lj.app.core.common.util.SessionCode;
import com.lj.app.core.common.util.SpringContextHolder;
import com.lj.app.core.common.util.ValidateUtil;
import com.lj.app.core.common.util.des.DesUtil;
import com.lj.app.core.common.web.AbstractBaseAction;
import com.lj.app.core.common.web.Struts2Utils;

@Controller
@Namespace("/")
@Results( {
		@Result(name = SecurityConstants.LOGIN, location = "/login.jsp"),
		@Result(name = SecurityConstants.INDEX, location = "/index.jsp", type = "redirect"),
		@Result(name = SecurityConstants.NOPERMISSION, location = "/jsp/common/nopermission.jsp"),
		@Result(name = SecurityConstants.SYSERROR, location = "/jsp/common/500.jsp") })

@Action("loginAction")
public class LoginAction extends AbstractBaseAction<User> {

	private static Log logger = LogFactory.getLog(LoginAction.class);
	private String loginNo;
	private String pwd;
	
	@Autowired
	private UserService userService;

	public String login() throws Exception {
		if (ValidateUtil.isEmpty(loginNo)|| ValidateUtil.isEmpty(pwd)) {
			return SecurityConstants.LOGIN;
		}

		User user = new User();
		user.setLoginNo(loginNo);
		user.setPwd(pwd);
		user.setLockStatus(CMCode.LOCK_SATE_0);
		
		List<User> userList = userService.findBaseModeList(user);
				
		User loginUser = null;
		if (ValidateUtil.isEmpty(loginNo)) {
			logger.debug("no user loginNo:" + userList + " found.");
			return SecurityConstants.LOGIN;
		} else {
			loginUser = userList.get(0);
			String encryptPwd = DesUtil.encrypt(pwd);
			String dbpwd = loginUser.getPwd();
			if (!encryptPwd.equals(dbpwd)) {
				logger.info("password wrong!!!");
				return SecurityConstants.LOGIN;
			} else if (!loginUser.getLockStatus().equals("0")) {
				logger
						.info("lockstatus is not 0(common status),so login denied!");
				return SecurityConstants.LOGIN;
			}
		}

		HttpSession session2 = Struts2Utils.getSession();// 清空session
		if (session2 != null) {
			session2.invalidate();
		}
		Cookie[] cookies = Struts2Utils.getRequest().getCookies();
		if (cookies != null && cookies.length > 0) {
			cookies[0].setMaxAge(0);
		}

		if (Struts2Utils
				.getSessionAttribute(SecurityConstants.SECURITY_CONTEXT) == null) {
//			UapSecurityContext securityContext = securityApiService
//					.getSecurityContext(loginNo, Struts2Utils
//							.getRequest().getContextPath(),
//							SecurityConstants.DOMAIN_CARD_MANAGE);
			//FIXME :修改信息，修改为查询信息
			
			CMPermissionService cMPermissionService = SpringContextHolder.getBean(CMPermissionService.class);
			logger.info("needValidateUrlSet load......");
			
			CMSecurityContext securityContext = new CMSecurityContext();
			securityContext.setMainAcctId(Long.getLong(String.valueOf(user.getUserId())));
			securityContext.setLoginName(loginUser.getUserName());
			securityContext.setUrls(cMPermissionService.findPermissionUrl());
			
			Struts2Utils.getSession().setAttribute(SecurityConstants.SECURITY_CONTEXT, securityContext);
			Struts2Utils.getSession().setAttribute(SessionCode.MAIN_ACCT,loginUser);
					
		}
		//FIXME 修改为从配置文件读取
		String isPermissionTree = "Y";
		if (isPermissionTree != null && isPermissionTree.equals("Y")) {
			Struts2Utils.getSession().setAttribute("isPermissionTree",
					isPermissionTree);
		} else {
			Struts2Utils.getSession().setAttribute("isPermissionTree", "N");
		}
		Struts2Utils.getSession().setAttribute(SessionCode.MAIN_ACCT,loginUser);
		Struts2Utils.getSession().setAttribute(SessionCode.LOGIN_NAME,user.getLoginNo());
		
		Struts2Utils.getResponse().sendRedirect(Struts2Utils.getRequest().getContextPath() + "/index.jsp");
		return null;
	}

	
	@Override
	public User getModel() {
		return null;
	}


	@Override
	public String list() throws Exception {
		return null;
	}


	@Override
	public String input() throws Exception {
		return null;
	}


	@Override
	public String save() throws Exception {
		return null;
	}


	@Override
	public String delete() throws Exception {
		return null;
	}


	@Override
	protected void prepareModel() throws Exception {
		
	}


	public String getLoginMode() throws Exception {
		String loginMode ="dev";
		logger.warn("==loginMode==" + loginMode);
		if (loginMode != null)
			Struts2Utils.renderText(loginMode);
		else {
			Struts2Utils.renderText("product");
		}
		return null;
	}

	public String logout() {
		if (Struts2Utils.getSessionAttribute(SecurityConstants.SECURITY_CONTEXT) != null) {
			Struts2Utils.getSession().invalidate();
		}

		return SecurityConstants.LOGIN;
	}


	public static Log getLogger() {
		return logger;
	}


	public static void setLogger(Log logger) {
		LoginAction.logger = logger;
	}


	public String getLoginNo() {
		return loginNo;
	}


	public void setLoginNo(String loginNo) {
		this.loginNo = loginNo;
	}


	public String getPwd() {
		return pwd;
	}


	public void setPwd(String pwd) {
		this.pwd = pwd;
	}


	public UserService getUserService() {
		return userService;
	}


	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
