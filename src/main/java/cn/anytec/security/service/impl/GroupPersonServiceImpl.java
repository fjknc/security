package cn.anytec.security.service.impl;

import cn.anytec.security.common.ServerResponse;
import cn.anytec.security.constant.RedisConst;
import cn.anytec.security.core.log.LogObjectHolder;
import cn.anytec.security.dao.TbGroupPersonMapper;
import cn.anytec.security.dao.TbPersonMapper;
import cn.anytec.security.model.TbGroupPerson;
import cn.anytec.security.model.TbGroupPersonExample;
import cn.anytec.security.model.TbPerson;
import cn.anytec.security.model.TbPersonExample;
import cn.anytec.security.service.GroupPersonService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("GroupPersonService")
public class GroupPersonServiceImpl implements GroupPersonService {

    private final Logger logger = LoggerFactory.getLogger(GroupPersonServiceImpl.class);

    @Autowired
    private TbGroupPersonMapper groupPersonMapper;
    @Autowired
    private TbPersonMapper personMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public TbGroupPerson getPersonGroupInfo(Integer personGroupId){
        TbGroupPerson personGroup = groupPersonMapper.selectByPrimaryKey(personGroupId);
        LogObjectHolder.me().set(personGroup);
        return personGroup;
    }

    public ServerResponse add(TbGroupPerson groupPerson) {
        int updateCount = groupPersonMapper.insertSelective(groupPerson);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("添加groupPerson成功", groupPerson);
        }
        return ServerResponse.createByErrorMessage("添加groupPerson失败");
    }

    public ServerResponse<PageInfo> list(Integer pageNum, Integer pageSize,String groupName) {
        if(pageNum != 0 && pageSize != 0){
            PageHelper.startPage(pageNum, pageSize);
        }
        TbGroupPersonExample example = new TbGroupPersonExample();
        TbGroupPersonExample.Criteria c = example.createCriteria();
        if(groupName != null){
            c.andNameLike("%"+groupName.trim()+"%");
        }
        List<TbGroupPerson> personGroupList = groupPersonMapper.selectByExample(example);

        for(TbGroupPerson personGroup : personGroupList){
            getPersonGroupNum(personGroup);
        }
        PageInfo pageResult = new PageInfo(personGroupList);
        return ServerResponse.createBySuccess(pageResult);
    }

    private void getPersonGroupNum(TbGroupPerson personGroup){
        String groupId = personGroup.getId().toString();
        if(redisTemplate.opsForHash().hasKey(RedisConst.PERSON_GROUP_NUM_BY_GROUPID,groupId)){
            Integer count = Integer.parseInt(redisTemplate.opsForHash().get(RedisConst.PERSON_GROUP_NUM_BY_GROUPID,groupId).toString());
            personGroup.setTotalNumber(count);
        }else {
            TbPersonExample example = new TbPersonExample();
            TbPersonExample.Criteria c = example.createCriteria();
            c.andGroupIdEqualTo(personGroup.getId());
            Integer count = personMapper.countByExample(example);
            personGroup.setTotalNumber(count);
            redisTemplate.opsForHash().put(RedisConst.PERSON_GROUP_NUM_BY_GROUPID,groupId,count.toString());
        }
    }

    @Override
    public List<TbGroupPerson> allList() {
        TbGroupPersonExample example = new TbGroupPersonExample();
        TbGroupPersonExample.Criteria c = example.createCriteria();
        List<TbGroupPerson> personGroupList = groupPersonMapper.selectByExample(example);
        return personGroupList;
    }

    public ServerResponse delete(String groupPersonIds){
        List<String> groupPersonIdList = Splitter.on(",").splitToList(groupPersonIds);
        for(String personGroupId : groupPersonIdList){
            if(!StringUtils.isEmpty(personGroupId)){
                TbPersonExample personExample = new TbPersonExample();
                TbPersonExample.Criteria personC = personExample.createCriteria();
                personC.andGroupIdEqualTo(Integer.parseInt(personGroupId));
                List<TbPerson> personList = personMapper.selectByExample(personExample);
                if(personList.size()>0){
                    return ServerResponse.createByErrorMessage("底库还有底库成员,不能删除底库组");
                }
                TbGroupPersonExample example = new TbGroupPersonExample();
                TbGroupPersonExample.Criteria c = example.createCriteria();
                c.andIdEqualTo(Integer.parseInt(personGroupId));
                groupPersonMapper.deleteByExample(example);
                logger.info("【deleteMysqlPersonGroup】{}",personGroupId);

                deleteRedisPersonGroup(personGroupId);
            }
        }
        return ServerResponse.createBySuccess();
    }

    public ServerResponse<TbGroupPerson> update(TbGroupPerson groupPerson) {
        int updateCount = groupPersonMapper.updateByPrimaryKeySelective(groupPerson);
        if (updateCount > 0) {
            TbGroupPerson personGroup = groupPersonMapper.selectByPrimaryKey(groupPerson.getId());
            deleteRedisPersonGroup(personGroup.getId().toString());
            return ServerResponse.createBySuccess("更新groupPerson信息成功", groupPerson);
        }
        return ServerResponse.createByErrorMessage("更新groupPerson信息失败");
    }

    private void deleteRedisPersonGroup(String personGroupId){
        String redisKey = RedisConst.PERSONGROUP_BY_ID;
        if (redisTemplate.opsForHash().hasKey(redisKey, personGroupId)) {
            redisTemplate.opsForHash().delete(redisKey,personGroupId);
            logger.info("【deleteRedisPersonGroup】{}",personGroupId);
        }
    }

    public ServerResponse<TbGroupPerson> getGroupPersonById(String personGroupId) {
        String redisKey = RedisConst.PERSONGROUP_BY_ID;
        if (redisTemplate.opsForHash().hasKey(redisKey, personGroupId)) {
            String groupPersonStr= (String)redisTemplate.opsForHash().get(redisKey, personGroupId);
            redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
            TbGroupPerson groupPerson = JSONObject.parseObject(groupPersonStr,TbGroupPerson.class);
            return ServerResponse.createBySuccess("getGroupPersonById返回成功", groupPerson);
        }
        TbGroupPersonExample example = new TbGroupPersonExample();
        TbGroupPersonExample.Criteria c = example.createCriteria();
        c.andIdEqualTo(Integer.parseInt(personGroupId));
        List<TbGroupPerson> groupCameraList = groupPersonMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(groupCameraList)) {
            TbGroupPerson groupPerson = groupCameraList.get(0);
            redisTemplate.opsForHash().put(redisKey, personGroupId, JSONObject.toJSONString(groupPerson));
            return ServerResponse.createBySuccess("getGroupPersonById返回成功", groupPerson);
        }
        return ServerResponse.createByErrorMessage("getGroupPersonById未查到符合条件的信息");
    }

    @Override
    public boolean isPersonGroupNameExist(String personGroupName) {
        TbGroupPersonExample example = new TbGroupPersonExample();
        TbGroupPersonExample.Criteria c = example.createCriteria();
        c.andNameEqualTo(personGroupName);
        List<TbGroupPerson> personGroupList = groupPersonMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(personGroupList)){
            return true;
        }
        return false;
    }
}
