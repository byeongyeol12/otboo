-- users
-- 올바른 값의 사용자 더미 2명
INSERT INTO users (id, email, name, role, locked, created_at, password_hash, Field)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'userA@email.com', '유저A', 'USER', false, now(), 'Testpw123!', null),
    ('22222222-2222-2222-2222-222222222222', 'userB@email.com', '유저B', 'USER', false, now(), 'Testpw123!!', null);

-- profiles
INSERT INTO profiles (id, nickname, gender, birth_date, latitude, longitude, x, y, location_names, temp_sensitivity, profile_img_url, user_id, created_at, updated_at)
VALUES
    ('aaaa1111-2222-3333-4444-555566667777', '유저A', 'MALE', null, 37.5665, 126.9780, 60, 127, '서울특별시', 3, null, '11111111-1111-1111-1111-111111111111', now(), null),
    ('bbbb2222-3333-4444-5555-666677778888', '유저B', 'FEMALE', null, 35.1796, 129.0756, 100, 50, '부산광역시', 4, null, '22222222-2222-2222-2222-222222222222', now(), null);

-- clothes_attribute_definitions
INSERT INTO clothes_attribute_definitions (id, name, selectable_values, created_at, updated_at)
VALUES
    ('cccc3333-4444-5555-6666-777788889999', '색상', ARRAY['빨강','파랑','노랑','검정','흰색'], now(), now()),
    ('dddd4444-5555-6666-7777-88889999aaaa', '재질', ARRAY['면','울','폴리','가죽'], now(), now());

-- clothes
INSERT INTO clothes (id, owner_id, name, image_url, type, created_at, updated_at)
VALUES
    ('eeee5555-6666-7777-8888-9999aaaabbbb', '11111111-1111-1111-1111-111111111111', '빨간티', null, 'TOP', now(), now()),
    ('ffff6666-7777-8888-9999-aaaabbbbcccc', '22222222-2222-2222-2222-222222222222', '청바지', null, 'BOTTOM', now(), now());

-- weathers
INSERT INTO weathers (id, forecasted_at, forecast_at, location, sky_status, precipitation, humidity, temperature, wind_speed, created_at, updated_at)
VALUES
    ('99998888-7777-6666-5555-444433332222', now(), now(), '{"latitude":37.5665,"longitude":126.9780}', 'CLEAR', '{"type":"NONE"}', '{"current":30}', '{"current":25}', '{"speed":2}', now(), now());

-- notifications
INSERT INTO notifications (id, title, content, level, created_at, confirmed, user_id)
VALUES
    ('aaaa9999-0000-0000-0000-000000000000', '테스트 알림', '알림 내용입니다.', 'INFO', now(), false, '11111111-1111-1111-1111-111111111111');

-- feeds
INSERT INTO feeds (id, user_id, weather_id, liked_by_me, created_at, updated_at, like_count, comment_count, content)
VALUES
    ('fedfedfe-dfed-dfed-dfed-fedfedfedfed', '11111111-1111-1111-1111-111111111111', '99998888-7777-6666-5555-444433332222', false, now(), now(), 1, 1, '오늘의 코디!');

-- feed_comments
INSERT INTO feed_comments (id, feed_id, author_id, created_at, content)
VALUES
    ('1111aaaa-2222-bbbb-3333-cccc44445555', 'fedfedfe-dfed-dfed-dfed-fedfedfedfed', '22222222-2222-2222-2222-222222222222', now(), '좋아요!');

-- clothes_attribute_values
INSERT INTO clothes_attribute_values (id, clothes_id, attribute_definition_id, value, created_at, updated_at)
VALUES
    ('12345678-90ab-cdef-1234-567890abcdef', 'eeee5555-6666-7777-8888-9999aaaabbbb', 'cccc3333-4444-5555-6666-777788889999', '빨강', now(), now()),
    ('23456789-0abc-def1-2345-67890abcdef1', 'ffff6666-7777-8888-9999-aaaabbbbcccc', 'dddd4444-5555-6666-7777-88889999aaaa', '면', now(), now());

-- ootds
INSERT INTO ootds (id, clothes_id, feed_id, created_at)
VALUES
    ('3333bbbb-4444-cccc-5555-dddd66667777', 'eeee5555-6666-7777-8888-9999aaaabbbb', 'fedfedfe-dfed-dfed-dfed-fedfedfedfed', now());

-- feed_likes
INSERT INTO feed_likes (id, created_at, feed_id, user_id)
VALUES
    ('4444cccc-5555-dddd-6666-eeee77778888', now(), 'fedfedfe-dfed-dfed-dfed-fedfedfedfed', '22222222-2222-2222-2222-222222222222');
