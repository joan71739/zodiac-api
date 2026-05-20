package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ClientListDto;
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

    /** 客戶列表（不含 ASC/MC），供 GET /api/clients 使用 */
    public List<ClientListDto> getAll() {
        return clientRepo.findAll().stream()
                .map(ClientListDto::from)
                .toList();
    }

    /** 單筆客戶（含 ASC/MC），供 GET /api/clients/{id} 使用 */
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
        findClient(id);
        clientRepo.deleteById(id);
    }

    // ── 星盤圖片 ─────────────────────────────────────────

    /**
     * 上傳星盤圖片。
     * chartImagePath 只儲存檔名（相對路徑），不暴露 uploadDir 完整路徑。
     */
    public void uploadChartImage(Integer id, MultipartFile file) throws IOException {
        Client c = findClient(id);

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String filename = "client_" + id + "_" + UUID.randomUUID() + ext;

        // 若已有舊圖，嘗試刪除
        if (c.getChartImagePath() != null) {
            try { Files.deleteIfExists(dir.resolve(c.getChartImagePath())); }
            catch (IOException ignored) { }
        }

        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        c.setChartImagePath(filename);
        clientRepo.save(c);
    }

    /** 讀取星盤圖片，回傳 Resource（Controller 直接串流） */
    public Resource loadChartImage(Integer id) throws MalformedURLException {
        Client c = findClient(id);
        if (c.getChartImagePath() == null) {
            throw new ResourceNotFoundException("尚未上傳星盤圖片，client id: " + id);
        }
        Path path = Paths.get(uploadDir).resolve(c.getChartImagePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("圖片檔案不存在: " + c.getChartImagePath());
        }
        return resource;
    }

    /** 由副檔名判斷圖片 Content-Type */
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
        c.setAscSign(req.getAscSign());
        c.setAscDegreeNum(req.getAscDegreeNum());
        c.setAscMinuteNum(req.getAscMinuteNum());
        c.setMcSign(req.getMcSign());
        c.setMcDegreeNum(req.getMcDegreeNum());
        c.setMcMinuteNum(req.getMcMinuteNum());
    }
}
