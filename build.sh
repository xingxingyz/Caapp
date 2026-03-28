#!/bin/bash
# 快速构建脚本

cd "$(dirname "$0")"

echo "🔨 开始构建日历闹钟 APK..."

# 检查 gradlew 是否存在
if [ ! -f "./gradlew" ]; then
    echo "生成 Gradle Wrapper..."
    gradle wrapper --gradle-version 8.2
fi

# 添加执行权限
chmod +x ./gradlew

# 构建 Debug APK
echo "📦 正在编译..."
./gradlew assembleDebug --no-daemon

# 检查构建结果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ 构建成功！"
    echo "📍 APK 路径: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "安装命令:"
    echo "adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ 构建失败"
    exit 1
fi
