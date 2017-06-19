package com.plumdo.cmd;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

import cn.starnet.activiti.engine.http.HttpRequest;
import cn.starnet.activiti.engine.http.HttpTemplate;

/**
 * 发送微信消息
 * 
 * @author wengwh
 * 
 */
public class SendWeiXinMsgCmd implements Command<String>, Serializable {
	private static final long serialVersionUID = 1L;
	protected HttpTemplate httpTemplate;
	protected String companyId;
	protected String title;
	protected String description;
	protected String url;
	protected List<String> users;
	protected List<String> groups;

	public SendWeiXinMsgCmd(HttpTemplate httpTemplate,String companyId,String title,String description,String url,List<String> users,List<String> groups) {
		this.httpTemplate = httpTemplate;
		this.companyId = companyId;
		this.title = title;
		this.description = description;
		this.url = url;
		this.users = users;
		this.groups = groups;
	}

	@Override
	public String execute(CommandContext commandContext) {
		HttpRequest httpRequest = httpTemplate.getWeChatRequest("WeiXinMessage/toWeiXin");
		httpRequest.setMethod("POST");
		httpRequest.setAsync(true);
		httpRequest.addParameter("company_id", companyId);
		httpRequest.addParameter("title", title);
		httpRequest.addParameter("description", description);
		httpRequest.addParameter("url", url);

		if (users != null) {
			for(String userId : users){
				User user = commandContext.getUserIdentityManager().findUserById(userId);
				if (user != null) {
					httpRequest.addParameter("user_id", user.getEmail());
				}
			}
		}
		
		if (groups != null) {
			for(String groupId : groups){
				Group group = commandContext.getGroupIdentityManager().createNewGroupQuery().groupId(groupId).singleResult();
				if (group != null) {
					httpRequest.addParameter("group_id", group.getId());
				}
			}
		}

		httpTemplate.execute(httpRequest);
		return null;
	}

}