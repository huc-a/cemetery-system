package com.cemetery.config;
import java.util.Map;

/** 本地化图片资源映射（图片存放在 static/img/，由 /img/** 托管） */
public class ImageAssets {
    public static final Map<String, String> AVATAR_BY_USERNAME = Map.ofEntries(
        Map.entry("admin", "/img/avatar-admin.jpg"),
        Map.entry("staff", "/img/avatar-staff.jpg"),
        Map.entry("staff2", "/img/avatar-staff2.jpg"),
        Map.entry("staff3", "/img/avatar-staff3.jpg"),
        Map.entry("family", "/img/avatar-family.jpg"),
        Map.entry("family2", "/img/avatar-family2.jpg"),
        Map.entry("family3", "/img/avatar-family3.jpg"),
        Map.entry("family4", "/img/avatar-family4.jpg"),
        Map.entry("family5", "/img/avatar-family5.jpg"),
        Map.entry("family6", "/img/avatar-family6.jpg"),
        Map.entry("family7", "/img/avatar-family7.jpg"),
        Map.entry("family8", "/img/avatar-family8.jpg"));

    public static final Map<String, String> AREA_BY_NAME = Map.of(
        "松鹤园", "/img/area-1.jpg",
        "梅香园", "/img/area-2.jpg",
        "兰馨园", "/img/area-3.jpg",
        "竹韵园", "/img/area-4.png",
        "福泽园", "/img/area-5.png");

    public static final Map<String, String> DECEASED_BY_NAME = Map.of(
        "张寿山", "/img/deceased-1.jpg",
        "王桂芳", "/img/deceased-2.jpg",
        "刘建国", "/img/deceased-3.jpg",
        "周雅琴", "/img/deceased-4.jpg",
        "吴文正", "/img/deceased-5.jpg",
        "郑秀英", "/img/deceased-6.jpg",
        "孙明远", "/img/deceased-7.jpg",
        "林静姝", "/img/deceased-8.jpg",
        "何志强", "/img/deceased-9.jpg",
        "张慧敏", "/img/deceased-10.jpg");

    public static final Map<String, String> SERVICE_BY_NAME = Map.of(
        "标准立碑服务", "/img/service-1.png",
        "碑文精刻加字", "/img/service-2.jpg",
        "代客祭扫·基础包", "/img/service-3.webp",
        "代客祭扫·尊享包", "/img/service-4.jpg",
        "墓位绿化养护", "/img/service-5.webp",
        "墓碑清洗翻新", "/img/service-6.jpg",
        "安葬礼仪服务", "/img/service-7.jpg",
        "鲜花预订配送", "/img/service-8.jpg",
        "骨灰寄存格位年费", "/img/service-9.webp",
        "迁墓/改穴咨询服务", "/img/service-10.jpg");
}
