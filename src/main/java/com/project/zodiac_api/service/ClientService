package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ClientRequestDto;
import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.Client;
import com.project.zodiac_api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepo;

    @Value("${app.chart-image.upload-dir}")
    private String uploadDir;

    // ── CRUD ────────────────────────────────────────────

    public List<ClientResponseDto> getAll() {
        return clientRepo.findAll().stream()
                .map(ClientResponseDto::from)
                .toList();
    }

    public ClientResponseDto getById(Integer id) {
        return ClientResponseDto.from(findClient(id));
    }

    public ClientResponseDto create(ClientRequestDto req) {
        Client c = new Client();
        mapRequest(req, c);
        return ClientResponseDto.from(clientRepo.save(c));
    }

    public ClientResponseDto update(Integer id, ClientRequestDto req) {
        Client c = findClient(id);
        mapRequest(req, c);
        return ClientResponseDto.from(clientRepo.save(c));
    }

    public void delete(Integer id) {
        findClient(id);             // 確認存在
        clientRepo.deleteById(id);
    }

    // ── 星盤圖片 ─────────────────────────────────────────

    /**
     * 上傳星盤圖片，儲存後更新 chartImagePath
     */
    public void uploadChartImage(Integer id, MultipartFile file) throws IOException {
        Client c = findClient(id);

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        // 取得副檔名並產生唯一檔名
        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String filename = "client_" + id + "_" + UUID.randomUUID() + ext;

        // 若已有舊圖，嘗試刪除
        if (c.getChartImagePath() != null) {
            try { Files.deleteIfExists(Paths.get(c.getChartImagePath())); }
            catch (IOException ignored) { }
        }

        Path dest = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        c.setChartImagePath(dest.toString());
        clientRepo.save(c);
    }

    /**
     * 讀取星盤圖片，回傳 Resource（Controller 直接串流）
     */
    public Resource loadChartImage(Integer id) throws MalformedURLException {
        Client c = findClient(id);
        if (c.getChartImagePath() == null) {
            throw new ResourceNotFoundException("尚未上傳星盤圖片，client id: " + id);
        }
        Path path = Paths.get(c.getChartImagePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("圖片檔案不存在: " + c.getChartImagePath());
        }
        return resource;
    }

    /**
     * 取得圖片 Content-Type（由副檔名判斷）
     */
    public String detectContentType(Integer id) {
        Client c = findClient(id);
        if (c.getChartImagePath() == null) return "image/jpeg";
        String path = c.getChartImagePath().toLowerCase();
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".webp")) return "image/webp";
        if (path.endsWith(".gif"))  return "image/gif";
        return "image/jpeg";
    }

    // ── 私有 helper ──────────────────────────────────────

    private Client findClient(Integer id) {
        return clientRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private void mapRequest(ClientRequestDto req, Client c) {
        c.setName(req.getName());
        c.setBirthDate(req.getBirthDate());
        c.setBirthTime(req.getBirthTime());
        c.setBirthPlace(req.getBirthPlace());
    }
}
