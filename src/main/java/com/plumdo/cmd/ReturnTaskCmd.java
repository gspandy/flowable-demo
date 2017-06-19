package com.plumdo.cmd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import cn.starnet.activiti.engine.exception.ActivitiConflictException;

/**
 * 回退任务
 * 规则：先查找回退的节点，开始删除分支，根据回退节点创建分支以及任务，对子流程等几种特殊节点做处理
 * @author wengwh
 *
 */
public class ReturnTaskCmd extends NeedsActiveTaskCmd<List<Task>> {

	private static final long serialVersionUID = 1L;
	
	public static final String DELETE_REASON="return";

	  
	public ReturnTaskCmd(String taskId) {
		super(taskId);
	}
	
	public ReturnTaskCmd(String taskId,String userId) {
		super(taskId);
	}

	@Override
	protected List<Task> execute(CommandContext commandContext,TaskEntity task) {
		List<Task> returnTasks = checkSequential(task);
		
		if(CollectionUtils.isEmpty(returnTasks)){
			List<HistoricActivityInstance> previousActivitys = findPreviousActivitys(task);

			checkCouldReturn(task, previousActivitys);
			
			deleteExecution(previousActivitys);

			return createExcution(previousActivitys);
		}else{
			return returnTasks;
		}
	}
	
	/**
	 * 检测是否是串行多实例自身回退(这种情况特殊处理)
	 * @param task
	 * @return
	 */
	private List<Task> checkSequential(TaskEntity task){
		CommandContext commandContext = Context.getCommandContext();
		List<Task> returnTasks = new ArrayList<Task>();
		ExecutionEntity executionEntity = task.getExecution();
        if ("sequential".equals(executionEntity.getActivity().getProperty("multiInstance"))){
        	Integer nrOfCompletedInstances = executionEntity.getVariableLocal("nrOfCompletedInstances",Integer.class);
        	if(nrOfCompletedInstances>0){
				executionEntity.setVariableLocal("nrOfCompletedInstances", nrOfCompletedInstances-1);
				executionEntity.setVariableLocal("loopCounter", nrOfCompletedInstances-1);
				this.deleteTask(task);
				List<HistoricTaskInstance> historicTaskInstances = new HistoricTaskInstanceQueryImpl(commandContext.getProcessEngineConfiguration().getCommandExecutor())
					.processInstanceId(task.getProcessInstanceId())
					.taskDefinitionKey(task.getTaskDefinitionKey())
					.orderByTaskCreateTime().desc()
					.list();
				
	        	Integer nrOfInstances = executionEntity.getVariableLocal("nrOfInstances",Integer.class);
				//应对一直来回的回退和完成，需要遍历同一个分支的非回退任务，并且排除多次分支不同情况
				String excutionId = task.getExecutionId();
				int i = nrOfCompletedInstances;
				for(HistoricTaskInstance historicTaskInstance : historicTaskInstances){
					if(!DELETE_REASON.equals(historicTaskInstance.getDeleteReason())){
						if(excutionId.equals(historicTaskInstance.getExecutionId())){
							if(i == nrOfCompletedInstances){
								commandContext.getHistoryManager().recordActivityStart(executionEntity);
								returnTasks.add(this.createTask(historicTaskInstance, executionEntity));
								break;
							}else{
								i--;
							}
						}else{
							excutionId = historicTaskInstance.getExecutionId();
							i = nrOfInstances-1;
						}
					}
				}
        	}
        }
        return returnTasks;
	}
	
	/**
	 * 没找到回退节点抛异常，后续可增加其他情况
	 * @param commandContext
	 * @param task
	 * @param previousActivitys
	 */
	private void checkCouldReturn(TaskEntity task,List<HistoricActivityInstance> previousActivitys){
		if(previousActivitys==null||previousActivitys.size()==0){
	  		throw new ActivitiConflictException("cannot find previous userTask."); 
		}
	}

	/**
	 * activiti5记录多级并发分支父子关系不对，多级并发的父分支全是流程主干，只能遍历子节点去删除
	 * @param commandContext
	 * @param executionEntity
	 * @param previousActivitys
	 * @return
	 */
	private void deleteExecution(List<HistoricActivityInstance> previousActivitys){
		String processInstanceId =null;
		for(HistoricActivityInstance activityInstance : previousActivitys){
			if(!activityInstance.getProcessInstanceId().equals(processInstanceId)){
				processInstanceId = activityInstance.getProcessInstanceId();
				deleteExecution(activityInstance);
			}
		}
	}
			
	
	public void deleteExecution(HistoricActivityInstance activityInstance){
		ActivityImpl activityImpl = this.getActivity(activityInstance);
		List<HistoricActivityInstance> historicActivityInstances = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
		 		.processInstanceId(activityInstance.getProcessInstanceId()).orderByHistoricActivityInstanceStartTime().desc().list();

		this.deleteExecution(activityImpl.getOutgoingTransitions(), historicActivityInstances,activityInstance.getActivityId());
	}
	
