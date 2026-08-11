# 杏林学堂 Medicine-AI

本仓库实现“杏林学堂——中医学习系统”的核心基础模块，当前包括视频学习中心、用户服务、前端页面和 ECS 单机 Docker 部署配置。

## 已实现功能

- 用户注册 / 登录 / JWT 鉴权 / 角色权限
- 用户资料、每日签到、积分账户、积分流水
- 视频学习首页
- 分类筛选
- 关键词搜索
- 视频详情
- HTML5 视频播放
- 有效播放统计
- 学习进度自动上报
- 继续学习
- 学习历史
- 点赞 / 取消点赞
- 收藏 / 取消收藏
- 后台视频分类新增、编辑、启用 / 禁用
- 后台视频新增、编辑、上线 / 下架
- 后台积分规则、等级规则维护
- 普通用户课程上传，管理员审核上线
- MySQL 持久化
- Redis 播放去重、登录限流、签到 Bitmap、积分幂等
- RabbitMQ 视频学习完成事件与积分消费
- Caffeine 本地详情缓存
- traceId 请求日志、业务日志、异常日志

## 目录结构

```text
Medicine-AI/
  backend/user-service/    Spring Boot 用户服务
  backend/video-service/   Spring Boot 视频服务
  frontend/                Vue 3 前端
  deploy/nginx/            Nginx 反向代理配置
  docker-compose.yml       ECS 单机部署编排
```

## 本地启动

用户服务：

```bash
cd backend/user-service
mvn spring-boot:run
```

视频服务：

```bash
cd backend/video-service
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

## ECS 部署

1. 安装 Docker 和 Docker Compose。
2. 拉取仓库到 ECS。
3. 回到仓库根目录启动服务。

```bash
docker compose up -d --build
```

4. 访问 ECS 公网 IP。

```text
http://你的ECS公网IP
```

也可以直接使用部署脚本。该脚本不要求 ECS 宿主机安装 Node/npm，前端会在 Docker 镜像构建阶段完成打包：

```bash
bash deploy/scripts/deploy-ecs.sh
```

生产环境第一次启动前请复制并修改 `.env`：

```bash
cp .env.example .env
vim .env
```

## 演示登录

用户服务第一次连接空数据库时，会按环境变量初始化一个演示账号。默认值如下，可在 `.env` 中修改或关闭：

```text
账号：student001
密码：abc123456
```

前端登录后会使用 JWT 调用用户服务，并临时透传 `X-User-Id` 给视频服务以兼容现有视频学习接口。后续接入统一网关后，会由网关解析 JWT 并向内部服务透传可信用户身份。

## 权限账号

系统会初始化一个管理员账号，用于访问数据维护中心：

```text
用户名：yjyjocyer
```

管理员可以维护视频分类、课程上下架、积分规则和等级规则。普通用户注册时只需要填写用户名、手机号、密码，登录后只能提交课程视频，提交后的课程默认为草稿状态，需管理员审核上线。

生产环境必须在 `.env` 中设置 `ADMIN_PASSWORD` 后才会自动创建管理员账号，避免管理员明文密码进入代码仓库。

## 数据维护

视频分类、课程视频、积分规则、等级规则均从 MySQL 读取。访问前端 `/admin` 可维护数据：

- 视频分类：新增、编辑、启用、禁用。
- 课程视频：新增、编辑、上线、下架。
- 积分规则：维护 `CHECKIN`、`CHECKIN_7_DAYS`、`VIDEO_FINISH` 等业务积分值。
- 等级规则：维护等级名称和最低累计积分。

用户服务启动时只会在规则表为空时初始化默认积分/等级规则。视频服务不再写死课程数据，课程内容需要通过管理页或管理接口写入数据库。
