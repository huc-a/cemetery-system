# 青松陵园 · 智慧墓园管理系统

Spring Boot 3.3.5 + Vue 3 + MySQL 8 的智慧墓园/陵园综合管理系统，参照行业真实业务设计：
看墓选墓 → 预订锁位 → 签约购墓 → 缴费 → 安葬 → 碑刻祭扫 → 管理费续缴 → 巡查养护 全流程闭环，并提供云祭扫在线纪念馆与代祭扫服务。

## 功能模块
- 墓区/墓位管理：5 类墓区分区，区-排-号编码，状态（空闲/已预订/已售/已安葬/维护中），批量生成墓位
- 可视化选墓：平面网格按状态着色，点击查看详情，在线预订 / 生成购墓合同
- 逝者档案（一人一档）：生卒享年自动计算，建档自动生成在线纪念馆
- 家属客户管理：三角色账号（ADMIN/STAFF/FAMILY），数据隔离
- 购墓合同：选墓→签约→缴费状态流转（可分次缴费），已售墓位重复购买拒绝，缴清自动生成管理费周期
- 管理费与续费：到期日动态状态（逾期/临期30天/待缴/已缴），提醒列表，续缴自动顺延周期并落财务流水
- 安葬预约与登记：仅已售墓位可预约，同墓位同时段冲突拒绝，登记完成墓位变已安葬
- 祭扫预约：每日 4 时段、每时段容量 20 人，超限拒绝，余量查询，工作人员核销入园
- 云祭扫/在线纪念馆：献花/点烛/上香/鞠躬/留言计数落库 + 祭扫时间线
- 服务项目与订单：立碑、代祭扫、绿化养护等 10 类服务，下单→支付→派单→服务→完成回传流转
- 巡查养护工单：派单→处理→完成（结果必填）流转
- 收费/财务记录：三类业务统一流水，与仪表盘收入同口径对账
- 数据统计仪表盘（ECharts）：墓位状态分布、近6月收入、收入构成、祭扫趋势、墓区售出率
- 通知公告、系统用户管理、双登录入口（家属入口 /login、员工入口 /admin/login）

## 演示账号
| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 园区工作人员 | staff | staff123 |
| 家属/客户 | family | family123 |

另有 staff2、staff3 工作人员与 family2–family8 家属种子账号（密码同角色默认）。

## 技术栈
Spring Boot 3.3.5 · Java 21 · Spring Data JPA · Spring Security + JWT · MySQL 8 · Vue 3 · Element Plus · Pinia · Vue Router · ECharts · Vite

## 启动方式
1. 准备 MySQL 8，创建数据库 cemetery_db 与应用账号（见 sql/ 说明），把连接信息写入项目根目录 `.env`（DB_USERNAME / DB_PASSWORD，文件权限 0600，不要提交）。
2. 后端构建运行（注意本机 Java 需 IPv4 优先参数）：
   - `cd backend && mvn -DskipTests package`
   - `java -Djava.net.preferIPv4Stack=true -jar target/cemetery-system-1.0.0.jar`（环境变量从 .env 注入，或使用 systemd）
3. 前端开发调试：`cd frontend && npm install && npm run dev`（5176 端口，代理到 8083）；生产构建 `npm run build` 后将 dist 内容复制到 `backend/src/main/resources/static/` 再打包，由后端静态托管。
4. systemd 部署（本机现行方式）：服务名 `cemetery-app`，WorkingDirectory 为 backend 目录，EnvironmentFile 指向项目 .env，端口 8083。
5. 访问：http://localhost:8083 （公网 8083.453.de5.net 需在 Cloudflare 为隧道添加该子域名路由指向 localhost:8083）

首次启动自动初始化种子数据（DataInitializer，仅空库执行）：5 墓区 / 122 墓位 / 10 逝者 / 12 合同 / 10 纪念馆等。

## 目录
- backend/ Spring Boot 后端（15 张表、约 50 个 Java 文件）
- frontend/ Vue 3 前端（25 个页面/布局）
- docs/需求分析与系统设计.md 行业调研与设计文档
- sql/ 数据库说明