	/**
	 * 遍历删除分支
	 * @param commandContext
	 * @param pvmTransitions
	 * @param historicActivityInstances
	 */
	private void deleteExecution(List<PvmTransition> pvmTransitions,List<HistoricActivityInstance> historicActivityInstances,String actId){
		for(PvmTransition transition : pvmTransitions){
			boolean isEnd=true;
			for(HistoricActivityInstance activityInstance : historicActivityInstances){
				if(transition.getDestination().getId().equals(activityInstance.getActivityId())&&!actId.equals(activityInstance.getActivityId())){
					ExecutionEntity preExecutionEntity = Context.getCommandContext().getExecutionEntityManager().findExecutionById(activityInstance.getExecutionId());
					if(preExecutionEntity!=null&&activityInstance.getActivityId().equals(preExecutionEntity.getActivityId())){
						//子流程并行,找到父分支删除
						if(preExecutionEntity.getParent()!=null 
								&& activityInstance.getActivityId().equals(preExecutionEntity.getParent().getActivityId())){
							preExecutionEntity = preExecutionEntity.getParent();
						}
						
						this.deleteChildExecution(preExecutionEntity.getId());
						this.deleteExecution(preExecutionEntity);
				
						//判断上级是并行分支,且无节点，存在是回退到并行分支无法加入网关节点
						ExecutionEntity parentExecution = preExecutionEntity.getParent();
						if(parentExecution!=null && parentExecution.getActivity()==null && parentExecution.isConcurrent()){
							this.deleteExecution(parentExecution);
						}
						//如果清理节点就无需再清理它的下一级节点
						isEnd=true;
					}else{
						isEnd=false;
					}
					
					//内嵌子流程删除的时候，手动设置结束时间，引擎分支并不会单独记录
					if("subProcess".equals(activityInstance.getActivityType())){
						 for (PvmActivity activity : transition.getDestination().getActivities()) {
							 if("startEvent".equals(activity.getProperty("type"))){
								 this.deleteExecution(activity.getOutgoingTransitions(), historicActivityInstances,activity.getId());
							 }
						 }

						 HistoricActivityInstanceEntity activityInstanceEntity = (HistoricActivityInstanceEntity) activityInstance;
						 
						 activityInstanceEntity.setEndTime(new Date());
						 activityInstanceEntity.setDurationInMillis(activityInstanceEntity.getEndTime().getTime()-activityInstanceEntity.getStartTime().getTime());
						 if(preExecutionEntity!=null){
							this.deleteExecution(preExecutionEntity,true);
						 }
					}
					break;
				}
			 }
			 if(!isEnd){
				 this.deleteExecution(transition.getDestination().getOutgoingTransitions(), historicActivityInstances,actId);
			 }
		 }
	}
	
	
	/**
	 * 遍历删除子分支(自带的分支变量删除api，无法设置正确的任务deleteReason)
	 * @param commandContext
	 * @param executionId
	 */
	private void deleteChildExecution(String executionId){
		//删除callActivity类型的子流程
		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(executionId);
		if(executionEntity!=null){
			this.deleteChildExecution(executionEntity.getId());
			commandContext.getExecutionEntityManager().deleteProcessInstance(executionEntity.getProcessInstanceId(), DELETE_REASON);
			this.deleteExecution(executionEntity);
		}
        
		//删除父子关系，可能是并发等
		List<ExecutionEntity> executionEntities = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(executionId);
		for(ExecutionEntity entity :executionEntities){
			this.deleteChildExecution(entity.getId());
			this.deleteExecution(entity,true);
		}
	}
	
	private void deleteExecution(ExecutionEntity executionEntity){
		this.deleteExecution(executionEntity, false);
	}
	/**
	 * 删除分支
	 * @param commandContext
	 * @param executionEntity
	 */
	private void deleteExecution(ExecutionEntity executionEntity,boolean isForce){
		CommandContext commandContext = Context.getCommandContext();
        List<TaskEntity> tasks = commandContext.getTaskEntityManager().findTasksByExecutionId(executionEntity.getId());
        for(TaskEntity taskEntity :tasks){
        	this.deleteTask(taskEntity);
        }
        
        executionEntity.setDeleteReason(DELETE_REASON);

        long actNum = 0;
        //如果分支是在内嵌子流程内部，判断分支是否是内嵌子流程的主干
        if(executionEntity.getActivity()!=null && executionEntity.getActivity().getParentActivity()!=null){
        	List<HistoricActivityInstance> historicActivityInstances= new HistoricActivityInstanceQueryImpl(commandContext)
		        	.activityId(executionEntity.getActivity().getParentActivity().getId())
		        	.executionId(executionEntity.getId())
		        	.unfinished().list();
        	if(isForce){
        		for(HistoricActivityInstance activityInstance : historicActivityInstances){
        			HistoricActivityInstanceEntity activityInstanceEntity = (HistoricActivityInstanceEntity) activityInstance;
				 
        			activityInstanceEntity.setEndTime(new Date());
        			activityInstanceEntity.setDurationInMillis(activityInstanceEntity.getEndTime().getTime()-activityInstanceEntity.getStartTime().getTime());
        		}
        	}else{
        		actNum = historicActivityInstances.size();
        	}
        }
        
        //如果分支不是流程实例主干，删除分支同时删除分支变量
        if(actNum==0 && !executionEntity.getId().equals(executionEntity.getProcessInstanceId())){
        	executionEntity.deleteVariablesInstanceForLeavingScope();
    		commandContext.getExecutionEntityManager().delete(executionEntity);
        }else{
        	executionEntity.setActivity(null);
        }

        HistoricActivityInstanceEntity activityInstanceEntity = commandContext.getHistoryManager().findActivityInstance(executionEntity);
        if(activityInstanceEntity!=null && isParallel(activityInstanceEntity.getActivityType())){
        	activityInstanceEntity.setEndTime(activityInstanceEntity.getStartTime());
        	activityInstanceEntity.setDurationInMillis(-1l);
        }else{
    		commandContext.getHistoryManager().recordActivityEnd(executionEntity);
        }
        
	}
	
	/**
	 * 删除任务，同时修改历史节点的结束时间
	 * @param commandContext
	 * @param taskEntity
	 */
	private void deleteTask(TaskEntity taskEntity){
		CommandContext commandContext = Context.getCommandContext();
		//触发任务完成事件
		taskEntity.fireEvent(TaskListener.EVENTNAME_COMPLETE);
		
		commandContext.getTaskEntityManager().deleteTask(taskEntity,DELETE_REASON, false);

		ExecutionEntity executionEntity = taskEntity.getExecution();

		commandContext.getHistoryManager().recordActivityEnd(executionEntity);
		
		executionEntity.removeTask(taskEntity);
		
	}
	
