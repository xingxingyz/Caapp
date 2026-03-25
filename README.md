# 📅 日历闹钟 - Android App

这是一个原生 Android 应用，可以安装到红米手机上使用。

---

## ✨ 功能特点

| 功能 | 说明 |
|:---|:---|
| 📅 日历选择 | 点击日历选择任意日期 |
| ⏰ 设置闹钟 | 调用系统闹钟，最可靠 |
| 📝 自定义标签 | 给每个闹钟添加备注 |
| 📋 闹钟列表 | 查看和管理所有闹钟 |
| 🔔 响铃提醒 | 到时间自动响铃 |

---

## 📁 项目结构

```
CalendarAlarm/
├── app/src/main/
│   ├── java/com/example/calendaralarm/
│   │   ├── MainActivity.java      # 主界面（日历）
│   │   ├── AlarmActivity.java     # 设置闹钟界面
│   │   ├── AlarmItem.java         # 闹钟数据类
│   │   └── AlarmAdapter.java      # 列表适配器
│   ├── res/layout/
│   │   ├── activity_main.xml      # 主界面布局
│   │   ├── activity_alarm.xml     # 设置界面布局
│   │   └── item_alarm.xml         # 列表项布局
│   ├── res/drawable/
│   │   ├── button_primary.xml     # 主按钮样式
│   │   ├── button_time.xml        # 时间按钮样式
│   │   └── button_cancel.xml      # 取消按钮样式
│   ├── res/values/
│   │   └── strings.xml            # 字符串资源
│   └── AndroidManifest.xml        # 应用配置
├── build.gradle                   # 项目配置
├── settings.gradle                # 项目设置
└── build.sh                       # 编译脚本
```

---

## 🚀 使用方式

### 方式一：在线构建服务（最简单）

我可以帮你用在线构建服务生成 APK：

1. 把项目文件上传到 GitHub
2. 使用 GitHub Actions 或在线构建服务自动编译
3. 下载 APK 安装

### 方式二：Android Studio（推荐）

#### 1. 安装 Android Studio
- 官网下载：https://developer.android.com/studio
- 安装时选择 Standard 配置

#### 2. 导入项目
- 打开 Android Studio
- File → Open → 选择 CalendarAlarm 文件夹
- 等待 Gradle 同步完成

#### 3. 编译 APK
- Build → Build Bundle(s) / APK(s) → Build APK(s)
- 生成的 APK 在：`app/build/outputs/apk/debug/app-debug.apk`

#### 4. 安装到手机
- 连接红米手机（开启 USB 调试）
- 点击 Run → 选择你的手机

### 方式三：命令行编译（高级用户）

```bash
# 1. 进入项目目录
cd CalendarAlarm

# 2. 运行编译脚本
./build.sh

# 3. 安装到手机
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔧 开启开发者模式（红米手机）

1. 打开 **设置**
2. 点击 **我的设备** → **全部参数**
3. 连续点击 **MIUI 版本** 7 次
4. 返回设置，进入 **更多设置** → **开发者选项**
5. 开启 **USB 调试**

---

## 📱 使用说明

1. **选择日期**：点击日历上的任意日期
2. **设置闹钟**：点击「设置闹钟」按钮
3. **选择时间**：点击时间显示，弹出时间选择器
4. **添加标签**：输入闹钟备注（可选）
5. **保存**：点击「保存闹钟」
6. **查看列表**：在主界面查看所有闹钟
7. **删除闹钟**：点击列表中的闹钟，选择删除

---

## ⚠️ 注意事项

- 应用需要调用系统闹钟，请允许相关权限
- 保持应用后台运行以确保闹钟正常
- 部分红米手机可能需要在设置中允许「自启动」

---

## 🆘 需要帮助？

告诉我：
1. 你的红米手机型号？
2. 你能开启开发者模式吗？
3. 你电脑上有 Android Studio 吗？

我可以根据你的情况给出最适合的安装方案！
Build test
