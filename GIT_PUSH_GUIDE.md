# GitHub 推送配置指南

## 方案一：使用 HTTPS 方式（最简单）

1. 修改远程仓库地址为 HTTPS：
```bash
cd CalendarAlarm-Android
git remote set-url origin https://github.com/xingxingyz/Caapp.git
```

2. 推送代码（会提示输入 GitHub 用户名和密码/Token）：
```bash
git push --set-upstream origin main
```

3. 推送后 GitHub Actions 会自动构建 APK，在 Releases 页面下载

---

## 方案二：使用 SSH 密钥

1. 生成 SSH 密钥：
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
```

2. 复制公钥到 GitHub：
```bash
cat ~/.ssh/id_ed25519.pub
```
- 登录 GitHub → Settings → SSH and GPG keys → New SSH key
- 粘贴公钥内容

3. 测试连接：
```bash
ssh -T git@github.com
```

4. 重新推送：
```bash
cd CalendarAlarm-Android
git push --set-upstream origin main
```

---

## 方案三：手动上传代码

如果不想配置 Git，可以：
1. 直接修改代码后打包整个项目文件夹
2. 上传到 GitHub 网页版
3. 或者使用 GitHub Desktop 客户端

---

## 关于权限问题的说明

**修复后的权限获取流程：**

1. 首次打开应用 → 弹出对话框"需要开启闹钟权限"
2. 点击"去开启" → 跳转到系统"闹钟和提醒"设置页
3. 在列表中找到"日历闹钟" → 开启开关
4. 返回应用即可正常使用

**为什么要这样？**
Android 12+ 把精确闹钟权限单独放在特殊权限里，不在应用信息页的普通权限列表中。