	/**
	 * 处理分支，分支就是流程的指针
	 * 情况：
	 * 1.回退节点多个，聚合节点退回到并行
	 * 2.回退节点一个，同时有并行的运行节点，并行节点退回到聚合
	 * 3.内嵌子流程回退，分支要删除
	 * 4.回退节点是子流程，创建分支
	 * 5.外嵌子流程回退，子流程要删除，
	 * 6.回退节点是外嵌子流程
	 * 7.没有分支的回退,最基本
	 * 8.多实例（并行）回退到普通节点
	 * 9.普通节点回退到多实例（并行）
	 * 10.多实例（串行）回退到普通节点
	 * 11.普通节点回退到多实例（串行）
	 * 12.多实例（串行）本身回退（比较特殊不做统一处理，直接在开始就判断掉）
	 * 13.内嵌子流程多实例（并行情况回退只能回退到首节点，无法到最后节点，串行情况，无法自身回退，直接到上一节点）
	 * 14.外嵌子流程多实例（并行，串行，串行自身都可以）
	 * 
	 * @param commandContext
	 * @param taskEntity
	 * @param previousActivitys
	 */
	private List<Task> createExcution(List<HistoricActivityInstance> previousActivitys){
		List<Task> returnTasks = new ArrayList<Task>();
		
		CommandContext commandContext = Context.getCommandContext();
		//外嵌子流程的分支map
		Map<String,ExecutionEntity> superExecutionMap = new HashMap<String, ExecutionEntity>();
		//流程实例id，节点id，分支实体map
		Map<String,Map<String,ExecutionEntity>> executionMap = new HashMap<String, Map<String,ExecutionEntity>>();
		//获取主干，根据是否并行，和子流程来创建分支
		
		for(HistoricActivityInstance historicActivityInstance : previousActivitys){
			ExecutionEntity executionEntity = getParentExecutionEntity(historicActivityInstance,executionMap);
			if(executionEntity==null){
				executionEntity = new ExecutionEntity();
				executionEntity.setId(historicActivityInstance.getProcessInstanceId());
				executionEntity.setProcessInstance(executionEntity);
				executionEntity.setSuperExecution(superExecutionMap.get(historicActivityInstance.getProcessInstanceId()));
				ProcessDefinitionEntity processDefinition = commandContext.getProcessDefinitionEntityManager().findProcessDefinitionById(historicActivityInstance.getProcessDefinitionId());
				executionEntity.setProcessDefinition(processDefinition);
				commandContext.getExecutionEntityManager().insert(executionEntity);
				this.updateHistoricProcess(historicActivityInstance.getProcessInstanceId());
				//如果要创建历史流程，直接设置历史流程实例ID，关联历史任务
				if(!executionMap.containsKey(historicActivityInstance.getProcessInstanceId())){
					executionMap.put(historicActivityInstance.getProcessInstanceId(), new HashMap<String, ExecutionEntity>());
				}
				executionMap.get(historicActivityInstance.getProcessInstanceId()).put(null, executionEntity);
			}
			
			//判断如果是并行，就创建分支
			if(this.isConcurrent(previousActivitys, historicActivityInstance)){
				this.setExecutionProperty(executionEntity, false, false, 0, executionEntity.getActivity());
				executionEntity = executionEntity.createExecution();
				this.setExecutionProperty(executionEntity, true,  true, 7, null);
			}
			
			ActivityImpl activity = this.getActivity(historicActivityInstance);
			
			//判断是并行多实例情况Parallel
			if(activity.getProperty("multiInstance")!=null){
				this.setExecutionProperty(executionEntity, false, executionEntity.isConcurrent(), 0, executionEntity.getActivity());
				executionEntity = executionEntity.createExecution();
			}
			
			//子流程的情况，创建分支
			if("callActivity".equals(historicActivityInstance.getActivityType())){
				//多实例情况，子流程是要回退到历史的流程上，所以无法直接使用executionEntity.executeActivity(activity)，执行节点，人工回退
				if("parallel".equals(activity.getProperty("multiInstance"))){
					List<HistoricActivityInstance> hisActInsts = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
					 		.processInstanceId(historicActivityInstance.getProcessInstanceId())
					 		.activityId(historicActivityInstance.getActivityId())
					 		.activityType(historicActivityInstance.getActivityType())
					 		.orderByHistoricActivityInstanceStartTime().desc().list();
					
					int loopCounter = 0;
					for(HistoricActivityInstance activityInstance : hisActInsts){
						if(activityInstance.getEndTime().after(historicActivityInstance.getStartTime())){
							this.setExecutionProperty(executionEntity, false, false, 0, activity);
							ExecutionEntity childrenExecution = executionEntity.createExecution();

							this.setExecutionProperty(childrenExecution, true,  true, 7, activity);
							superExecutionMap.put(activityInstance.getCalledProcessInstanceId(), childrenExecution);
							//设置子分支的引擎变量，没有设置遍历的list变量
							childrenExecution.setVariableLocal("loopCounter", loopCounter++);
							//多实例，节点规则，第一个节点是父分支的id
							if(activityInstance.getId().equals(historicActivityInstance.getId())){
								this.updateCallActity(activity,activityInstance, executionEntity);
							}else{
								this.updateCallActity(activity,activityInstance, childrenExecution);
							}
							
						}
					}
					//设置多实例引擎变量,分支本地变量
					executionEntity.setVariableLocal("nrOfInstances", loopCounter);
					executionEntity.setVariableLocal("nrOfActiveInstances", loopCounter);
					executionEntity.setVariableLocal("nrOfCompletedInstances", 0);
					
				}else if("sequential".equals(activity.getProperty("multiInstance"))){
					//串行多实例子流程，并没有比正规的子流程多创建一个分支，规则特殊
					//这里包括了串行子流程本身，以及回退的节点是上一节点（2种情况）
					this.setExecutionProperty(executionEntity, true,  false, 0, activity);
					
					HistoricVariableInstance historicVariableInstance = new HistoricVariableInstanceQueryImpl(commandContext)
						.executionId(historicActivityInstance.getExecutionId())
						.variableName("nrOfInstances").singleResult();

					Integer nrOfInstances = (Integer) historicVariableInstance.getValue();
					
					historicVariableInstance = new HistoricVariableInstanceQueryImpl(commandContext)
						.executionId(historicActivityInstance.getExecutionId())
						.variableName("nrOfCompletedInstances").singleResult();
					
					Integer nrOfCompletedInstances = (Integer) historicVariableInstance.getValue();
					
					executionEntity.setVariableLocal("nrOfInstances", nrOfInstances);
					executionEntity.setVariableLocal("nrOfCompletedInstances", nrOfCompletedInstances-1);
					executionEntity.setVariableLocal("nrOfActiveInstances", 1);
					executionEntity.setVariableLocal("loopCounter", nrOfCompletedInstances-1);
					
					superExecutionMap.put(historicActivityInstance.getCalledProcessInstanceId(), executionEntity);
					this.updateCallActity(activity, historicActivityInstance, executionEntity);
				}else{
					this.setExecutionProperty(executionEntity, false,  executionEntity.isConcurrent(), 0, null);
					executionEntity = executionEntity.createExecution();

					this.setExecutionProperty(executionEntity, true,  false, 0, activity);
					superExecutionMap.put(historicActivityInstance.getCalledProcessInstanceId(), executionEntity);
					this.updateCallActity(activity, historicActivityInstance, executionEntity);
				}
			}else if("subProcess".equals(historicActivityInstance.getActivityType())){
				//内嵌子流程并行多实例(无法记录历史的父子关系)回退到首个节点
				if("parallel".equals(activity.getProperty("multiInstance"))){
					executionEntity.executeActivity(activity);
					Task task = executionEntity.getExecutions().get(0).getExecutions().get(0).getTasks().get(0);
					List<HistoricTaskInstance> historicTaskInstances = new HistoricTaskInstanceQueryImpl(commandContext.getProcessEngineConfiguration().getCommandExecutor())
							.processInstanceId(historicActivityInstance.getProcessInstanceId())
							.taskDefinitionKey(task.getTaskDefinitionKey())
							.orderByHistoricTaskInstanceEndTime().desc()
							.list();
					int taskNum = 0;
					for(int i=0;i<executionEntity.getExecutions().size();i++){
						for(;taskNum<historicTaskInstances.size();taskNum++){
							if(!DELETE_REASON.equals(historicTaskInstances.get(taskNum).getDeleteReason())){
								executionEntity.getExecutions().get(i).getExecutions().get(0).getTasks().get(0).setAssignee(historicTaskInstances.get(taskNum).getAssignee());
								returnTasks.add(executionEntity.getExecutions().get(i).getExecutions().get(0).getTasks().get(0));
								taskNum++;
								break;
							}
						}
					}
				}else if("sequential".equals(activity.getProperty("multiInstance"))){
					//subProcess，自身多实例回退不支持
					this.setExecutionProperty(executionEntity, false,  executionEntity.isConcurrent(), 0, activity);
					
					HistoricVariableInstance historicVariableInstance = new HistoricVariableInstanceQueryImpl(commandContext)
						.executionId(historicActivityInstance.getExecutionId())
						.variableName("nrOfInstances").singleResult();

					Integer nrOfInstances = (Integer) historicVariableInstance.getValue();
					
					historicVariableInstance = new HistoricVariableInstanceQueryImpl(commandContext)
						.executionId(historicActivityInstance.getExecutionId())
						.variableName("nrOfCompletedInstances").singleResult();
					
					Integer nrOfCompletedInstances = (Integer) historicVariableInstance.getValue();
					
					executionEntity.setVariableLocal("nrOfInstances", nrOfInstances);
					executionEntity.setVariableLocal("nrOfCompletedInstances", nrOfCompletedInstances-1);
					executionEntity.setVariableLocal("nrOfActiveInstances", 1);
					executionEntity.setVariableLocal("loopCounter", nrOfCompletedInstances-1);
					
					commandContext.getHistoryManager().recordActivityStart(executionEntity);
					
					if(!executionMap.containsKey(historicActivityInstance.getProcessInstanceId())){
						executionMap.put(historicActivityInstance.getProcessInstanceId(), new HashMap<String, ExecutionEntity>());
					}
					executionMap.get(historicActivityInstance.getProcessInstanceId()).put(historicActivityInstance.getActivityId(), executionEntity);
					
				}else{
					this.setExecutionProperty(executionEntity, false,  executionEntity.isConcurrent(), 0, null);
					executionEntity = executionEntity.createExecution();	
					this.setExecutionProperty(executionEntity, false,  executionEntity.isConcurrent(), 0, activity);
					commandContext.getHistoryManager().recordActivityStart(executionEntity);
	
					if(!executionMap.containsKey(historicActivityInstance.getProcessInstanceId())){
						executionMap.put(historicActivityInstance.getProcessInstanceId(), new HashMap<String, ExecutionEntity>());
					}
					executionMap.get(historicActivityInstance.getProcessInstanceId()).put(historicActivityInstance.getActivityId(), executionEntity);
				}
			}else{
				//执行节点
				executionEntity.executeActivity(activity);
				
				if("parallel".equals(activity.getProperty("multiInstance"))){
					//并行多实例回退，查询同一时刻的回退任务设置负责人，后面加入候选人
					List<HistoricTaskInstance> historicTaskInstances = new HistoricTaskInstanceQueryImpl(commandContext.getProcessEngineConfiguration().getCommandExecutor())
								.processInstanceId(historicActivityInstance.getProcessInstanceId())
								.taskDefinitionKey(historicActivityInstance.getActivityId())
								.orderByHistoricTaskInstanceEndTime().desc()
								.list();
					int taskNum=0;
					for(int i=0;i<executionEntity.getExecutions().size();i++){
						executionEntity.getExecutions().get(i).setActive(true);
						for(;taskNum<historicTaskInstances.size();taskNum++){
							if(!DELETE_REASON.equals(historicTaskInstances.get(taskNum).getDeleteReason())){
								executionEntity.getExecutions().get(i).getTasks().get(0).setAssignee(historicTaskInstances.get(taskNum).getAssignee());
								returnTasks.add(executionEntity.getExecutions().get(i).getTasks().get(0));
								taskNum++;
								break;
							}
						}
					}
				}else if("sequential".equals(activity.getProperty("multiInstance"))){
					//串行多实例回退，回退到串行最后一个执行，设置多实例的引擎变量
					Integer nrOfInstances = executionEntity.getVariableLocal("nrOfInstances",Integer.class);
					executionEntity.setVariableLocal("nrOfCompletedInstances", nrOfInstances-1);
					executionEntity.setVariableLocal("loopCounter", nrOfInstances-1);
					executionEntity.setActive(true);
					executionEntity.getTasks().get(0).setAssignee(historicActivityInstance.getAssignee());
					returnTasks.add(executionEntity.getTasks().get(0));
				}else{
					//普通任务就设置负责人
					executionEntity.setActive(true);
					executionEntity.getTasks().get(0).setAssignee(historicActivityInstance.getAssignee());
					returnTasks.add(executionEntity.getTasks().get(0));
				}
			}
		}
		return returnTasks;
	}
	
