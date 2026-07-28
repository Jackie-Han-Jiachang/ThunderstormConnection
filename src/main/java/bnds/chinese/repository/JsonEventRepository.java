package bnds.chinese.repository;

import bnds.chinese.exception.DataStorageException;
import bnds.chinese.model.AppState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Repository
public class JsonEventRepository implements EventRepository {
    private final ObjectMapper objectMapper;
    private final Path dataFile;

    public JsonEventRepository(ObjectMapper objectMapper,
            @Value("${thunderstorm.data-file:#{systemProperties['user.home'] + '/.thunderstorm/events.json'}}") String dataFile) {
        this.objectMapper = objectMapper;
        this.dataFile = Path.of(dataFile).toAbsolutePath().normalize();
    }

    @Override
    public synchronized AppState load() {
        if (!Files.exists(dataFile)) {
            return new AppState();
        }
        try {
            return objectMapper.readValue(dataFile.toFile(), AppState.class);
        } catch (IOException exception) {
            preserveCorruptFile();
            throw new DataStorageException("本地数据文件无法读取，已保留损坏文件的备份", exception);
        }
    }

    @Override
    public synchronized void save(AppState state) {
        Path parent = dataFile.getParent();
        Path temporary = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(parent);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), state);
            try {
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new DataStorageException("事件保存失败，请检查本地数据目录是否可写", exception);
        }
    }

    private void preserveCorruptFile() {
        try {
            Path backup = dataFile.resolveSibling(dataFile.getFileName() + ".corrupt");
            Files.copy(dataFile, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // The original remains in place if the backup cannot be created.
        }
    }
}
