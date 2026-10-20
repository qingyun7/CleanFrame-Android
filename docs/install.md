# 安装和恢复

## 先安装、再选择主页

下载发布页的 APK，在电视上安装并打开。检查画面是否完整、方向键和确认键是否正常、常用应用是否能打开。安装包不会自动修改系统组件或安装其他应用。

如果使用电脑，通过电视的开发者选项开启网络 ADB 调试，让电脑和电视处于同一局域网，在电视上允许该电脑的调试请求。具体开启位置和配对方式由电视固件决定。以下地址是占位符，请替换成自己的电视地址：

```sh
adb connect YOUR_TV_IP:5555
adb install -r CleanFrame-Android-v1.1.2.apk
adb shell am start -n com.qingyun.cleanframe/.MainActivity
```

安装后先在系统的“默认应用／主屏幕应用”里选择 CleanFrame。部分固件也支持下面的命令；记录原来的主页组件，便于恢复：

```sh
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb shell cmd package set-home-activity com.qingyun.cleanframe/.MainActivity
adb shell input keyevent KEYCODE_HOME
```

请从另一个应用按 Home 实际确认。部分小米固件会拦截 Home；系统拒绝切换时，这些命令也无法保证解决。优先使用固件提供的默认桌面选项，不要直接套用网上批量删除系统包的脚本。本项目不附带自动停用原生桌面的脚本。

## 恢复原桌面

在系统默认应用里重新选择原桌面，或将 `set-home-activity` 的参数换成之前记录的组件。先恢复并实际测试 Home，再卸载 CleanFrame。

本项目的安装步骤不会停用小米桌面。只有你此前另外停用了 `com.mitv.tvhome`，且确认它仍存在时，恢复它才需要：

```sh
adb shell pm enable --user 0 com.mitv.tvhome
```

不同型号的组件可能不同，应使用自己记录的值。本项目不保存任何人的电视地址、调试授权或恢复配置。

## 更新

使用同一发布者签名的新版 APK 覆盖安装，通常可保留主页排列和壁纸。自行编译会生成自己的签名，不能覆盖不同签名的 APK；不要为了解决签名冲突直接卸载而丢失偏好。公开安装包不包含影片、NAS 账号，也不会自动安装视频应用。