	/**
	 * 设置分支的属性
	 * @param executionEntity
	 * @param isActive
	 * @param isConcurrent
	 * @param cachedEntityState
	 * @param activityImpl
	 */
	private void setExecutionProperty(ExecutionEntity executionEntity,boolean isActive,boolean isConcurrent,int cachedEntityState ,ActivityImpl activityImpl){
		executionEntity.setActive(isActive);
		executionEntity.setScope(!isConcurrent);
		executionEntity.setConcurrent(isConcurrent);
		executionEntity.setCachedEntityState(cachedEntityState);
		executionEntity.setActivity(activityImpl);
	}
	
	
	/**
	 * 创建任务，同时创建历史节点
	 * @param commandContext
	 * @param historicActivityInstance
	 * @param executionEntity
	 */
	private TaskEntity createTask(HistoricTaskInstance historicTaskInstance,ExecutionEntity executionEntity){
		CommandContext commandContext = Context.getCommandContext();
		TaskEntity task = TaskEntity.create(new Date());
        task.setProcessDefinitionId(historicTaskInstance.getProcessDefinitionId());
        task.setExecutionId(executionEntity.getId());
        task.setAssigneeWithoutCascade(historicTaskInstance.getAssignee());
        task.setParentTaskIdWithoutCascade(historicTaskInstance.getParentTaskId());
        task.setNameWithoutCascade(historicTaskInstance.getName());
        task.setTaskDefinitionKey(historicTaskInstance.getTaskDefinitionKey());
        task.setPriority(historicTaskInstance.getPriority());
        task.setProcessInstanceId(historicTaskInstance.getProcessInstanceId());
        task.setDescriptionWithoutCascade(historicTaskInstance.getDescription());
        commandContext.getTaskEntityManager().insert(task);
        // 创建HistoricTaskInstance
        commandContext.getHistoryManager().recordTaskCreated(task, executionEntity);
        commandContext.getHistoryManager().recordTaskId(task);
        // 更新ACT_HI_ACTIVITY里的assignee字段
        commandContext.getHistoryManager().recordTaskAssignment(task);
        
        return task;
	}
	
	
	/**
	 * 获取主干分支
	 * @param commandContext
	 * @param historicActivityInstance
	 * @param executionMap
	 * @return
	 */
	private ExecutionEntity getParentExecutionEntity(HistoricActivityInstance historicActivityInstance,Map<String,Map<String,ExecutionEntity>> executionMap){
		CommandContext commandContext = Context.getCommandContext();
		ExecutionEntity executionEntity = null;
		ActivityImpl activityImpl = this.getActivity(historicActivityInstance);
		//内嵌子流程
		if(activityImpl.getParentActivity()!=null){
			//内嵌子流程外部回退
			if(executionMap.containsKey(historicActivityInstance.getProcessInstanceId())){
				executionEntity = executionMap.get(historicActivityInstance.getProcessInstanceId()).get(activityImpl.getParentActivity().getId());
			}
			//内嵌子流程内部回退
			if(executionEntity==null){
				List<HistoricActivityInstance> parentHisActivities = new HistoricActivityInstanceQueryImpl(commandContext)
			 		.processInstanceId(historicActivityInstance.getProcessInstanceId())
			 		.activityId(activityImpl.getParentActivity().getId()).unfinished().list();
				if(parentHisActivities!=null && parentHisActivities.size()>0){
					if(parentHisActivities.size()==1){
						executionEntity = commandContext.getExecutionEntityManager().findExecutionById(parentHisActivities.get(0).getExecutionId());
					}else{
						//并行多实例,根据回退节点的
						 TaskEntity task = commandContext.getTaskEntityManager().findTaskById(taskId);
						 executionEntity = task.getExecution();
						 while(executionEntity.getParent()!=null
								 &&!activityImpl.getParentActivity().getId().equals(executionEntity.getParent().getActivityId())){
							 executionEntity = executionEntity.getParent();
						 }
						 //并行多实例，删除内部任务，需要在创建一个分支
						 if(DELETE_REASON.equals(executionEntity.getDeleteReason())){
							Map<String,Object> variables = executionEntity.getVariablesLocal();
							executionEntity = executionEntity.getParent().createExecution();
							executionEntity.setVariablesLocal(variables);
						 }
					}
					//由于多实例的变量设置，需要复制
					//判断是否删除，如果删除就创建一个新的分支
					/*if(!StringUtils.isEmpty(executionEntity.getDeleteReason())){
						Map<String,Object> variables = executionEntity.getVariablesLocal();
						executionEntity = executionEntity.getParent().createExecution();
						if(activityImpl.getParentActivity().getProperty("multiInstance")!=null){
							executionEntity.setVariablesLocal(variables);
						}
					}*/
				}
			}
		}else{
			ExecutionQueryImpl executionQuery = new ExecutionQueryImpl(commandContext);
			executionQuery.executionId(historicActivityInstance.getProcessInstanceId());
			executionEntity = (ExecutionEntity) executionQuery.singleResult();
			if(executionEntity==null){
				if(executionMap.containsKey(historicActivityInstance.getProcessInstanceId())){
					executionEntity = executionMap.get(historicActivityInstance.getProcessInstanceId()).get(null);
				}
			}
		}
		return executionEntity;
	}
	
	
	/**
	 * 修改历史流程信息
	 * @param commandContext
	 * @param processInstanceId
	 */
	private void updateHistoricProcess(String processInstanceId){
		CommandContext commandContext = Context.getCommandContext();
		HistoricProcessInstanceEntity historicProcessInstanceEntity = commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(processInstanceId);
		
		historicProcessInstanceEntity.setEndActivityId(null);
		historicProcessInstanceEntity.setEndTime(null);
		historicProcessInstanceEntity.setDeleteReason(null);
		historicProcessInstanceEntity.setDurationInMillis(null);
		
		commandContext.getDbSqlSession().update(historicProcessInstanceEntity);
	}
	
