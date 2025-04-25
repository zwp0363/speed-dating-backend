package com.zwp.speeddating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwp.speeddating.model.domain.UserTeam;
import com.zwp.speeddating.service.UserTeamService;
import com.zwp.speeddating.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author zwp
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-04-25 14:20:08
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




