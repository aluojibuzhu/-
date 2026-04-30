# 项目启动与访问说明

## 1. 环境依赖

启动项目前，请确认本机已安装：

```powershell
java -version
mvn -version
node -v
npm -v
```

建议环境：

- JDK 17
- Maven 3.x
- Node.js / npm
- MySQL
- Redis

## 2. 数据库与 Redis

先启动本机 MySQL 和 Redis。

后端默认数据库配置位于：

```text
RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml
```

默认连接信息：

```text
数据库: ry-vue
用户: root
密码: 123456
```

首次运行需要导入 SQL，建议按以下顺序执行：

```text
RuoYi-Vue/sql/ry_20260417.sql
RuoYi-Vue/sql/quartz.sql
RuoYi-Vue/sql/2026-04-21_project_establishment_schema.sql
```

## 3. 启动后端

打开 PowerShell，进入后端工程目录：

```powershell
cd D:\cursor\project\Engineering-Cost-Management-System\1st\RuoYi-Vue
```

打包并启动：

```powershell
mvn -pl ruoyi-admin -am -DskipTests package
java -jar .\ruoyi-admin\target\ruoyi-admin.jar
```

后端默认地址：

```text
http://localhost:8080
```

如果本机 MySQL 账号密码不是默认值，可以在启动前设置环境变量：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的数据库密码"
$env:MYSQL_URL="jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8"

java -jar .\ruoyi-admin\target\ruoyi-admin.jar
```

## 4. 启动前端

新开一个 PowerShell 窗口，进入前端工程目录：

```powershell
cd D:\cursor\project\Engineering-Cost-Management-System\1st\RuoYi-Vue\ruoyi-ui
```

安装依赖并启动：

```powershell
npm ci
$env:port=8081
npm run dev
```

前端访问地址：

```text
http://localhost:8081
```

## 5. 登录账号

默认账号：

```text
admin / admin123
```

当前项目登录页已省略验证码。修改后需要重新启动后端和前端才会生效。

## 6. 常见问题

## 6. 关闭项目

### 正常关闭

如果前端和后端都是在 PowerShell 窗口中启动的，直接在对应窗口按：

```text
Ctrl + C
```

分别停止：

- 后端窗口：停止 `java -jar ...`
- 前端窗口：停止 `npm run dev`

### 端口占用时强制关闭

如果窗口已经关闭，但端口仍然被占用，可以通过端口查找进程并停止。

查看后端 `8080` 端口：

```powershell
netstat -ano | findstr :8080
```

查看前端 `8081` 端口：

```powershell
netstat -ano | findstr :8081
```

输出末尾的数字是进程 PID，例如：

```text
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    12345
```

停止对应进程：

```powershell
taskkill /PID 12345 /F
```

也可以使用 PowerShell：

```powershell
Stop-Process -Id 12345 -Force
```

### 确认端口已释放

再次执行：

```powershell
netstat -ano | findstr :8080
netstat -ano | findstr :8081
```

如果没有输出，说明端口已释放。

## 7. 常见问题

### 登录提示系统接口 500 异常

优先查看后端启动窗口中的异常信息。

如果看到类似：

```text
/project/info/undefined
```

这通常不是登录验证码问题，而是页面跳转时带了 `undefined` 项目 ID，需要检查前端路由跳转参数。

### 端口被占用

后端默认端口是 `8080`，前端建议使用 `8081`。

如果端口被占用，可以换端口：

后端：

```powershell
java -jar .\ruoyi-admin\target\ruoyi-admin.jar --server.port=8082
```

前端：

```powershell
$env:port=8083
npm run dev
```

### 数据库连接失败

检查：

- MySQL 是否已启动
- `ry-vue` 数据库是否存在
- SQL 是否已导入
- 用户名和密码是否与 `application-druid.yml` 或环境变量一致

### Redis 连接失败

检查 Redis 是否已启动，默认地址为：

```text
localhost:6379
```