	/**
	 * 修改历史CallActity
	 * @param commandContext
	 * @param historicActivityInstanceEntity
	 * @param executionEntity
	 */
	private void updateCallActity(ActivityImpl activity,HistoricActivityInstance historicActivityInstance,ExecutionEntity executionEntity){

		CommandContext commandContext = Context.getCommandContext();
		executionEntity.setActivity(activity);
		commandContext.getHistoryManager().recordActivityStart(executionEntity);
		commandContext.getHistoryManager().findActivityInstance(executionEntity).setCalledProcessInstanceId(historicActivityInstance.getCalledProcessInstanceId());
	
	}
	
	
	/**
	 * 判断是否是并行
	 * @param previousActivitys
	 * @param instance
	 * @return
	 */
	private boolean isConcurrent(List<HistoricActivityInstance> previousActivitys,HistoricActivityInstance instance){
		ActivityImpl activityImpl = this.getActivity(instance);
		for(HistoricActivityInstance historicActivityInstance : previousActivitys){
			if(!historicActivityInstance.getId().equals(instance.getId())){
				if(historicActivityInstance.getProcessInstanceId().equals(instance.getProcessInstanceId())){
					if(!historicActivityInstance.getExecutionId().equals(instance.getExecutionId())){
						//判断是否属于同一个内嵌子流程
						if(activityImpl.getParentActivity()!=null){
							ActivityImpl parentActity = this.getActivity(historicActivityInstance).getParentActivity();
							if(parentActity==activityImpl.getParentActivity()){
								return true;
							}
						}else if("subProcess".equals(instance.getActivityType())){
							//如果是内嵌子流程本身节点，非嵌套即可
							ActivityImpl parentActity = this.getActivity(historicActivityInstance);
							if(!this.isParentActivity(parentActity, activityImpl)){
								return true;
							}
						}else{
							return true;
						}
					}
				}
			}
		}
		//需要加入层级判断
		if(activityImpl.getParentActivity()==null&&!"subProcess".equals(instance.getActivityType())){
			ExecutionQueryImpl executionQuery = new ExecutionQueryImpl(Context.getCommandContext());
			executionQuery.processInstanceId(instance.getProcessInstanceId());
			List<Execution> executions = executionQuery.list();
			for(Execution execution : executions){
				ExecutionEntity executionEntity = (ExecutionEntity) execution;
				if(StringUtils.isEmpty(executionEntity.getDeleteReason())
						&& executionEntity.isConcurrent()){
					return true;
				}
			}
		}
		
		return false;
	}	
	
	
	private boolean isParentActivity(ActivityImpl parentActity,ActivityImpl actity){
		if(parentActity.getParentActivity()!=null){
			if(parentActity.getParentActivity()==actity){
				return true;
			}else{
				return isParentActivity(parentActity.getParentActivity(), actity);
			}
		}
		return false;
	}
	
		
	/**
	 * 获取流程定义的节点信息
	 * @param commandContext
	 * @param historicActivityInstanceEntity
	 * @return
	 */
	public ActivityImpl getActivity(HistoricActivityInstance historicActivityInstance) {
		ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
				historicActivityInstance.getProcessDefinitionId()).execute(Context.getCommandContext());

