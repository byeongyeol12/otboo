-- === 기존 도메인 테이블들 ===

DROP TABLE IF EXISTS feed_likes CASCADE;
DROP TABLE IF EXISTS ootds CASCADE;
DROP TABLE IF EXISTS direct_messages CASCADE;
DROP TABLE IF EXISTS follows CASCADE;
DROP TABLE IF EXISTS clothes_attribute_values CASCADE;
DROP TABLE IF EXISTS feed_comments CASCADE;
DROP TABLE IF EXISTS feeds CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS weathers CASCADE;
DROP TABLE IF EXISTS clothes CASCADE;
DROP TABLE IF EXISTS clothes_attribute_definitions CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS sky_status_enum CASCADE;

-- Enum types
CREATE TYPE sky_status_enum AS ENUM (
  'CLEAR',
  'MOSTLY_CLOUDY',
  'CLOUDY'
);

CREATE TYPE user_role AS ENUM (
  'USER',
  'ADMIN'
);

-- users
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
                       locked BOOLEAN NOT NULL DEFAULT false,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ,
                       password_hash VARCHAR(255) NOT NULL,
                       Field VARCHAR(255)
);

-- profiles
CREATE TABLE profiles (
                          id UUID PRIMARY KEY,
                          nickname VARCHAR(50) NOT NULL,
                          gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
                          birth_date TIMESTAMPTZ,
                          latitude DOUBLE PRECISION,
                          longitude DOUBLE PRECISION,
                          x INTEGER,
                          y INTEGER,
                          location_names TEXT,
                          temp_sensitivity INTEGER NOT NULL DEFAULT 3,
                          profile_img_url TEXT,
                          user_id UUID NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ,
                          CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- clothes_attribute_definitions
CREATE TABLE clothes_attribute_definitions (
                                               id UUID PRIMARY KEY,
                                               name VARCHAR(50) NOT NULL,
                                               selectable_values TEXT[] NOT NULL,
                                               created_at TIMESTAMPTZ NOT NULL,
                                               updated_at TIMESTAMPTZ NOT NULL
);

-- clothes
CREATE TABLE clothes (
                         id UUID PRIMARY KEY,
                         owner_id UUID NOT NULL,
                         name VARCHAR(50) NOT NULL,
                         image_url VARCHAR(255),
                         type VARCHAR(100) NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL,
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         CONSTRAINT fk_clothes_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- weathers
CREATE TABLE weathers (
                          id UUID PRIMARY KEY,
                          forecasted_at TIMESTAMPTZ NOT NULL, -- 예보 발표 시각
                          forecast_at   TIMESTAMPTZ NOT NULL, -- 예보 대상 시각
                          location JSON,                      -- 위치
                          sky_status VARCHAR(255) NOT NULL,   -- 하늘상태
                          precipitation JSON,
                          precipitation_type VARCHAR(32),      -- 강수정보
                          humidity JSON,                      -- 습도
                          temperature JSON,                   -- 온도
                          wind_speed JSON,                    -- 풍속
                          created_at TIMESTAMPTZ NOT NULL,    -- 생성시각
                          updated_at TIMESTAMPTZ              -- 수정시각
);

-- notifications
CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               title VARCHAR(100) NOT NULL,
                               content TEXT NOT NULL,
                               level VARCHAR(10) NOT NULL DEFAULT 'INFO',
                               created_at TIMESTAMPTZ NOT NULL,
                               confirmed BOOLEAN NOT NULL,
                               user_id UUID NOT NULL,
                               event_ref_id UUID,
                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- feeds
CREATE TABLE feeds (
                       id UUID PRIMARY KEY,
                       user_id UUID NOT NULL,
                       weather_id UUID NOT NULL,
                       liked_by_me BOOLEAN NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ,
                       like_count BIGINT NOT NULL,
                       comment_count INTEGER NOT NULL,
                       content TEXT,
                       CONSTRAINT fk_feeds_user FOREIGN KEY (user_id) REFERENCES users(id),
                       CONSTRAINT fk_feeds_weather FOREIGN KEY (weather_id) REFERENCES weathers(id)
);

-- feed_comments
CREATE TABLE feed_comments (
                               id UUID PRIMARY KEY,
                               feed_id UUID NOT NULL,
                               author_id UUID NOT NULL,
                               created_at TIMESTAMPTZ NOT NULL,
                               content TEXT NOT NULL,
                               CONSTRAINT fk_feed_comments_feed FOREIGN KEY (feed_id) REFERENCES feeds(id),
                               CONSTRAINT fk_feed_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

-- clothes_attribute_values
CREATE TABLE clothes_attribute_values (
                                          id UUID PRIMARY KEY,
                                          clothes_id UUID NOT NULL,
                                          attribute_definition_id UUID NOT NULL,
                                          value VARCHAR(50) NOT NULL,
                                          created_at TIMESTAMPTZ NOT NULL,
                                          updated_at TIMESTAMPTZ NOT NULL,
                                          CONSTRAINT fk_attr_vals_clothes FOREIGN KEY (clothes_id) REFERENCES clothes(id),
                                          CONSTRAINT fk_attr_vals_defs FOREIGN KEY (attribute_definition_id) REFERENCES clothes_attribute_definitions(id)
);

-- follows
CREATE TABLE follows (
                         id UUID PRIMARY KEY,
                         created_at TIMESTAMPTZ NOT NULL,
                         follower_id UUID NOT NULL,
                         followee_id UUID NOT NULL,
                         CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id),
                         CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users(id),
                         CONSTRAINT uq_follows UNIQUE (follower_id, followee_id)
);

-- direct_messages
CREATE TABLE direct_messages (
                                 id UUID PRIMARY KEY,
                                 content TEXT,
                                 created_at TIMESTAMPTZ NOT NULL,
                                 sender_id UUID NOT NULL,
                                 receiver_id UUID NOT NULL,
                                 CONSTRAINT fk_direct_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
                                 CONSTRAINT fk_direct_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- ootds
CREATE TABLE ootds (
                       id UUID PRIMARY KEY,
                       clothes_id UUID NOT NULL,
                       feed_id UUID NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       CONSTRAINT fk_ootds_clothes FOREIGN KEY (clothes_id) REFERENCES clothes(id),
                       CONSTRAINT fk_ootds_feed FOREIGN KEY (feed_id) REFERENCES feeds(id)
);

-- feed_likes
CREATE TABLE feed_likes (
                            id UUID PRIMARY KEY,
                            created_at TIMESTAMPTZ NOT NULL,
                            feed_id UUID NOT NULL,
                            user_id UUID NOT NULL,
                            CONSTRAINT fk_feed_likes_feed FOREIGN KEY (feed_id) REFERENCES feeds(id),
                            CONSTRAINT fk_feed_likes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- === [Spring Batch 메타데이터 테이블 추가] ===

CREATE TABLE BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT ,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
);

CREATE TABLE BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                      VERSION BIGINT  ,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME TIMESTAMP NOT NULL,
                                      START_TIME TIMESTAMP DEFAULT NULL ,
                                      END_TIME TIMESTAMP DEFAULT NULL ,
                                      STATUS VARCHAR(10) ,
                                      EXIT_CODE VARCHAR(2500) ,
                                      EXIT_MESSAGE VARCHAR(2500) ,
                                      LAST_UPDATED TIMESTAMP,
                                      constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                          references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                                             JOB_EXECUTION_ID BIGINT NOT NULL ,
                                             PARAMETER_NAME VARCHAR(100) NOT NULL ,
                                             PARAMETER_TYPE VARCHAR(100) NOT NULL ,
                                             PARAMETER_VALUE VARCHAR(2500) ,
                                             IDENTIFYING CHAR(1) NOT NULL ,
                                             constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                                                 references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION  (
                                       STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                       VERSION BIGINT NOT NULL,
                                       STEP_NAME VARCHAR(100) NOT NULL,
                                       JOB_EXECUTION_ID BIGINT NOT NULL,
                                       CREATE_TIME TIMESTAMP NOT NULL,
                                       START_TIME TIMESTAMP DEFAULT NULL ,
                                       END_TIME TIMESTAMP DEFAULT NULL ,
                                       STATUS VARCHAR(10) ,
                                       COMMIT_COUNT BIGINT ,
                                       READ_COUNT BIGINT ,
                                       FILTER_COUNT BIGINT ,
                                       WRITE_COUNT BIGINT ,
                                       READ_SKIP_COUNT BIGINT ,
                                       WRITE_SKIP_COUNT BIGINT ,
                                       PROCESS_SKIP_COUNT BIGINT ,
                                       ROLLBACK_COUNT BIGINT ,
                                       EXIT_CODE VARCHAR(2500) ,
                                       EXIT_MESSAGE VARCHAR(2500) ,
                                       LAST_UPDATED TIMESTAMP,
                                       constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                                           references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
                                               STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                               SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                               SERIALIZED_CONTEXT TEXT ,
                                               constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                                                   references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
                                              JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT ,
                                              constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                                                  references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
