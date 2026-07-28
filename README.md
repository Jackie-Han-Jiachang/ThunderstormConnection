# 《雷雨》人物关系网

一个供中文课堂使用的离线互动应用。用户可以创建《雷雨》事件卡片，把事件从“预备事件”放到“已发生事件”，并观察角色好感度如何改变人物关系网。

## 环境要求

- Java 17 或更高版本
- Maven 3.9 或更高版本（仅构建时需要）

## 运行

```bash
./mvnw clean package
java -jar target/thunderstorm-1.0.0.jar
```

然后在浏览器打开 [http://localhost:8080](http://localhost:8080)。

Windows 构建时使用：

```powershell
mvnw.cmd clean package
java -jar target\thunderstorm-1.0.0.jar
```

应用完全离线运行。事件数据默认保存在：

- macOS/Linux：`~/.thunderstorm/events.json`
- Windows：`%USERPROFILE%\\.thunderstorm\\events.json`

如需使用其他数据文件：

```bash
java -jar target/thunderstorm-1.0.0.jar --thunderstorm.data-file=/absolute/path/events.json
```

## 测试

```bash
./mvnw test
```

## 项目文档

- [需求说明](PROMPT.md)
- [设计与实施计划](PLAN.md)
- [界面 Mock-up](Mock-ups.pdf)