        return processDefinitionEntity.findActivity(historicActivityInstance.getActivityId());
    }
	
	
	/**
	 * 查找历史任务
	 * @param commandContext
	 * @param historicActivityInstance
	 * @return
	 */
	public HistoricTaskInstance findPreviousHistoricTask(HistoricActivityInstance historicActivityInstance) {
		HistoricTaskInstanceQueryImpl historicTaskInstanceQueryImpl = new HistoricTaskInstanceQueryImpl();
    	historicTaskInstanceQueryImpl.taskDefinitionKey(historicActivityInstance.getActivityId());
        historicTaskInstanceQueryImpl.taskId(historicActivityInstance.getTaskId());
        historicTaskInstanceQueryImpl.processInstanceId(historicActivityInstance.getProcessInstanceId());
        historicTaskInstanceQueryImpl.setFirstResult(0);
        historicTaskInstanceQueryImpl.setMaxResults(1);
        historicTaskInstanceQueryImpl.orderByTaskCreateTime().desc();

        HistoricTaskInstance historicTaskInstance = Context.getCommandContext()
                .getHistoricTaskInstanceEntityManager()
                .findHistoricTaskInstancesByQueryCriteria(
                        historicTaskInstanceQueryImpl).get(0);

        return historicTaskInstance;
    }
		 
	/**
	 * 查找回退节点
	 * @param commandContext
	 * @param taskEntity
	 * @return
	 */
	public List<HistoricActivityInstance> findPreviousActivitys(TaskEntity taskEntity) {
		List<HistoricActivityInstance> historicActivityInstances = this.getHisActInsts(taskEntity.getProcessInstanceId());
		 
		ProcessDefinitionEntity processDefinition = new GetDeploymentProcessDefinitionCmd(taskEntity.getProcessDefinitionId()).execute(Context.getCommandContext());
		 
		List<PvmTransition> pvmTransitions = processDefinition.findActivity(taskEntity.getTaskDefinitionKey()).getIncomingTransitions();
		 
		List<HistoricActivityInstance> previousActivitys = new ArrayList<HistoricActivityInstance>();
		 
		findNearestUserTask(historicActivityInstances, pvmTransitions, previousActivitys,null);

		return previousActivitys;
	}
	/**
	 *  包容网关出来，结束
	 * @param processInstanceId
	 * @return
	 */
	private List<HistoricActivityInstance> getHisActInsts(String processInstanceId){
		List<HistoricActivityInstance> hisActInsts = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
	 		.processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().desc().list();
		
        List<String> parallActIds = new ArrayList<String>();
    	List<HistoricActivityInstance> newHisActInsts = new ArrayList<HistoricActivityInstance>();
    	//过滤掉回退之前的节点，防止出现回退多个
        for(HistoricActivityInstance hisActInst : hisActInsts){
        	if(!parallActIds.contains(hisActInst.getActivityId())){
        		newHisActInsts.add(hisActInst);
        		//如果包容分支是回退的情况，后续的就无须再加入改节点
        		if(!isParallel(hisActInst.getActivityType()) || isReturnGateway(hisActInst)){
        			parallActIds.add(hisActInst.getActivityId());
        		}
        	}
        }
        return newHisActInsts;
	}
	 
	/**
	 * 仅限包容网关的一种特殊情况：
	 * （如果大于2条分支中，一条分支全部处理完，宁外一条分支退回到聚合点，开始新的并发，但是并没有包括上一次全部处理完的那个分支，就需要加入那个回退的网关做判断，否则会导致新一次的并发结束后回退把上上次的分支也回退了）
	 */
	private boolean isInclusive(List<HistoricActivityInstance> historicActivityInstances,HistoricActivityInstance preActivity,List<PvmTransition> pvmTransitions,HistoricActivityInstance activityInstance,List<HistoricActivityInstance> previousActivitys){
		//过滤掉回退的网关，加入该网关是为了过滤掉改网关回退之前的上一级节点
		if(isReturnGateway(activityInstance)){
			return false;
		}else if(preActivity!=null && pvmTransitions.size()>1 && isParallel(preActivity.getActivityType())){
			//判断上一级节点非空，并且是并发的结束阶段
			//如果已经在回退节点的集合里面就不做遍历
			if(isContains(previousActivitys, activityInstance)){
				return false;
			}else{
				//如果是用户任务分支ID必须是一样的
				if("userTask".equals(activityInstance.getActivityType())){
					if(!activityInstance.getExecutionId().equals(preActivity.getExecutionId())){
						return false;
					}
				}else{
					//判断节点是否是回退网关之前的节点，如果是不遍历。
					//如果是多个子流程的情况，且是多级分支回归到同一个聚合点，有一种情况，会出现有的节点无法回退
					for(HistoricActivityInstance instance : historicActivityInstances){
						if(instance.getActivityId().equals(preActivity.getActivityId())&& isReturnGateway(instance)){
							if(instance.getStartTime().after(activityInstance.getStartTime())){
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	/**
	 * 查找回退节点，多类型，多情况遍历
	 * 1.如果节点是subProcess，内嵌子流程继续遍历子节点
	 * 2.如果节点是callActivity，继续遍历外嵌子流程
	 * 3.如果节点是startEvent，遍历是否有父流程
	 * 
	 * @param commandContext
	 * @param historicActivityInstances
	 * @param pvmTransitions
	 * @param previousActivitys
	 */
	private void findNearestUserTask(List<HistoricActivityInstance> historicActivityInstances,List<PvmTransition> pvmTransitions,List<HistoricActivityInstance> previousActivitys,HistoricActivityInstance previousActivity){
		CommandContext commandContext = Context.getCommandContext();
		if(historicActivityInstances!=null&&pvmTransitions!=null){
			 for(int i=0;i<historicActivityInstances.size();i++){
				 HistoricActivityInstance activityInstance = historicActivityInstances.get(i);
				 for(PvmTransition transition : pvmTransitions){
					 //开始节点，遍历父节点，可能是内嵌子流程回退到父流程,由于activiti历史节点没有记录子流程的开始节点
					 if("startEvent".equals(transition.getSource().getProperty("type"))){
						 //内嵌子流程subProcess
						 if(transition.getSource().getParent()!=null){
							 if(transition.getSource().getParent() instanceof ActivityImpl){
								 findNearestUserTask(historicActivityInstances, ((ActivityImpl)transition.getSource().getParent()).getIncomingTransitions(), previousActivitys,null);
							 }
						 }
					 }
					 if(transition.getSource().getId().equals(activityInstance.getActivityId()) && isInclusive(historicActivityInstances,previousActivity, pvmTransitions, activityInstance,previousActivitys)){
						 //判断包含网关，回退的节点的运行ID是一样
						if("userTask".equals(activityInstance.getActivityType())){
							if(!isContains(previousActivitys, activityInstance)){
								HistoricTaskInstance historicTaskInstance = this.findPreviousHistoricTask(activityInstance);
								if(!DELETE_REASON.equals(historicTaskInstance.getDeleteReason())){
									previousActivitys.add(activityInstance);
								}
							}
						}else if("callActivity".equals(activityInstance.getActivityType())){
							if(!isContains(previousActivitys, activityInstance)){
								ActivityImpl activity =this.getActivity(activityInstance);
								List<String> processInstanceIds = new ArrayList<String>();
								//多实例并行的情况查找相同的子流程节点
								if("parallel".equals(activity.getProperty("multiInstance"))){
									List<HistoricActivityInstance> hisActInsts = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
								 		.processInstanceId(activityInstance.getProcessInstanceId())
								 		.activityId(activityInstance.getActivityId())
								 		.activityType(activityInstance.getActivityType())
								 		.orderByHistoricActivityInstanceStartTime().desc().list();
									for(HistoricActivityInstance historicActivityInstance : hisActInsts){
										//需要过滤一些子流程回退过的重新启动子流程
										if(historicActivityInstance.getEndTime().after(activityInstance.getStartTime())){
											processInstanceIds.add(historicActivityInstance.getCalledProcessInstanceId());
										}
									}
								}else{
									processInstanceIds.add(activityInstance.getCalledProcessInstanceId());
								}
								
								for(String processInstanceId : processInstanceIds){
									HistoricProcessInstanceEntity historicProcessInstanceEntity = commandContext.getHistoricProcessInstanceEntityManager().findHistoricProcessInstance(processInstanceId);
									
									if(!DELETE_REASON.equals(historicProcessInstanceEntity.getDeleteReason())){
										//只添加一个调用节点
										if(processInstanceId.equals(activityInstance.getCalledProcessInstanceId())){
											previousActivitys.add(activityInstance);
										}
										
										ProcessDefinitionEntity processDefinition = new GetDeploymentProcessDefinitionCmd(historicProcessInstanceEntity.getProcessDefinitionId()).execute(commandContext);
										 
										ActivityImpl endActivity = processDefinition.findActivity(historicProcessInstanceEntity.getEndActivityId());

										List<PvmTransition> childPvmTransitions = null;
										if(endActivity == null){
											for (PvmActivity pvmActivity : processDefinition.getActivities()) {
												if("endEvent".equals(pvmActivity.getProperty("type"))){
													childPvmTransitions = pvmActivity.getIncomingTransitions();
													break;
												}
											}
										}else{
											childPvmTransitions = endActivity.getIncomingTransitions();
										}
										
										List<HistoricActivityInstance> activityInstances  = this.getHisActInsts(historicProcessInstanceEntity.getProcessInstanceId());
										
										findNearestUserTask(activityInstances, childPvmTransitions, previousActivitys,null);
									}
								}
							}
						}else if("subProcess".equals(activityInstance.getActivityType())){
							//父流程回退到内嵌子流程，先查找结束节点
							if(!isContains(previousActivitys, activityInstance)){
								ActivityImpl activityImpl =this.getActivity(activityInstance);
							
								if("parallel".equals(activityImpl.getProperty("multiInstance"))){
									previousActivitys.add(activityInstance);
									//由于无法明确记录多实例并行的内部节点关系，并行多实例暂时回退到内嵌子流程的第一个环节
								}else{
									List<PvmTransition> childPvmTransitions = new ArrayList<PvmTransition>();
									for (PvmActivity activity : transition.getSource().getActivities()) {
										if("endEvent".equals(activity.getProperty("type"))){
											for(HistoricActivityInstance historicActivityInstance: historicActivityInstances){
												if(historicActivityInstance.getActivityId().equals(activity.getId())){
													childPvmTransitions.addAll(activity.getIncomingTransitions());
													break;
												}
											}
										}
									}
									if(childPvmTransitions.size()>0){
										previousActivitys.add(activityInstance);
										findNearestUserTask(historicActivityInstances, childPvmTransitions, previousActivitys,null);
									}
								}
								
							}
						}else if("startEvent".equals(activityInstance.getActivityType())){
							//如果遍历到开始节点，判断是否有父流程
							ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(activityInstance.getExecutionId());
							if(executionEntity!=null&&executionEntity.getSuperExecution()!=null){
								
								ExecutionEntity superExecution = executionEntity.getSuperExecution();
								//如果外嵌子流程串行多实例
								boolean isSequential = false;
								if("sequential".equals(superExecution.getActivity().getProperty("multiInstance"))){
									//判断多实例的数量
									Integer nrOfCompletedInstances = superExecution.getVariableLocal("nrOfCompletedInstances",Integer.class);
						        	if(nrOfCompletedInstances>0){
						        		List<HistoricProcessInstance> processInstanceEntities = new HistoricProcessInstanceQueryImpl(commandContext)
						        			.superProcessInstanceId(superExecution.getProcessInstanceId())
						        			.finished()
						        			.orderByProcessInstanceEndTime().desc().list();
						        		for(HistoricProcessInstance instance : processInstanceEntities){
						        			if(!DELETE_REASON.equals(instance.getDeleteReason())){
						        				List<HistoricActivityInstance> activityInstances = new HistoricActivityInstanceQueryImpl(commandContext)
							        				.processInstanceId(superExecution.getProcessInstanceId())
							        				.activityId(superExecution.getActivityId())
							        				.executionId(superExecution.getId())
							        				.finished()
							        				.orderByHistoricActivityInstanceEndTime().desc().list();
						        				
						        				for(HistoricActivityInstance historicActivityInstance : activityInstances){
						        					if(instance.getId().equals(historicActivityInstance.getCalledProcessInstanceId())){
						        						previousActivitys.add(historicActivityInstance);
						        						break;
						        					}
						        				}
						        				
						        				ProcessDefinitionEntity processDefinition = new GetDeploymentProcessDefinitionCmd(instance.getProcessDefinitionId()).execute(commandContext);
												 
												ActivityImpl endActivity = processDefinition.findActivity(((HistoricProcessInstanceEntity)instance).getEndActivityId());

												List<PvmTransition> childPvmTransitions = endActivity.getIncomingTransitions();
												
												List<HistoricActivityInstance> childActivityInstances  = this.getHisActInsts(instance.getId());
												
												findNearestUserTask(childActivityInstances, childPvmTransitions, previousActivitys,null);
												
												//多实例串行本身回退无法删除，只能这删除
												this.deleteChildExecution(superExecution.getId());
												this.deleteExecution(superExecution);
												
												if(!superExecution.getParentId().equals(superExecution.getProcessInstanceId())){
													this.deleteExecution(superExecution.getParent());
												}
												isSequential=true;
												break;
						        			}
						        		}
						        	}
								
								}
								//如果非子流程串行自身回退
								if(!isSequential){
									List<HistoricActivityInstance> activityInstances = this.getHisActInsts(superExecution.getProcessInstanceId());
										
									List<PvmTransition> childPvmTransitions = superExecution.getActivity().getIncomingTransitions();
									
									findNearestUserTask(activityInstances, childPvmTransitions, previousActivitys,null);
								}
							}
						}else{
							findNearestUserTask(historicActivityInstances, transition.getSource().getIncomingTransitions(), previousActivitys,activityInstance);
						}
						//如果命中，并且非并行网关的情况可以结束遍历,否者跳出内循环
						if(!isParallel(activityInstance.getActivityType())){
							return;
						}else{
							break;
						}
					 }
				 }
			 }
		 }
	 }

	private boolean isParallel(String activityType){
		if("parallelGateway".equals(activityType)
				||"inclusiveGateway".equals(activityType)){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isReturnGateway(HistoricActivityInstance historicActivityInstance){
		if(historicActivityInstance.getDurationInMillis()!=null && historicActivityInstance.getDurationInMillis()==-1){
			return true;
		}else{
			return false;
		}
	}
	/**
	  * 由于加入回退，历史节点会出现重复，剔除重复节点
	  * @param previousActivitys
	  * @param activityInstance
	  * @return
	  */
	 private boolean isContains(List<HistoricActivityInstance> previousActivitys,HistoricActivityInstance activityInstance){
		 for(HistoricActivityInstance historicActivityInstance : previousActivitys){
			 if(historicActivityInstance.getProcessInstanceId().equals(activityInstance.getProcessInstanceId())){
				 if(historicActivityInstance.getActivityId().equals(activityInstance.getActivityId())){
					 return true;
				 }
			 }
		 }
		 return false;
	 }
}
