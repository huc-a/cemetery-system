package com.cemetery.config;
import com.cemetery.entity.*;
import com.cemetery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 为已有数据库回填图片字段（仅填充为空的记录，不覆盖用户后来上传的图片） */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class ImageBackfill implements CommandLineRunner {
    private final SysUserRepository users;
    private final CemeteryAreaRepository areas;
    private final DeceasedRepository deceased;
    private final MemorialHallRepository halls;
    private final ServiceProductRepository services;

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    @Override public void run(String... args) {
        int n = 0;
        for (SysUser u : users.findAll()) {
            if (blank(u.getAvatarUrl())) {
                String a = ImageAssets.AVATAR_BY_USERNAME.get(u.getUsername());
                if (a != null) { u.setAvatarUrl(a); users.save(u); n++; }
            }
        }
        for (CemeteryArea a : areas.findAll()) {
            if (blank(a.getCoverUrl())) {
                String img = ImageAssets.AREA_BY_NAME.get(a.getName());
                if (img != null) { a.setCoverUrl(img); areas.save(a); n++; }
            }
        }
        for (Deceased d : deceased.findAll()) {
            if (blank(d.getPhotoUrl())) {
                String img = ImageAssets.DECEASED_BY_NAME.get(d.getName());
                if (img != null) { d.setPhotoUrl(img); deceased.save(d); n++; }
            }
        }
        for (MemorialHall h : halls.findAll()) {
            if (blank(h.getPhotoUrl())) {
                // 纪念馆与对应逝者用同一张照片：优先按逝者档案已回填的 photoUrl，其次按姓名映射
                String img = null;
                if (h.getDeceasedId() != null) {
                    img = deceased.findById(h.getDeceasedId()).map(Deceased::getPhotoUrl).filter(s -> !blank(s)).orElse(null);
                }
                if (img == null) img = ImageAssets.DECEASED_BY_NAME.get(h.getDeceasedName());
                if (img != null) { h.setPhotoUrl(img); halls.save(h); n++; }
            }
        }
        for (ServiceProduct s : services.findAll()) {
            if (blank(s.getCoverUrl())) {
                String img = ImageAssets.SERVICE_BY_NAME.get(s.getName());
                if (img != null) { s.setCoverUrl(img); services.save(s); n++; }
            }
        }
        if (n > 0) log.info("ImageBackfill: filled image fields for {} records", n);
    }
}
