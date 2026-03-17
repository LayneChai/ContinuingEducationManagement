# Environment Variables Example

以下变量建议在本地命令行或系统环境变量中设置，不要写入代码仓库。

```bat
set DB_URL=jdbc:mysql://127.0.0.1:3306/continue_education?useUnicode=true^&characterEncoding=utf8^&serverTimezone=Asia/Shanghai^&useSSL=false^&allowPublicKeyRetrieval=true
set DB_USERNAME=root
set DB_PASSWORD=your_password

set REDIS_HOST=127.0.0.1
set REDIS_PORT=6379
set REDIS_DATABASE=0

set DEEPSEEK_API_KEY=your_deepseek_key
set DEEPSEEK_BASE_URL=https://api.deepseek.com
set DEEPSEEK_MODEL=deepseek-chat
```

设置完成后再执行：

```bat
start-backend.bat
```
