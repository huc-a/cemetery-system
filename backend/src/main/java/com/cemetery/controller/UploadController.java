package com.cemetery.controller;
import com.cemetery.common.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Value("${cemetery.upload-dir}")
    private String uploadDir;
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX = 5L * 1024 * 1024;

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new BizException("请选择要上传的图片");
        if (file.getSize() > MAX) throw new BizException("图片大小不能超过 5MB");
        String orig = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = orig.contains(".") ? orig.substring(orig.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) throw new BizException("只支持 jpg/png/webp 图片");
        // 校验真实文件头，防止改后缀伪装
        byte[] head = file.getBytes();
        boolean magicOk = (head.length > 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8)
            || (head.length > 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P')
            || (head.length > 12 && head[0] == 'R' && head[8] == 'W');
        if (!magicOk) throw new BizException("文件内容不是有效的图片");
        String ym = java.time.LocalDate.now().toString().substring(0, 7);
        String name = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "." + ext;
        Path dir = Paths.get(uploadDir, ym);
        Files.createDirectories(dir);
        Path dest = dir.resolve(name);
        Files.write(dest, head);
        String url = "/uploads/" + ym + "/" + name;
        return Result.ok(Map.of("url", url, "name", orig, "size", String.valueOf(file.getSize())));
    }
}
