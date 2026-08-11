# 杏林学堂 Medicine-AI

本仓库当前实现“杏林学堂——中医学习系统”的视频学习中心模块，包含 Spring Boot 后端、Vue 3 前端和 ECS 单机 Docker 部署配置。

## 已实现功能

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
- 后台视频新增、编辑、上下架接口
- MySQL 持久化
- Redis 播放去重与搜索词统计
- RabbitMQ 学习完成事件
- Caffeine 本地详情缓存

## 目录结构

```text
Medicine-AI/
  backend/video-service/   Spring Boot 视频服务
  frontend/                Vue 3 视频学习中心
  deploy/nginx/            Nginx 反向代理配置
  docker-compose.yml       ECS 单机部署编排
```

## 本地启动

后端：

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

第一阶段尚未接入用户服务，前端默认通过请求头 `X-User-Id: 10001` 模拟登录用户。等用户服务完成后，将替换为 JWT 鉴权。

## 视频资源

初始化数据使用公开 MP4 示例地址，保证部署后可以直接播放。真实视频可以上传到 `data/media` 并将视频 URL 改为 `/media/xxx.mp4`。
