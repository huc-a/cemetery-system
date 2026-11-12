数据库：cemetery_db（MySQL 8，utf8mb4）。表结构由 JPA (ddl-auto=update) 依据实体自动创建，无需手工执行 DDL。
应用账号：cemetery（密码见项目 .env，勿写入仓库）。授权：GRANT ALL ON cemetery_db.* TO 'cemetery'@'127.0.0.1' / 'localhost'。
表清单（15）：sys_user, cemetery_area, grave_plot, deceased, purchase_contract, management_fee, burial_record, visit_appointment, memorial_hall, memorial_action, service_product, service_order, work_order, payment_record, notice。
