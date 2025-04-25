package com.zwp.speeddating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwp.speeddating.model.domain.Team;
import com.zwp.speeddating.service.TeamService;
import com.zwp.speeddating.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author zwp
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-04-25 14:20:02
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




