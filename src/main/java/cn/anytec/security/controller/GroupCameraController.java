package cn.anytec.security.controller;

import cn.anytec.security.common.ServerResponse;
import cn.anytec.security.core.annotion.OperLog;
import cn.anytec.security.core.annotion.Permission;
import cn.anytec.security.core.enums.PermissionType;
import cn.anytec.security.model.TbGroupCamera;
import cn.anytec.security.service.GroupCameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/groupCamera")
public class GroupCameraController {
    @Autowired
    private GroupCameraService groupCameraService;

    @OperLog(value = "添加设备组", key="id,name")
    @RequestMapping("/add")
    @ResponseBody
    @Permission(value = "添加设备组", method = PermissionType.IS_ADMIN)
    public ServerResponse add(TbGroupCamera groupCamera){
        String cameraGroupName = groupCamera.getName();
        if(groupCameraService.isCameraGroupNameExist(cameraGroupName)){
            return ServerResponse.createByErrorMessage("设备祖名称 "+cameraGroupName+" 已存在");
        }
        return groupCameraService.add(groupCamera);
    }

    @OperLog(value = "删除设备组", key = "groupCameraIds")
    @RequestMapping("/delete")
    @ResponseBody
    @Permission(value = "删除设备组", method = PermissionType.IS_ADMIN)
    public ServerResponse delete(@RequestParam(value = "groupCameraIds") String groupCameraIds){
        return groupCameraService.delete(groupCameraIds);
    }


    @RequestMapping("/list")
    @ResponseBody
//    @Permission(value = "查询设备组", method = PermissionType.IS_ADMIN)
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "0") Integer pageSize,
                               @RequestParam(value = "groupName", required = false)String groupName){
        return groupCameraService.list(pageNum,pageSize,groupName);
    }

    @OperLog(value = "修改设备组", key="id,name")
    @RequestMapping("/update")
    @ResponseBody
    @Permission(value = "修改设备组", method = PermissionType.IS_ADMIN)
    public ServerResponse update(TbGroupCamera groupCamera){
        return groupCameraService.update(groupCamera);
    }

    @RequestMapping("/getAllCameras")
    @ResponseBody
    public ServerResponse getAllCameras(@RequestParam(value = "status",required = false)String status){
        return groupCameraService.getAllCameras(status);
    }
}
