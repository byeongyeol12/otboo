package com.codeit.otboo.domain.follow.mapper;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.dto.UserSummary;
import com.codeit.otboo.domain.follow.entity.Follow;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T10:10:12+0900",
    comments = "version: 1.5.3.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.2.jar, environment: Java 17.0.15 (Homebrew)"
)
@Component
public class FollowMapperImpl implements FollowMapper {

    @Override
    public FollowDto toFollowDto(Follow follow) {
        if ( follow == null ) {
            return null;
        }

        UUID id = null;

        id = follow.getId();

        UserSummary followee = toUserSummary(follow.getFollowee());
        UserSummary follower = toUserSummary(follow.getFollower());

        FollowDto followDto = new FollowDto( id, followee, follower );

        return followDto;
    }

    @Override
    public List<FollowDto> toFollowDtoList(List<Follow> follows) {
        if ( follows == null ) {
            return null;
        }

        List<FollowDto> list = new ArrayList<FollowDto>( follows.size() );
        for ( Follow follow : follows ) {
            list.add( toFollowDto( follow ) );
        }

        return list;
    }

    @Override
    public FollowSummaryDto toFollowSummaryDto(UUID followeeId, Long followerCount, Long followingCount, Boolean followedByMe, UUID followedByMeId, Boolean followingMe) {
        if ( followeeId == null && followerCount == null && followingCount == null && followedByMe == null && followedByMeId == null && followingMe == null ) {
            return null;
        }

        UUID followeeId1 = null;
        followeeId1 = followeeId;
        Long followerCount1 = null;
        followerCount1 = followerCount;
        Long followingCount1 = null;
        followingCount1 = followingCount;
        Boolean followedByMe1 = null;
        followedByMe1 = followedByMe;
        UUID followedByMeId1 = null;
        followedByMeId1 = followedByMeId;
        Boolean followingMe1 = null;
        followingMe1 = followingMe;

        FollowSummaryDto followSummaryDto = new FollowSummaryDto( followeeId1, followerCount1, followingCount1, followedByMe1, followedByMeId1, followingMe1 );

        return followSummaryDto;
    }
}
