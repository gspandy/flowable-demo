package com.plumdo.cmd;

import java.io.Serializable;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * 执行数据库脚本
 * @author wengwh
 *
 */
public class ExecuteDbResourceCmd implements Command<Void>, Serializable  {

	private static final long serialVersionUID = 1L;
	private String tableName;
	private String operation;
	private String component;
	  
	public ExecuteDbResourceCmd(String tableName,String operation,String component) {
		this.tableName = tableName;
	    this.operation = operation;
	    this.component = component;
	}
	  
	public Void execute(CommandContext commandContext) {
		if(!"create".equals(operation)||!commandContext.getDbSqlSession().isTablePresent(tableName)){
			String databaseType = commandContext.getDbSqlSession().getDbSqlSessionFactory().getDatabaseType();
			String resourceName = getResourceName(databaseType, operation,component);
			commandContext.getDbSqlSession().executeSchemaResource(operation, component, resourceName, false);
		}
	    return null;
	}

	private String getResourceName(String databaseType,String operation,String component){
	    return "cn/starnet/activiti/engine/db/sql/"+operation+"/activiti." + databaseType + "." + operation +"." + component + ".sql";
	}
}