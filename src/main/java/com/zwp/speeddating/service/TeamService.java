package com.zwp.speeddating.service;

import com.zwp.speeddating.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zwp.speeddating.model.domain.User;
import com.zwp.speeddating.model.dto.TeamQuery;
import com.zwp.speeddating.model.request.TeamJoinRequest;
import com.zwp.speeddating.model.request.TeamQuitRequest;
import com.zwp.speeddating.model.request.TeamUpdateRequest;
import com.zwp.speeddating.model.vo.TeamUserVO;

import java.util.List;

/**
* @author zwp
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-04-25 14:20:02
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 修改队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 离开队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除（解散）队伍
     * @param id
     * @return
     */
    Boolean deleteTeam(long id, User loginUser);
}
